package com.pharmasiu.mobile.data.local;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.pharmasiu.mobile.data.local.PharmaRepository.AccountRow;
import com.pharmasiu.mobile.data.local.PharmaRepository.ProductRow;
import com.pharmasiu.mobile.data.local.PharmaRepository.LotRow;
import com.pharmasiu.mobile.data.local.PharmaRepository.OrderRow;
import com.pharmasiu.mobile.data.local.PharmaRepository.LineRow;
import static com.pharmasiu.mobile.data.local.PharmaRepository.now;
import static com.pharmasiu.mobile.data.local.PharmaRepository.text;

/** Scoped SQLite operations (CatalogQueries); used only through PharmaRepository façade. */
final class CatalogQueries {
    private final PharmaRepository owner;
    CatalogQueries(PharmaRepository repository) { this.owner = repository; }
    private SQLiteDatabase db(){return owner.db();}
    private boolean hasRole(long actor,String role){return owner.hasRole(actor,role);}
    public List<ProductRow> loadProducts() {
        List<ProductRow> rows=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT san_pham_id,ma_san_pham,ten_san_pham,"+
                "hoat_chat,ham_luong,don_vi_ban,loai_thuoc,gia_niem_yet_vnd,dang_ban "+
                "FROM san_pham ORDER BY ten_san_pham",null)){
            while(c.moveToNext())rows.add(new ProductRow(c.getLong(0),c.getString(1),
                    c.getString(2),text(c,3),text(c,4),c.getString(5),c.getString(6),
                    c.getLong(7),c.getInt(8)==1));
        }
        return rows;
    }

    public List<LotRow> loadLots() {
        List<LotRow> rows=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT l.lo_thuoc_id,l.san_pham_id,p.ma_san_pham,"+
                "l.so_lo_nha_sx,l.tinh_trang,l.han_su_dung,l.so_luong_ton FROM lo_thuoc l "+
                "JOIN san_pham p ON p.san_pham_id=l.san_pham_id ORDER BY l.han_su_dung,l.lo_thuoc_id",null)){
            while(c.moveToNext())rows.add(new LotRow(c.getLong(0),c.getLong(1),
                    c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getInt(6)));
        }
        return rows;
    }

    public Map<String,Integer> availableStock() {
        Map<String,Integer> stock=new LinkedHashMap<>();
        try(Cursor c=db().rawQuery("SELECT ma_san_pham,COALESCE(SUM(kha_dung_du_kien),0) "+
                "FROM v_ton_kha_dung GROUP BY ma_san_pham",null)){
            while(c.moveToNext())stock.put(c.getString(0),c.getInt(1));
        }
        return stock;
    }

    public boolean updateProduct(long actor,String code,String name,long price){
        if(!hasRole(actor,"ADMIN")||code==null||name==null||name.trim().length()<2||price<=0)return false;
        ContentValues v=new ContentValues();v.put("ten_san_pham",name.trim());
        v.put("gia_niem_yet_vnd",price);
        return db().update("san_pham",v,"ma_san_pham=? AND loai_thuoc!='RX'",
                new String[]{code})==1;
    }

    private long productId(String code){
        try(Cursor c=db().rawQuery("SELECT san_pham_id FROM san_pham WHERE ma_san_pham=?",
                new String[]{code})){return c.moveToFirst()?c.getLong(0):-1;}
    }

    public boolean receiveLot(long actor,String productCode,String lotCode,
             int quantity,LocalDate expiry,boolean quarantine){
        if(!hasRole(actor,"ADMIN")||productCode==null||lotCode==null||lotCode.trim().isEmpty()
          ||quantity<1||expiry==null||(!quarantine&&!expiry.isAfter(LocalDate.now())))return false;
        long productId=productId(productCode);
        if(productId<=0)return false;
        ContentValues v=new ContentValues();
        v.put("san_pham_id",productId);v.put("so_lo_nha_sx",lotCode.trim());
        v.put("ngay_nhap",now());v.put("han_su_dung",expiry.toString());
        v.put("so_luong_nhap",quantity);v.put("so_luong_ton",quantity);
        v.put("tinh_trang",quarantine?"CACH_LY":"DAT");
        v.put("nguoi_nhap_id",actor);
        try{return db().insertOrThrow("lo_thuoc",null,v)>0;}
        catch(SQLiteConstraintException duplicate){return false;}
    }

    public boolean quarantineLot(long actor,String lotCode,boolean quarantined){
        if(!hasRole(actor,"ADMIN")||lotCode==null)return false;
        ContentValues v=new ContentValues();
        v.put("tinh_trang",quarantined?"CACH_LY":"DAT");
        try{return db().update("lo_thuoc",v,"so_lo_nha_sx=?",
                new String[]{lotCode})==1;}
        catch(SQLiteConstraintException constraint){return false;}
    }

    ProductRow productByCode(String code){
        for(ProductRow p:loadProducts())if(p.code.equals(code))return p;
        return null;
    }

    public boolean createProduct(long actor,String name,String unit,long price,
           int openingQty,String lotCode,LocalDate expiry) {
        if(!hasRole(actor,"ADMIN") || name==null || name.trim().length()<2
            || unit==null || unit.trim().isEmpty() || price<=0 || openingQty<0
            || (openingQty>0 && (lotCode==null||lotCode.trim().length()<2
                  || expiry==null||!expiry.isAfter(LocalDate.now()))))return false;
        SQLiteDatabase sql=db();sql.beginTransaction();
        try {
            String code="SHOP-"+UUID.randomUUID().toString().replace("-","").substring(0,15).toUpperCase(Locale.ROOT);
            ContentValues product=new ContentValues();
            product.put("ma_san_pham",code);product.put("ten_san_pham",name.trim());
            product.put("don_vi_ban",unit.trim());product.put("loai_thuoc","KHAC");
            product.put("gia_niem_yet_vnd",price);product.put("dang_ban",1);
            long productId=sql.insertOrThrow("san_pham",null,product);
            if(openingQty>0){
                ContentValues lot=new ContentValues();
                lot.put("san_pham_id",productId);lot.put("so_lo_nha_sx",lotCode.trim());
                lot.put("ngay_nhap",now());lot.put("han_su_dung",expiry.toString());
                lot.put("so_luong_nhap",openingQty);lot.put("so_luong_ton",openingQty);
                lot.put("tinh_trang","DAT");lot.put("nguoi_nhap_id",actor);
                sql.insertOrThrow("lo_thuoc",null,lot);
            }
            sql.setTransactionSuccessful();return true;
        }catch(RuntimeException invalid){return false;}
        finally{sql.endTransaction();}
    }

}
