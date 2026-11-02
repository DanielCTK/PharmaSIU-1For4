package com.pharmasiu.mobile.core;

import java.time.LocalDate;
import android.content.Context;
import com.pharmasiu.mobile.data.local.PharmaRepository;
import com.pharmasiu.mobile.core.state.CustomerAccountStore;
import com.pharmasiu.mobile.core.state.InventoryStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * UI state adapter. Production creation uses a six-table device SQLite repository;
 * no example account/product is automatically created in the installed application.
 * The no-argument constructor retains isolated legacy fixtures ONLY for historical JVM tests.
 * Do not use fixture-mode in any Android screen.
 */
public final class DemoState {
    public static final String NONE = "NONE";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String CASHIER = "CASHIER";
    public static final String ADMIN = "ADMIN";

    public static final class Product {
        public final String id, subtitle, category, unit, description;
        public String name;
        public long priceVnd;
        public boolean requiresPrescription;
        public Product(String id, String name, String subtitle, long priceVnd,
                       String category, boolean requiresPrescription, String unit, String description) {
            this.id=id; this.name=name; this.subtitle=subtitle; this.priceVnd=priceVnd;
            this.category=category; this.requiresPrescription=requiresPrescription; this.unit=unit;
            this.description=description;
        }
    }
    public static final class CustomerProfile {
        public String phone;
        public String displayName, email, birthday, password;
        public CustomerProfile(String name,String phone,String email,String birthday,String password){
            this.displayName=name;this.phone=phone;this.email=email;this.birthday=birthday;
            this.password=password;
        }
    }
    public static final class Address {
        public final String id;
        public String label, receiver, phone, detail;
        public boolean isDefault;
        public Address(String id,String label,String receiver,String phone,String detail,boolean isDefault){
            this.id=id;this.label=label;this.receiver=receiver;this.phone=phone;this.detail=detail;
            this.isDefault=isDefault;
        }
    }
    public static final class Order {
        public final String reference, pickup, customerName, contactPhone, displayTime;
        public final Map<String,String> productNamesSnapshot;
        public final Map<String,Long> unitPricesSnapshot;
        public String customerPhone;
        public final LinkedHashMap<String,Integer> lines;
        public final long pricedSubtotalVnd;
        public final boolean containsUnpriced, containsRx;
        public String status = "Đã lưu yêu cầu DEMO";
        public String cancelReason;
        public Order(String reference, String phone, String pickup, String name, String contact, Map<String,Integer> lines,
                     long subtotal, boolean unpriced, boolean rx, Map<String,String> names, Map<String,Long> prices) {
            this.reference=reference; this.customerPhone=phone; this.pickup=pickup;
            this.customerName=name;this.contactPhone=contact;
            this.displayTime=java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",new Locale("vi","VN")));
            this.lines=new LinkedHashMap<>(lines);
            productNamesSnapshot=Collections.unmodifiableMap(new LinkedHashMap<>(names));
            unitPricesSnapshot=Collections.unmodifiableMap(new LinkedHashMap<>(prices));
            pricedSubtotalVnd=subtotal;
            containsUnpriced=unpriced; containsRx=rx;
        }
    }
    public static final class Lot {
        public final String code, productId;
        public int quantity;
        public LocalDate expiresOn;
        public boolean quarantined;
        public Lot(String code, String productId, int quantity, LocalDate expiresOn) {
            this.code=code; this.productId=productId; this.quantity=quantity; this.expiresOn=expiresOn;
        }
    }
    public static final class Sale {
        public final String reference, method, cashierName, createdAt;
        public final long amountVnd, receivedVnd, changeVnd;
        public final int shiftNumber;
        public final LinkedHashMap<String,Integer> lines;
        /** Immutable product-name/price snapshots created before stock mutations. */
        public final com.pharmasiu.mobile.domain.CounterSale immutableDetail;
        public Sale(String reference, String method, long amount, Map<String,Integer> cart,
                    int shiftNumber, java.util.List<com.pharmasiu.mobile.domain.CounterLine> snapshots,
                    boolean managerOperator, long tendered) {
            this.reference=reference;this.method=method;this.amountVnd=amount;
            this.lines=new LinkedHashMap<>(cart);this.shiftNumber=shiftNumber;
            this.cashierName=managerOperator?"Quản lý demo":"Nhân viên demo";
            this.createdAt=java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm",new Locale("vi","VN")));
            this.receivedVnd="CASH".equals(method)?tendered:0L;
            this.changeVnd="CASH".equals(method)?Math.max(0L,tendered-amount):0L;
            this.immutableDetail=new com.pharmasiu.mobile.domain.CounterSale(
                reference,cashierName,createdAt,snapshots,
                com.pharmasiu.mobile.domain.CounterPayment.valueOf(method),receivedVnd);
        }
    }

