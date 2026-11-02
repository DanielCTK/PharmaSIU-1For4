package com.pharmasiu.mobile.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Data boundary for the six-table PharmaSIU schema. Every persistent operation
 * uses SQLite, foreign keys and database guards, never in-Java sample fixtures.
 * Caller controls authorization; DAL rechecks account role for privileged writes.
 */
public final class PharmaRepository implements AutoCloseable {
    public static final class ProductRow {
        public final long id, price;
        public final String code, name, ingredient, strength, unit, kind;
        public final boolean active;
        ProductRow(long id,String code,String name,String ingredient,String strength,
                   String unit,String kind,long price,boolean active){
            this.id=id;this.code=code;this.name=name;this.ingredient=ingredient;
            this.strength=strength;this.unit=unit;this.kind=kind;this.price=price;this.active=active;
        }
    }
    public static final class LotRow {
        public final long id, productId;
        public final String productCode,code,condition,expiry;
        public final int quantity;
        LotRow(long id,long productId,String productCode,String code,
               String condition,String expiry,int quantity){
            this.id=id;this.productId=productId;this.productCode=productCode;this.code=code;
            this.condition=condition;this.expiry=expiry;this.quantity=quantity;
        }
    }
    public static final class AccountRow {
        public final long id;
        public final String email,name,phone,role;
        public final boolean approvedRx;
        AccountRow(long id,String email,String name,String phone,String role,boolean rx){
            this.id=id;this.email=email;this.name=name;this.phone=phone;this.role=role;
            this.approvedRx=rx;
        }
    }
    public static final class LineRow {
        public final String productCode,productName;
        public final int quantity;
        public final long price;
        LineRow(String code,String name,int qty,long price){
            this.productCode=code;this.productName=name;this.quantity=qty;this.price=price;
        }
    }
    public static final class OrderRow {
        public final String reference,channel,status,created;
        public final long total;
        public final boolean rx;
        public final List<LineRow> lines;
        public final String customerName, customerPhone, pickup;
        OrderRow(String ref,String channel,String status,String created,long total,
                 boolean rx,List<LineRow> lines){
            this(ref, channel, status, created, total, rx, lines, "", "", "11 Tống Hữu Định, Phường An Khánh, TP.HCM");
        }
        OrderRow(String ref,String channel,String status,String created,long total,
                 boolean rx,List<LineRow> lines, String customerName, String customerPhone, String pickup){
            this.reference=ref;this.channel=channel;this.status=status;
            this.created=created;this.total=total;this.rx=rx;
            this.lines=Collections.unmodifiableList(lines);
            this.customerName=customerName!=null?customerName:"";
            this.customerPhone=customerPhone!=null?customerPhone:"";
            this.pickup=pickup!=null?pickup:"11 Tống Hữu Định, Phường An Khánh, TP.HCM";
        }
    }

    private final PharmaDatabase database;
    private final AccountQueries accounts;
    private final CatalogQueries catalog;
    private final OrderQueries orders;
    public PharmaRepository(Context context){
        database=new PharmaDatabase(context);
        accounts=new AccountQueries(this);
        catalog=new CatalogQueries(this);
        orders=new OrderQueries(this);
    }
    SQLiteDatabase db(){return database.database();}
    @Override public void close(){database.close();}
    static String text(Cursor c,int i){return c.isNull(i)?"":c.getString(i);}
    static String now(){return Instant.now().toString();}

    public synchronized List<ProductRow> loadProducts() {
        return catalog.loadProducts();
    }
    public synchronized List<LotRow> loadLots() {
        return catalog.loadLots();
    }
    public synchronized Map<String,Integer> availableStock() {
        return catalog.availableStock();
    }

    public synchronized AccountRow signIn(String identity,String password) {
        return accounts.signIn(identity,password);
    }
    public synchronized boolean registerCustomer(String name,String phone,String email,String password) {
        return accounts.registerCustomer(name,phone,email,password);
    }
    public synchronized boolean updateCustomer(long actor,String name,String email) {
        return accounts.updateCustomer(actor,name,email);
    }
    public synchronized boolean changePassword(long actor,String oldPassword,String newPassword) {
        return accounts.changePassword(actor,oldPassword,newPassword);
    }
    public synchronized boolean changePhone(long actor,String phone) {
        return accounts.changePhone(actor,phone);
    }
    public synchronized boolean hasRole(long actor,String role) {
        return accounts.hasRole(actor,role);
    }

    public synchronized boolean updateProduct(long actor,String code,String name,long price) {
        return catalog.updateProduct(actor,code,name,price);
    }

    public synchronized boolean receiveLot(long actor,String productCode,String lotCode,
             int quantity,LocalDate expiry,boolean quarantine) {
        return catalog.receiveLot(actor,productCode,lotCode,quantity,expiry,quarantine);
    }
    public synchronized boolean quarantineLot(long actor,String lotCode,boolean quarantined) {
        return catalog.quarantineLot(actor,lotCode,quarantined);
    }
    public synchronized boolean saveCustomerOrder(long actor,Map<String,Integer> cart,
                                                   boolean containsRx,String pickup,String[] result) {
        return orders.saveCustomerOrder(actor,cart,containsRx,pickup,result);
    }
    public synchronized List<OrderRow> loadCustomerOrders(long actor) {
        return orders.loadCustomerOrders(actor);
    }
    public synchronized List<OrderRow> loadAllOrders(long actor) {
        return orders.loadAllOrdersForStaff(actor);
    }
    public synchronized boolean updateOrderStatus(long actor, String reference, String newStatus, String reason) {
        return orders.updateOrderStatus(actor, reference, newStatus, reason);
    }
    public synchronized boolean cancelCustomerOrder(long actor,String reference,String reason) {
        return orders.cancelCustomerOrder(actor,reference,reason);
    }

    /**
     * Cash/other method in this isolated mobile app is only a DEMO ledger entry.
     * It is never bank verification or pharmacist approval. Each lot allocation is
     * selected FEFO and decremented by SQLite trigger in the same transaction.
     */
    public synchronized String recordPosSale(long actor,Map<String,Integer> items,
                                              String payment,long cashReceived,int shift) {
        return orders.recordPosSale(actor,items,payment,cashReceived,shift);
    }
    ProductRow productByCode(String code) {
        return catalog.productByCode(code);
    }
    /** Create product and first lot atomically; no Java-side sample catalog mutation. */
    public synchronized boolean createProduct(long actor,String name,String unit,long price,
           int openingQty,String lotCode,LocalDate expiry) {
        return catalog.createProduct(actor,name,unit,price,openingQty,lotCode,expiry);
    }

}
