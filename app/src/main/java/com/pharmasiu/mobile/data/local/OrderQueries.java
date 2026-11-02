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
        try(Cursor c=db().rawQuery("SELECT d.don_hang_id, d.ma_don, d.kenh_ban, d.trang_thai, d.tao_luc, d.ghi_chu "+
                "FROM don_hang d WHERE d.khach_hang_id=? ORDER BY d.don_hang_id DESC",
                new String[]{String.valueOf(actor)})){
            while(c.moveToNext()){
                long id=c.getLong(0);
                String ghiChu=c.getString(5);
                String pickup = "11 Tống Hữu Định, Phường An Khánh, TP.HCM";
                if(ghiChu!=null && ghiChu.contains("nhận hàng tại: ")){
                    int start = ghiChu.indexOf("nhận hàng tại: ") + "nhận hàng tại: ".length();
                    int end = ghiChu.indexOf(";", start);
                    pickup = end > start ? ghiChu.substring(start, end).trim() : ghiChu.substring(start).trim();
                }
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
                        c.getString(4),total,rx,lines, "", "", pickup));
            }
        }
        return out;
    }

    public List<OrderRow> loadAllOrdersForStaff(long actor){
        if(!hasRole(actor,"ADMIN")&&!hasRole(actor,"CASHIER"))return Collections.emptyList();
        List<OrderRow> out=new ArrayList<>();
        try(Cursor c=db().rawQuery("SELECT d.don_hang_id, d.ma_don, d.kenh_ban, d.trang_thai, d.tao_luc, "+
                "t.ho_ten, t.so_dien_thoai, d.ghi_chu FROM don_hang d "+
                "LEFT JOIN tai_khoan t ON t.tai_khoan_id = d.khach_hang_id "+
                "WHERE d.kenh_ban='APP' ORDER BY d.don_hang_id DESC", null)){
            while(c.moveToNext()){
                long id=c.getLong(0);
                String ref=c.getString(1);
                String channel=c.getString(2);
                String status=c.getString(3);
                String created=c.getString(4);
                String custName=c.getString(5);
                String custPhone=c.getString(6);
                String ghiChu=c.getString(7);
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
                String pickup = "11 Tống Hữu Định, Phường An Khánh, TP.HCM";
                if(ghiChu!=null && ghiChu.contains("nhận hàng tại: ")){
                    int start = ghiChu.indexOf("nhận hàng tại: ") + "nhận hàng tại: ".length();
                    int end = ghiChu.indexOf(";", start);
                    pickup = end > start ? ghiChu.substring(start, end).trim() : ghiChu.substring(start).trim();
                }
                out.add(new OrderRow(ref, channel, status, created, total, rx, lines, custName, custPhone, pickup));
            }
        }
        return out;
    }

    public boolean updateOrderStatus(long actor, String reference, String newStatus, String reason){
        if((!hasRole(actor,"ADMIN")&&!hasRole(actor,"CASHIER"))||reference==null||newStatus==null)return false;
        SQLiteDatabase sql=db();
        sql.beginTransaction();
        try {
            long orderId = -1;
            try(Cursor c = sql.rawQuery("SELECT don_hang_id FROM don_hang WHERE ma_don=?", new String[]{reference})) {
                if(c.moveToFirst()) orderId = c.getLong(0);
            }
            if(orderId == -1) return false;

            if("SAN_SANG_NHAN".equals(newStatus)){
                ContentValues v=new ContentValues();
                v.put("trang_thai", "SAN_SANG_NHAN");
                v.put("nhan_vien_xu_ly_id", actor);
                v.put("nguoi_duyet_toa_id", actor);
                v.put("trang_thai_toa", "DA_DUYET");
                v.put("duyet_toa_luc", now());
                boolean ok = sql.update("don_hang", v, "don_hang_id=?", new String[]{String.valueOf(orderId)}) == 1;
                if(ok) sql.setTransactionSuccessful();
                return ok;
            } else if("HOAN_TAT".equals(newStatus)){
                long totalAmount = 0;
                List<long[]> lines = new ArrayList<>();
                try(Cursor c = sql.rawQuery("SELECT chi_tiet_id, san_pham_id, so_luong, don_gia_chot_vnd FROM chi_tiet_don_hang WHERE don_hang_id=?", 
                        new String[]{String.valueOf(orderId)})) {
                    while(c.moveToNext()){
                        lines.add(new long[]{c.getLong(0), c.getLong(1), c.getLong(2), c.getLong(3)});
                        totalAmount += c.getLong(2) * c.getLong(3);
                    }
                }

                for(long[] line : lines){
                    long cId = line[0];
                    long pId = line[1];
                    long qty = line[2];
                    long lotId = -1;
                    try(Cursor lc = sql.rawQuery("SELECT lo_thuoc_id FROM lo_thuoc WHERE san_pham_id=? AND tinh_trang='DAT' " +
                            "AND date(han_su_dung)>date('now') AND so_luong_ton>=? ORDER BY han_su_dung ASC LIMIT 1",
                            new String[]{String.valueOf(pId), String.valueOf(qty)})){
                        if(lc.moveToFirst()){
                            lotId = lc.getLong(0);
                        }
                    }
                    if(lotId != -1){
                        ContentValues cl = new ContentValues();
                        cl.put("lo_thuoc_id", lotId);
                        sql.update("chi_tiet_don_hang", cl, "chi_tiet_id=?", new String[]{String.valueOf(cId)});
                    }
                }

                ContentValues orderPre = new ContentValues();
                orderPre.put("trang_thai", "SAN_SANG_NHAN");
                orderPre.put("trang_thai_tt", "DA_THANH_TOAN");
                orderPre.put("thanh_toan_luc", now());
                orderPre.put("giao_luc", now());
                orderPre.put("so_tien_da_thu_vnd", totalAmount);
                orderPre.put("nhan_vien_xu_ly_id", actor);
                sql.update("don_hang", orderPre, "don_hang_id=?", new String[]{String.valueOf(orderId)});

                for(long[] line : lines){
                    long cId = line[0];
                    long qty = line[2];
                    ContentValues cl = new ContentValues();
                    cl.put("so_luong_giu", 0);
                    cl.put("so_luong_xuat", qty);
                    sql.update("chi_tiet_don_hang", cl, "chi_tiet_id=?", new String[]{String.valueOf(cId)});
                }

                ContentValues orderDone = new ContentValues();
                orderDone.put("trang_thai", "HOAN_TAT");
                boolean ok = sql.update("don_hang", orderDone, "don_hang_id=?", new String[]{String.valueOf(orderId)}) == 1;
                if(ok) sql.setTransactionSuccessful();
                return ok;
            } else if("DA_HUY".equals(newStatus)){
                ContentValues v=new ContentValues();
                v.put("trang_thai", "DA_HUY");
                v.put("nhan_vien_xu_ly_id", actor);
                if(reason!=null && !reason.trim().isEmpty()){
                    v.put("ly_do_tu_choi_toa", reason.trim());
                    v.put("ghi_chu", "Nhà thuốc từ chối: " + reason.trim());
                }
                boolean ok = sql.update("don_hang", v, "don_hang_id=?", new String[]{String.valueOf(orderId)}) == 1;
                if(ok) sql.setTransactionSuccessful();
                return ok;
            }
            return false;
        } catch(Exception e){
            return false;
        } finally {
            sql.endTransaction();
        }
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