    public final List<Product> products = new ArrayList<>();
    public final Map<String,Integer> cart = new LinkedHashMap<>();
    public final Map<String,Integer> stock = new LinkedHashMap<>();
    public final Map<String,Integer> posCart = new LinkedHashMap<>();
    public final List<Order> orders = new ArrayList<>();
    public final List<Lot> lots = new ArrayList<>();
    public final List<Sale> sales = new ArrayList<>();
    public final List<String> favorites = new ArrayList<>();
    public final Map<String,String> staffPhotoUris = new LinkedHashMap<>();
    public final Map<String,CustomerProfile> customerAccounts = new LinkedHashMap<>();
    public final Map<String,List<Address>> addressBooks = new LinkedHashMap<>();
    private final CustomerAccountStore customerStore = new CustomerAccountStore();
    public static final class DemoInventoryCount {
        public final String productId,productName,note;
        public final int expectedUnits,countedUnits,difference;
        public DemoInventoryCount(String id,String productName,int expected,int counted,String note){
            this.productId=id;this.productName=productName;this.expectedUnits=expected;
            this.countedUnits=counted;this.difference=counted-expected;this.note=note;
        }
    }
    public static final class DemoShiftReconciliation {
        public final int shiftNumber,invoiceCount;
        public final long openingCashVnd,expectedCashVnd,otherDemoVnd,manuallyCountedCashVnd,differenceVnd;
        public DemoShiftReconciliation(int shiftNumber,int count,long openingCash,long expectedCash,
                                long other,long counted){
            this.shiftNumber=shiftNumber;this.invoiceCount=count;this.openingCashVnd=openingCash;
            this.expectedCashVnd=expectedCash;this.otherDemoVnd=other;
            this.manuallyCountedCashVnd=counted;
            this.differenceVnd=counted-(openingCash+expectedCash);
        }
    }
    public final List<DemoInventoryCount> inventoryCountDetails=new ArrayList<>();
    public final List<DemoShiftReconciliation> shiftReconciliationDetails=new ArrayList<>();
    public final List<String> inventoryChecks = new ArrayList<>();
    public final List<String> shiftReconciliations = new ArrayList<>();
    private final InventoryStore inventoryStore = new InventoryStore(this);
    private PharmaRepository repository;
    private long authenticatedAccountId = -1L;
    /** True only for device-backed state, not for the historical JVM fixture constructor. */
    public boolean isDatabaseBacked(){return repository!=null;}
    public long authenticatedAccountId(){return authenticatedAccountId;}
    public PharmaRepository repository(){return repository;}
    public void refreshDatabaseViews(){
        if(repository==null)return;
        products.clear();stock.clear();lots.clear();
        for(PharmaRepository.ProductRow row:repository.loadProducts()){
            String category="RX".equals(row.kind)?"Kê đơn":
                ("OTC".equals(row.kind)?"Không kê đơn":
                (row.code.startsWith("MP-")||row.code.startsWith("DA-")?"Mỹ phẩm":
                (row.code.startsWith("TP-")||row.code.startsWith("TPCN-")||row.code.startsWith("SNACK-")||row.code.startsWith("DOUONG-")?"Thực phẩm":
                (row.code.startsWith("VTYT-")?"ThiẾt bị":
                (row.code.startsWith("CN-")||row.code.startsWith("TIEUDUNG-")?"Chăm sóc":"Sản phẩm khác")))));
            String detail=(row.ingredient.isEmpty()?"":row.ingredient+" ")+row.strength;
            String description=detail.trim().isEmpty()?
                "Thông tin được lưu trong cơ sở dữ liệu thiết bị; xác minh tại quầy trước sử dụng.":
                detail.trim();
            products.add(new Product(row.code,row.name,
                row.unit+" · "+category,row.price,category,
                "RX".equals(row.kind),row.unit,description));
            stock.put(row.code,0);
        }
        for(PharmaRepository.LotRow lot:repository.loadLots()){
            LocalDate expiry;
            try{expiry=LocalDate.parse(lot.expiry);}catch(Exception error){continue;}
            Lot item=new Lot(lot.code,lot.productCode,lot.quantity,expiry);
            item.quarantined=!"DAT".equals(lot.condition);
            lots.add(item);
        }
        stock.putAll(repository.availableStock());
    }
    /** Used by Android Activity; starts from EMPTY schema, never the 93-row fixture. */
    public DemoState(Context context){
        this(false);
        repository=new PharmaRepository(context);
        refreshDatabaseViews();
    }


