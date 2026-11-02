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

/** Scoped SQLite operations (OrderQueries); used only through PharmaRepository façade. */
final class OrderQueries {
    private final PharmaRepository owner;
    OrderQueries(PharmaRepository repository) { this.owner = repository; }
    private SQLiteDatabase db(){return owner.db();}
    private boolean hasRole(long actor,String role){return owner.hasRole(actor,role);}
    private ProductRow productByCode(String code){return owner.productByCode(code);}
    private List<ProductRow> loadProducts(){return owner.loadProducts();}
    public boolean saveCustomerOrder(long actor,Map<String,Integer> cart,
                                                   boolean containsRx,String pickup,String[] result){
        if(!hasRole(actor,"CUSTOMER")||cart==null||cart.isEmpty())return false;
        SQLiteDatabase sql=db();
        sql.beginTransaction();
        try {
            String ref="PS-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(Locale.ROOT);
            ContentValues order=new ContentValues();
            order.put("ma_don",ref);order.put("kenh_ban","APP");
            order.put("nguoi_tao_id",actor);order.put("khach_hang_id",actor);
            order.put("trang_thai",containsRx?"CHO_DUYET_TOA":"CHO_XU_LY");
            order.put("trang_thai_toa",containsRx?"CHUA_GUI":"KHONG_CAN");
            order.put("trang_thai_tt","CHUA_THANH_TOAN");
            order.put("so_tien_da_thu_vnd",0);
            order.put("ghi_chu","Yêu cầu nhận hàng tại: "+(pickup==null?"Chưa xác định":pickup)+
                    (containsRx?"; chưa gửi/duyệt toa qua backend":""));
            long id=sql.insertOrThrow("don_hang",null,order);
            for(Map.Entry<String,Integer> selected:cart.entrySet()){
                if(selected.getValue()==null||selected.getValue()<=0||selected.getValue()>20)
                    throw new IllegalArgumentException("Invalid quantity");
                try(Cursor c=sql.rawQuery("SELECT san_pham_id,gia_niem_yet_vnd FROM san_pham "+
                        "WHERE ma_san_pham=? AND dang_ban=1",new String[]{selected.getKey()})){
                    if(!c.moveToFirst())throw new IllegalArgumentException("Unknown/inactive product");
                    ContentValues line=new ContentValues();
                    line.put("don_hang_id",id);line.put("san_pham_id",c.getLong(0));
                    line.put("so_luong",selected.getValue());line.put("don_gia_chot_vnd",c.getLong(1));
                    line.put("so_luong_giu",0);line.put("so_luong_xuat",0);
                    sql.insertOrThrow("chi_tiet_don_hang",null,line);
                }
            }
            sql.setTransactionSuccessful();if(result!=null&&result.length>0)result[0]=ref;
            return true;
        } catch(RuntimeException invalid){return false;}
        finally{sql.endTransaction();}
    }

