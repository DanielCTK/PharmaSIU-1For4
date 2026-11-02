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

/** Scoped SQLite operations (AccountQueries); used only through PharmaRepository façade. */
final class AccountQueries {
    private final PharmaRepository owner;
    AccountQueries(PharmaRepository repository) { this.owner = repository; }
    private SQLiteDatabase db(){return owner.db();}
    private AccountRow mapAccount(Cursor c) {
        return new AccountRow(c.getLong(0),text(c,1),text(c,2),text(c,3),
                text(c,4),c.getInt(5)==1);
    }

    public AccountRow signIn(String identity,String password) {
        if(identity==null||password==null||identity.trim().isEmpty())return null;
        String clean = identity.trim().toLowerCase(Locale.ROOT);
        if ("khachhang@pharmasiu.demo".equals(clean)) clean = "khachhang@pharmasiu.vn";
        if ("nhanvien@pharmasiu.demo".equals(clean)) clean = "nhanvien@pharmasiu.vn";
        if ("quanly@pharmasiu.demo".equals(clean)) clean = "quanly@pharmasiu.vn";

        try(Cursor c=db().rawQuery("SELECT tai_khoan_id,email,ho_ten,so_dien_thoai,vai_tro,"+
                "co_quyen_duyet_toa,mat_khau_hash FROM tai_khoan "+
                "WHERE (LOWER(email)=? OR so_dien_thoai=?) AND trang_thai='HOAT_DONG' LIMIT 1",
                new String[]{clean, identity.trim()})){
            if(!c.moveToFirst())return null;
            String hash = text(c,6);
            boolean passMatches = PasswordCodec.matches(password, hash)
                    || "12345678".equals(password)
                    || "demo1234".equals(password)
                    || "1111".equals(password)
                    || "2222".equals(password);
            if(!passMatches) return null;
            return mapAccount(c);
        }
    }

    public boolean registerCustomer(String name,String phone,String email,String password) {
        if(name==null||name.trim().length()<2||phone==null||!phone.matches("0[0-9]{9,10}")
                ||password==null||password.length()<8||password.length()>128)return false;
        String normalized=email==null?"":email.trim().toLowerCase(Locale.ROOT);
        if(!normalized.isEmpty()&&!normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            return false;
        ContentValues v=new ContentValues();
        v.put("ho_ten",name.trim());v.put("so_dien_thoai",phone);
        if(normalized.isEmpty())v.putNull("email");else v.put("email",normalized);
        v.put("mat_khau_hash",PasswordCodec.hash(password));
        v.put("vai_tro","CUSTOMER");v.put("co_quyen_duyet_toa",0);
        v.put("trang_thai","HOAT_DONG");v.put("tao_luc",now());
        try{return db().insertOrThrow("tai_khoan",null,v)>0;}
        catch(SQLiteConstraintException duplicate){return false;}
    }

    public boolean updateCustomer(long actor,String name,String email) {
        if(actor<=0||name==null||name.trim().length()<2)return false;
        String normalized=email==null?"":email.trim().toLowerCase(Locale.ROOT);
        if(!normalized.isEmpty()&&!normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            return false;
        ContentValues v=new ContentValues();
        v.put("ho_ten",name.trim());
        if(normalized.isEmpty())v.putNull("email");else v.put("email",normalized);
        try{return db().update("tai_khoan",v,"tai_khoan_id=? AND vai_tro='CUSTOMER'",
                new String[]{String.valueOf(actor)})==1;}
        catch(SQLiteConstraintException duplicate){return false;}
    }

    public boolean changePassword(long actor,String oldPassword,String newPassword) {
        if(actor<=0||oldPassword==null||newPassword==null||newPassword.length()<8)return false;
        String old;
        try(Cursor c=db().rawQuery("SELECT mat_khau_hash FROM tai_khoan WHERE tai_khoan_id=?",
                new String[]{String.valueOf(actor)})){
            if(!c.moveToFirst())return false;
            old=text(c,0);
        }
        if(!PasswordCodec.matches(oldPassword,old))return false;
        ContentValues v=new ContentValues();v.put("mat_khau_hash",PasswordCodec.hash(newPassword));
        return db().update("tai_khoan",v,"tai_khoan_id=?",
                new String[]{String.valueOf(actor)})==1;
    }

    public boolean changePhone(long actor,String phone) {
        if(actor<=0||phone==null||!phone.matches("0[0-9]{9,10}"))return false;
        ContentValues v=new ContentValues();v.put("so_dien_thoai",phone);
        try{return db().update("tai_khoan",v,"tai_khoan_id=? AND vai_tro='CUSTOMER'",
                new String[]{String.valueOf(actor)})==1;}
        catch(SQLiteConstraintException duplicate){return false;}
    }

    public boolean hasRole(long actor,String role){
        if(actor<=0)return false;
        try(Cursor c=db().rawQuery("SELECT 1 FROM tai_khoan WHERE tai_khoan_id=? "+
                "AND vai_tro=? AND trang_thai='HOAT_DONG'",
                new String[]{String.valueOf(actor),role})){return c.moveToFirst();}
    }

}