    public String role=NONE, customerPhone="", selectedProductId="", selectedOrderId="";
    public String chosenPrescriptionName=null, chosenPrescriptionFingerprint=null;
    public boolean shiftOpen=false;
    public int shiftNumber=0;
    public long openingCashVnd=0, cashCollectedVnd=0, cashCountedVnd=0;
    public String lastDemoMessage="";
    private int orderCounter=0, saleCounter=0;

    /** Historical no-arg constructor for standalone Java unit tests only. */
    @Deprecated public DemoState(){this(true);}
    private DemoState(boolean legacyTestFixtures) {
        if(!legacyTestFixtures)return;
        customerAccounts.put("0900000000",new CustomerProfile("Khách hàng mẫu","0900000000","demo@pharmasiu.example","","demo1234"));
        seed("mask-01","Khẩu trang y tế","Hộp 50 chiếc · Vật tư y tế",36000,"Vật tư y tế",false,"Hộp");
        seed("saline-01","Dung dịch NaCl 0,9%","Chai 500 ml · Chăm sóc cá nhân",18000,"Chăm sóc",false,"Chai");
        seed("cotton-01","Bông y tế","Gói 100 g · Vật tư y tế",24000,"Vật tư y tế",false,"Gói");
        seed("thermometer-01","Nhiệt kế điện tử","Thiết bị chăm sóc gia đình",129000,"Thiết bị",false,"Cái");
        seed("bandage-01","Băng gạc vô trùng","Gói 10 miếng · Vật tư y tế",29000,"Vật tư y tế",false,"Gói");
        seed("rx-demo-01","Thuốc kê đơn · Mẫu","Sản phẩm minh họa quy trình Rx",0,"Thuốc kê đơn",true,"Hộp");
        // Ported from ExpandedMockCatalog.kt; 24 additional UI-only fixtures.
        seed("otc-01","Sản phẩm không kê đơn · Mẫu A","Chỉ là mẫu UI, chưa xác minh phân loại",42000L,"Không kê đơn",false,"Hộp");
        seed("otc-02","Sản phẩm không kê đơn · Mẫu B","Chỉ là mẫu UI, chưa xác minh phân loại",55000L,"Không kê đơn",false,"Chai");
        seed("otc-03","Sản phẩm không kê đơn · Mẫu C","Chỉ là mẫu UI, chưa xác minh phân loại",32000L,"Không kê đơn",false,"Gói");
        seed("rx-demo-02","Thuốc kê đơn · Mẫu B","Chỉ mô phỏng luồng toa Rx",0L,"Thuốc kê đơn",true,"Hộp");
        seed("beauty-01","Sữa rửa mặt dịu nhẹ","Mỹ phẩm chăm sóc da · 150 ml",118000L,"Mỹ phẩm",false,"Chai");
        seed("beauty-02","Kem dưỡng ẩm hằng ngày","Mỹ phẩm chăm sóc da · 50 g",169000L,"Mỹ phẩm",false,"Hũ");
        seed("beauty-03","Son dưỡng môi","Mỹ phẩm chăm sóc môi · 4 g",59000L,"Mỹ phẩm",false,"Thỏi");
        seed("beauty-04","Sữa tắm dịu nhẹ","Mỹ phẩm chăm sóc cơ thể · 400 ml",129000L,"Mỹ phẩm",false,"Chai");
        seed("beauty-05","Dầu gội chăm sóc tóc","Mỹ phẩm chăm sóc tóc · 300 ml",145000L,"Mỹ phẩm",false,"Chai");
        seed("beauty-06","Kem chống nắng mẫu","Mỹ phẩm chăm sóc da · 50 ml",189000L,"Mỹ phẩm",false,"Tuýp");
        seed("food-01","Yến mạch cán mỏng","Thực phẩm đóng gói · 500 g",79000L,"Thực phẩm",false,"Gói");
        seed("food-02","Ngũ cốc ăn sáng","Thực phẩm đóng gói · 350 g",68000L,"Thực phẩm",false,"Hộp");
        seed("food-03","Hạt điều rang","Thực phẩm đóng gói · 250 g",109000L,"Thực phẩm",false,"Hũ");
        seed("food-04","Bánh quy yến mạch","Thực phẩm đóng gói · 180 g",45000L,"Thực phẩm",false,"Hộp");
        seed("food-05","Trà thảo mộc dạng túi","Thực phẩm, đồ uống · 20 túi",66000L,"Thực phẩm",false,"Hộp");
        seed("food-06","Nước trái cây đóng chai","Thực phẩm, đồ uống · 300 ml",29000L,"Thực phẩm",false,"Chai");
        seed("supp-01","Thực phẩm bổ sung · Mẫu A","Mẫu thiết kế · không phải thuốc",179000L,"Thực phẩm bổ sung",false,"Hộp");
        seed("supp-02","Thực phẩm bổ sung · Mẫu B","Mẫu thiết kế · không thay thế thuốc",239000L,"Thực phẩm bổ sung",false,"Hộp");
        seed("care-01","Nước súc miệng","Chăm sóc cá nhân · 250 ml",64000L,"Chăm sóc",false,"Chai");
        seed("care-02","Bàn chải đánh răng mềm","Chăm sóc răng miệng · 1 chiếc",32000L,"Chăm sóc",false,"Cái");
        seed("care-03","Khăn giấy bỏ túi","Chăm sóc cá nhân · 10 gói",35000L,"Chăm sóc",false,"Bịch");
        seed("device-02","Máy đo huyết áp mẫu","Thiết bị chăm sóc gia đình",599000L,"Thiết bị",false,"Cái");
        seed("device-03","Hộp chia đồ dùng","Dụng cụ gia đình · 7 ngăn",79000L,"Thiết bị",false,"Cái");
        seed("supplies-02","Băng cá nhân","Vật tư y tế · hộp 20 miếng",27000L,"Vật tư y tế",false,"Hộp");
    }
    private void seed(String id,String name,String detail,long price,String cat,boolean rx,String unit) {
        String description;
        switch(id){
            case "mask-01": description="Thông tin sản phẩm minh họa. Vui lòng kiểm tra quy cách, giá và tình trạng hàng tại nhà thuốc.";break;
            case "saline-01": description="Sản phẩm được đưa vào dữ liệu mẫu chỉ để trình bày giao diện. Không thay thế hướng dẫn chuyên môn.";break;
            case "cotton-01": description="Dữ liệu minh họa cho luồng đặt trước và nhận tại quầy.";break;
            case "thermometer-01": description="Thông số, giá và điều kiện cung ứng sẽ được xác nhận tại quầy.";break;
            case "bandage-01": description="Thông tin giả lập nhằm kiểm tra giao diện xem sản phẩm.";break;
            case "rx-demo-01": description="Không phải thuốc được chào bán. Quy trình này yêu cầu gửi toa hợp lệ và được dược sĩ xem xét trước khi xác nhận.";break;
            default: description="Dữ liệu mẫu dành cho giao diện PharmaSIU. Quy cách, nguồn gốc, thành phần, giá và khả năng cung ứng phải được xác minh tại nơi bán. Sản phẩm không thay thế tư vấn của nhân viên chuyên môn.";
        }
        products.add(new Product(id,name,detail,price,cat,rx,unit,description));
        stock.put(id,10);
    }
    public Product product(String id) {
        for(Product p:products) if(p.id.equals(id)) return p;
        return null;
    }
    public boolean login(String identifier,String password) {
        if(repository!=null){
            PharmaRepository.AccountRow user=repository.signIn(identifier,password);
            if(user!=null){
                role=user.role;authenticatedAccountId=user.id;
                customerPhone=CUSTOMER.equals(user.role)?user.phone:"";
                favorites.clear();cart.clear();posCart.clear();
                if(CUSTOMER.equals(user.role))
                    customerAccounts.put(user.phone,new CustomerProfile(
                        user.name,user.phone,user.email,"",""));
                return true;
            }
        }
        if(identifier==null||password==null)return false;
        String email=identifier.trim().toLowerCase(Locale.ROOT);
        if((email.equals("quanly@pharmasiu.demo")||email.equals("quanly@pharmasiu.vn")||email.equals("0902222222"))
            &&(password.equals("2222")||password.equals("12345678"))) {
            role=ADMIN;customerPhone="";return true;
        }
        if((email.equals("nhanvien@pharmasiu.demo")||email.equals("nhanvien@pharmasiu.vn")||email.equals("0901111111"))
            &&(password.equals("1111")||password.equals("12345678"))) {
            role=CASHIER;customerPhone="";return true;
        }
        for(CustomerProfile customer:customerAccounts.values()){
            if((email.equals(customer.phone)||email.equals(customer.email.trim().toLowerCase(Locale.ROOT)))
                &&password.equals(customer.password)){
                role=CUSTOMER;customerPhone=customer.phone;favorites.clear();
                customerStore.restoreFavorites(customer.phone,favorites);
                return true;
            }
        }
        return false;
    }
    public void logout() {
        if(repository==null && CUSTOMER.equals(role))
            customerStore.rememberFavorites(customerPhone,favorites);
        authenticatedAccountId=-1L;
        favorites.clear();
        role=NONE;customerPhone="";cart.clear();posCart.clear();
        chosenPrescriptionName=null;chosenPrescriptionFingerprint=null;
        selectedProductId=""; selectedOrderId="";
    }
    public boolean canAdmin(){return ADMIN.equals(role);}
    public boolean canSell(){return ADMIN.equals(role)||CASHIER.equals(role);}
    public boolean canApproveRx(){return false;} // No real licensed pharmacist verification/API.
    public boolean addToCart(String productId) {
        if(!CUSTOMER.equals(role)||product(productId)==null)return false;
        int current=cart.getOrDefault(productId,0);
        if(current>=20)return false;
        cart.put(productId,current+1);invalidateRxSelectionIfNeeded();return true;
    }
    public void changeCartQty(String id,int qty) {
        if(!CUSTOMER.equals(role))return;
        if(qty<=0)cart.remove(id);
        else if(qty<=20 && product(id)!=null)cart.put(id,qty);
        invalidateRxSelectionIfNeeded();
    }
    private Map<String,String> snapshotProductNames(Map<String,Integer> selected) {
        Map<String,String> out=new LinkedHashMap<>();
        for(String id:selected.keySet()){
            Product p=product(id);
            out.put(id,p==null?"Sản phẩm không còn trong danh mục":p.name);
        }
        return out;
    }
    private Map<String,Long> snapshotProductPrices(Map<String,Integer> selected) {
        Map<String,Long> out=new LinkedHashMap<>();
        for(String id:selected.keySet()){
            Product p=product(id);
            out.put(id,p==null?0L:p.priceVnd);
        }
        return out;
    }
    public long knownSubtotal(Map<String,Integer> items) {
        long subtotal=0L;
        for(Map.Entry<String,Integer> e:items.entrySet()){
            Product p=product(e.getKey());
            if(p!=null&&p.priceVnd>0&&e.getValue()>0)subtotal+=p.priceVnd*e.getValue();
        }
        return subtotal;
    }
    public boolean hasRx(Map<String,Integer> items) {
        for(String id:items.keySet()){Product p=product(id);if(p!=null&&p.requiresPrescription)return true;}
        return false;
    }
    public boolean hasUnpriced(Map<String,Integer> items) {
        for(String id:items.keySet()){Product p=product(id);if(p==null||p.priceVnd<=0)return true;}
        return false;
    }
    public String rxFingerprint() {
        List<String> keys=new ArrayList<>();
        for(Map.Entry<String,Integer> e:cart.entrySet()){
            Product p=product(e.getKey());
            if(p!=null&&p.requiresPrescription)keys.add(e.getKey()+":"+e.getValue());
        }
        Collections.sort(keys);return String.join("|",keys);
    }
    private void invalidateRxSelectionIfNeeded() {
        if(chosenPrescriptionFingerprint!=null&&!chosenPrescriptionFingerprint.equals(rxFingerprint())) {
            chosenPrescriptionFingerprint=null; chosenPrescriptionName=null;
        }
    }
    public void attachLocalPrescription(String displayName) {
        chosenPrescriptionName=displayName;
        chosenPrescriptionFingerprint=rxFingerprint();
    }
    public boolean localPrescriptionSelected() {
        return chosenPrescriptionName!=null && !rxFingerprint().isEmpty()
            && rxFingerprint().equals(chosenPrescriptionFingerprint);
    }
    public boolean canSaveRequest(String name,String phone,String pickup) {
        if(!CUSTOMER.equals(role)||cart.isEmpty()||name==null||name.trim().length()<2||phone==null
           ||!phone.matches("0[0-9]{9,10}")||pickup==null||pickup.trim().isEmpty())return false;
        if(repository==null && !(pickup.equals("Điểm nhận mẫu A")||pickup.equals("Điểm nhận mẫu B")))return false;
        for(int qty:cart.values())if(qty<1||qty>20)return false;
        return !hasRx(cart)||localPrescriptionSelected();
    }
    public Order saveRequest(String name,String phone,String pickup) {
        if(!canSaveRequest(name,phone,pickup))return null;
        if(repository!=null){
            String[] ref=new String[1];
            Map<String,Integer> requested=new LinkedHashMap<>(cart);
            if(!repository.saveCustomerOrder(authenticatedAccountId,requested,hasRx(requested),pickup,ref))
                return null;
            Order o=new Order(ref[0],customerPhone,pickup,name,phone,
                requested,knownSubtotal(requested),hasUnpriced(requested),hasRx(requested),
                snapshotProductNames(requested),snapshotProductPrices(requested));
            o.status=o.containsRx?"Chờ gửi và xác minh toa":"Chờ xử lý";
            orders.add(0,o);selectedOrderId=o.reference;cart.clear();
            chosenPrescriptionName=null;chosenPrescriptionFingerprint=null;
            return o;
        }
        Order o=new Order(String.format(Locale.ROOT,"DEMO-%04d",++orderCounter),customerPhone,pickup,name,phone,
            cart,knownSubtotal(cart),hasUnpriced(cart),hasRx(cart),
            snapshotProductNames(cart),snapshotProductPrices(cart));
        orders.add(0,o); selectedOrderId=o.reference;cart.clear();
        chosenPrescriptionName=null; chosenPrescriptionFingerprint=null;return o;
    }
    public boolean cancelRequest(String id,String reason) {
        if(!CUSTOMER.equals(role)||reason==null||reason.trim().length()<3)return false;
        if(repository!=null){
            boolean ok=repository.cancelCustomerOrder(authenticatedAccountId,id,reason);
            if(ok)reloadCustomerOrders();
            return ok;
        }
        for(Order o:orders)if(o.reference.equals(id)&&o.customerPhone.equals(customerPhone)
          &&o.status.equals("Đã lưu yêu cầu DEMO")){
            o.status="Đã hủy DEMO";o.cancelReason=reason.trim().substring(0,Math.min(160,reason.trim().length()));return true;
        }
        return false;
    }
    public List<Order> customerOrders(){
        if(repository!=null)reloadCustomerOrders();
        List<Order> list=new ArrayList<>();
        for(Order o:orders)if(customerPhone.equals(o.customerPhone))list.add(o);
        return list;
    }
    private void reloadCustomerOrders(){
        if(repository==null||!CUSTOMER.equals(role))return;
        orders.clear();
        CustomerProfile profile=currentCustomer();
        String name=profile==null?"":profile.displayName;
        for(PharmaRepository.OrderRow row:repository.loadCustomerOrders(authenticatedAccountId)){
            Map<String,Integer> lines=new LinkedHashMap<>();
            Map<String,String> names=new LinkedHashMap<>();
            Map<String,Long> prices=new LinkedHashMap<>();
            for(PharmaRepository.LineRow line:row.lines){
                lines.merge(line.productCode,line.quantity,Integer::sum);
                names.put(line.productCode,line.productName);
                prices.put(line.productCode,line.price);
            }
            Order order=new Order(row.reference,customerPhone,"Nhận tại quầy",
                name,customerPhone,lines,row.total,false,row.rx,names,prices);
            switch(row.status){
                case "DA_HUY":order.status="Đã hủy";break;
                case "CHO_DUYET_TOA":order.status="Chờ xác minh toa";break;
                case "SAN_SANG_NHAN":order.status="Sẵn sàng nhận";break;
                case "HOAN_TAT":order.status="Hoàn tất";break;
                default:order.status="Đang xử lý";
            }
            orders.add(order);
        }
    }
    public boolean addPos(String id) {
        Product p=product(id);
        if(!canSell()||!shiftOpen||p==null||p.requiresPrescription||p.priceVnd<=0)return false;
        int qty=posCart.getOrDefault(id,0);
        if(qty>=30||qty>=stock.getOrDefault(id,0))return false;
        posCart.put(id,qty+1);return true;
    }
    public void changePosQty(String id,int qty) {
        if(!canSell())return;
        if(qty<=0){posCart.remove(id);return;}
        if(qty<=30 && qty<=stock.getOrDefault(id,0) && product(id)!=null &&
          !product(id).requiresPrescription && product(id).priceVnd>0)posCart.put(id,qty);
    }
    public boolean openShift(long openingCash){
        if(!canSell()||shiftOpen||openingCash<0)return false;
        shiftOpen=true;shiftNumber++;openingCashVnd=openingCash;cashCollectedVnd=0;
        posCart.clear();return true;
    }
    public long posTotal(){return knownSubtotal(posCart);}
    public Sale savePosSale(String method,long amountTendered) {
        if(repository!=null){
            if(!canSell()||!shiftOpen||posCart.isEmpty())return null;
            Map<String,Integer> submitted=new LinkedHashMap<>(posCart);
            long total=posTotal();
            String ref=repository.recordPosSale(authenticatedAccountId,submitted,
                method,amountTendered,shiftNumber);
            if(ref==null)return null;
            List<com.pharmasiu.mobile.domain.CounterLine> snapshots=new ArrayList<>();
            for(Map.Entry<String,Integer> item:submitted.entrySet()){
                Product p=product(item.getKey());
                if(p!=null)snapshots.add(new com.pharmasiu.mobile.domain.CounterLine(
                    p.id,p.name,p.unit,p.priceVnd,item.getValue(),false));
            }
            Sale sale=new Sale(ref,method,total,submitted,shiftNumber,snapshots,
                ADMIN.equals(role),amountTendered);
            sales.add(0,sale);
            if("CASH".equals(method))cashCollectedVnd+=total;
            posCart.clear();refreshDatabaseViews();return sale;
        }
        if(!canSell()||!shiftOpen||posCart.isEmpty()||
            !("CASH".equals(method)||"OTHER_DEMO".equals(method)))return null;
        long total=posTotal();
        if(total<=0||("CASH".equals(method)&&amountTendered<total))return null;
        for(Map.Entry<String,Integer> e:posCart.entrySet()){
            Product p=product(e.getKey());
            if(p==null||p.requiresPrescription||p.priceVnd<=0||
              e.getValue()>stock.getOrDefault(e.getKey(),0))return null;
            boolean hasDatedLots=false;
            for(Lot lot:lots)if(lot.productId.equals(e.getKey()))hasDatedLots=true;
            if(hasDatedLots && com.pharmasiu.mobile.domain.JavaRules.allocateFefo(
                lots,e.getKey(),e.getValue(),LocalDate.now())==null)return null;
        }
        java.util.List<com.pharmasiu.mobile.domain.CounterLine> snapshots=new ArrayList<>();
        for(Map.Entry<String,Integer> selected:posCart.entrySet()){
            Product product=product(selected.getKey());
            snapshots.add(new com.pharmasiu.mobile.domain.CounterLine(
                product.id,product.name,product.unit,product.priceVnd,selected.getValue(),false));
        }
        Sale sale=new Sale(String.format(Locale.ROOT,"POS-DEMO-%04d",++saleCounter),
             method,total,posCart,shiftNumber,snapshots,ADMIN.equals(role),amountTendered);
        for(Map.Entry<String,Integer> e:posCart.entrySet()){
            boolean hasDatedLots=false;
            for(Lot lot:lots)if(lot.productId.equals(e.getKey()))hasDatedLots=true;
            if(hasDatedLots){
                List<com.pharmasiu.mobile.domain.JavaRules.Allocation> allocations =
                    com.pharmasiu.mobile.domain.JavaRules.allocateFefo(
                        lots,e.getKey(),e.getValue(),LocalDate.now());
                if(allocations!=null)for(com.pharmasiu.mobile.domain.JavaRules.Allocation allocation:allocations)
                    for(Lot lot:lots)if(lot.productId.equals(e.getKey()) &&
                        lot.code.equals(allocation.code))lot.quantity-=allocation.quantity;
            }
            stock.put(e.getKey(),stock.get(e.getKey())-e.getValue());
        }
        sales.add(0,sale);
        if("CASH".equals(method))cashCollectedVnd+=total;
        posCart.clear();return sale;
    }
    public boolean closeShift(){
        if(!canSell()||!shiftOpen||!posCart.isEmpty())return false;
        shiftOpen=false;return true;
    }
    public boolean renameProduct(String id,String name,long price){
        return inventoryStore.renameProduct(id,name,price);
    }
    public boolean receiveLot(String productId,String code,int qty,LocalDate expires){
        return inventoryStore.receiveLot(productId,code,qty,expires);
    }
    public boolean quarantineLot(String code){return inventoryStore.quarantineLot(code);}
    public boolean receiveQuarantinedLot(String productId,String code,int qty,LocalDate expires){
        return inventoryStore.receiveQuarantinedLot(productId,code,qty,expires);
    }
    public boolean setLotQuarantined(String code,boolean quarantined){
        return inventoryStore.setLotQuarantined(code,quarantined);
    }
    public int eligibleLotQty(String productId,LocalDate today){
        return inventoryStore.eligibleLotQty(productId,today);
    }
    public boolean countDemoStock(String productId,int counted,String reason){
        return inventoryStore.countDemoStock(productId,counted,reason);
    }
    public boolean reconcileDemoShift(long counted,String reason){
        return inventoryStore.reconcileDemoShift(counted,reason);
    }

public CustomerProfile currentCustomer(){
        return CUSTOMER.equals(role)?customerAccounts.get(customerPhone):null;
    }
    public boolean registerCustomer(String name,String phone,String email,String password){
        return repository!=null?
            repository.registerCustomer(name,phone,email,password):
            customerStore.register(this,name,phone,email,password);
    }
    public boolean updateCustomer(String name,String email,String birthday){
        if(repository!=null){
            if(birthday!=null&&!birthday.trim().isEmpty())return false; // No birth-date column in 6-table schema.
            boolean ok=repository.updateCustomer(authenticatedAccountId,name,email);
            if(ok&&currentCustomer()!=null){
                currentCustomer().displayName=name.trim();currentCustomer().email=email==null?"":email.trim();
            }
            return ok;
        }
        return customerStore.update(this,name,email,birthday);
    }
    public boolean changePassword(String oldPassword,String newPassword){
        return repository!=null?
            repository.changePassword(authenticatedAccountId,oldPassword,newPassword):
            customerStore.changePassword(this,oldPassword,newPassword);
    }
    public List<Address> myAddresses(){
        return repository!=null?Collections.emptyList():customerStore.addresses(this);
    }
    public Address addressById(String id){
        return repository!=null?null:customerStore.addressById(this,id);
    }
    public boolean saveAddress(String id,String label,String receiver,String phone,String detail,
                               boolean makeDefault){
        return repository==null&&customerStore.saveAddress(this,id,label,receiver,phone,detail,makeDefault);
    }
    public boolean deleteAddress(String id){
        return repository==null&&customerStore.deleteAddress(this,id);
    }
    public void toggleFavorite(String id){
        if(repository==null)customerStore.toggleFavorite(this,id);
        else lastDemoMessage="Tính năng yêu thích chưa có bảng lưu trữ trong CSDL 6 bảng.";
    }
    public boolean reorderRequest(String reference){
        if(!CUSTOMER.equals(role))return false;
        for(Order order:customerOrders())if(order.reference.equals(reference)){
            for(Map.Entry<String,Integer> item:order.lines.entrySet()){
                if(product(item.getKey())==null)continue;
                int requested=cart.getOrDefault(item.getKey(),0)+item.getValue();
                cart.put(item.getKey(),Math.min(20,requested));
            }
            chosenPrescriptionName=null;
            chosenPrescriptionFingerprint=null;
            return !cart.isEmpty();
        }
        return false;
    }
    public void removeLocalPrescription(){
        chosenPrescriptionName=null;
        chosenPrescriptionFingerprint=null;
    }

    public boolean changeCustomerPhone(String nextPhone){
        if(repository!=null){
            boolean changed=repository.changePhone(authenticatedAccountId,nextPhone);
            if(changed){
                CustomerProfile current=customerAccounts.remove(customerPhone);
                if(current!=null){current.phone=nextPhone;customerAccounts.put(nextPhone,current);}
                customerPhone=nextPhone;
            }
            return changed;
        }
        return customerStore.changePhone(this,nextPhone);
    }
    /** Local demo recovery only: no SMS/email verification or identity assurance. */
    public boolean resetCustomerPasswordDemo(String phone,String nextPassword){
        return repository==null&&customerStore.resetPassword(this,phone,nextPassword);
    }
    /** UI-only local content image. Only an ADMIN can assign/remove a photo. */
    public boolean changePhoto(String productId,String contentUri){
        if(repository!=null){lastDemoMessage="Ảnh hàng hóa chưa có cột trong CSDL.";return false;}
        if(!canAdmin()||product(productId)==null)return false;
        if(contentUri!=null&&!contentUri.startsWith("content://"))return false;
        if(contentUri==null)staffPhotoUris.remove(productId);
        else staffPhotoUris.put(productId,contentUri);
        return true;
    }
}