    public List<OrderRow> loadCustomerOrders(long actor){
        if(!hasRole(actor,"CUSTOMER"))return Collections.emptyList();
        List<OrderRow> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT don_hang_id,ma_don,kenh_ban,trang_thai,tao_luc "+
                "FROM don_hang WHERE khach_hang_id=? ORDER BY don_hang_id DESC",
                new String[]{String.valueOf(actor)})){
            while(c.moveToNext()){
                long id=c.getLong(0);
                List<LineRow> lines=new ArrayList<>();long total=0;boolean rx=false;
                try(Cursor detail=db().rawQuery("SELECT p.ma_san_pham,p.ten_san_pham,"+
                        "c.so_luong,c.don_gia_chot_vnd,p.loai_thuoc FROM chi_tiet_don_hang c "+
                        "JOIN san_pham p ON p.san_pham_id=c.san_pham_id "+
                        "WHERE c.don_hang_id=? ORDER BY c.chi_tiet_id",
                        new String[]{String.valueOf(id)})){
                    while(detail.moveToNext()){
                        lines.add(new LineRow(detail.getString(0),detail.getString(1),
                                detail.getInt(2),detail.getLong(3)));
                        total+=detail.getInt(2)*detail.getLong(3);
                        if("RX".equals(detail.getString(4)))rx=true;
                    }
                }
                out.add(new OrderRow(c.getString(1),c.getString(2),c.getString(3),
                        c.getString(4),total,rx,lines));
            }
        }
        return out;
    }

    public boolean cancelCustomerOrder(long actor,String reference,String reason){
        if(!hasRole(actor,"CUSTOMER")||reference==null||reason==null||reason.trim().length()<3)return false;
        ContentValues v=new ContentValues();v.put("trang_thai","DA_HUY");
        v.put("ghi_chu","Khách yêu cầu hủy: "+reason.trim().substring(0,Math.min(160,reason.trim().length())));
        try {return db().update("don_hang",v,"ma_don=? AND khach_hang_id=? "+
                "AND trang_thai IN ('CHO_XU_LY','CHO_DUYET_TOA')",
                new String[]{reference,String.valueOf(actor)})==1;}
        catch(SQLiteConstraintException invalid){return false;}
    }

    public String recordPosSale(long actor,Map<String,Integer> items,
                                              String payment,long cashReceived,int shift){
        if(items==null||items.isEmpty()||(!hasRole(actor,"ADMIN")&&!hasRole(actor,"CASHIER")))
            return null;
        if(!"CASH".equals(payment)&&!"OTHER_DEMO".equals(payment))return null;
        long total=0;
        SQLiteDatabase sql=db();sql.beginTransaction();
        try{
            for(Map.Entry<String,Integer> entry:items.entrySet()){
                ProductRow p=productByCode(entry.getKey());
                if(p==null||!p.active||"RX".equals(p.kind)||p.price<=0
                   ||entry.getValue()==null||entry.getValue()<=0)
                    throw new IllegalArgumentException("Unpriced/Rx/inactive");
                total+=p.price*entry.getValue();
            }
            if(total<=0||("CASH".equals(payment)&&cashReceived<total))
                throw new IllegalArgumentException("Insufficient cash");
            String ref="POS-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(Locale.ROOT);
            ContentValues sale=new ContentValues();
            sale.put("ma_don",ref);sale.put("kenh_ban","POS");sale.put("nguoi_tao_id",actor);
            sale.put("nhan_vien_xu_ly_id",actor);sale.put("trang_thai","CHO_XU_LY");
            sale.put("trang_thai_toa","KHONG_CAN");
            sale.put("trang_thai_tt","DEMO_DA_GHI_NHAN");
            sale.put("phuong_thuc_tt","CASH".equals(payment)?"TIEN_MAT_DEMO":"KHAC_DEMO");
            sale.put("so_tien_da_thu_vnd",total);sale.put("thanh_toan_luc",now());
            sale.put("ma_ca","LOCAL-"+shift);
            sale.put("ghi_chu","Sổ ghi nhận tại thiết bị; chưa đối chiếu ngân hàng hoặc tiền thật");
            long orderId=sql.insertOrThrow("don_hang",null,sale);
            for(Map.Entry<String,Integer> entry:items.entrySet()){
                ProductRow product=productByCode(entry.getKey());
                int remaining=entry.getValue();
                try(Cursor eligible=sql.rawQuery("SELECT l.lo_thuoc_id,v.kha_dung_du_kien "+
                        "FROM lo_thuoc l JOIN v_ton_kha_dung v ON v.lo_thuoc_id=l.lo_thuoc_id "+
                        "WHERE l.san_pham_id=? AND v.kha_dung_du_kien>0 "+
                        "ORDER BY l.han_su_dung,l.lo_thuoc_id",
                        new String[]{String.valueOf(product.id)})){
                    while(remaining>0&&eligible.moveToNext()){
                        int picked=Math.min(remaining,eligible.getInt(1));
                        ContentValues line=new ContentValues();
                        line.put("don_hang_id",orderId);line.put("san_pham_id",product.id);
                        line.put("lo_thuoc_id",eligible.getLong(0));line.put("so_luong",picked);
                        line.put("don_gia_chot_vnd",product.price);
                        line.put("so_luong_giu",0);line.put("so_luong_xuat",picked);
                        sql.insertOrThrow("chi_tiet_don_hang",null,line);
                        remaining-=picked;
                    }
                }
                if(remaining!=0)throw new IllegalArgumentException("Insufficient eligible FEFO lots");
            }
            ContentValues completed=new ContentValues();
            completed.put("trang_thai","HOAN_TAT");completed.put("giao_luc",now());
            if(sql.update("don_hang",completed,"don_hang_id=?",
                    new String[]{String.valueOf(orderId)})!=1)
                throw new IllegalStateException("Cannot complete local POS record");
            sql.setTransactionSuccessful();
            return ref;
        }catch(RuntimeException invalid){return null;}
        finally{sql.endTransaction();}
    }

}
