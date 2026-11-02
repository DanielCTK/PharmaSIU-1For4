# PharmaSIU — Quy tắc chuyển đổi sang Java + Android Views/XML

## Quyết định kỹ thuật
- Ứng dụng hiện tại có 50 tệp Kotlin nguồn chính, màn hình hoàn toàn dùng Jetpack Compose. **Không thể đổi đuôi tệp**; Java phải dùng Activity/Views/XML, xử lý sự kiện bằng View.OnClickListener, TextWatcher khi cần, Android layout và Android lifecycle.
- Tạo module app-java độc lập song song với :app để bảo vệ các chỉnh sửa chưa commit. Module Java phải có thể build riêng bằng :app-java:assembleDebug. Chỉ thay launcher chính sau khi parity 17 chức năng qua nghiệm thu.
- Lấy giao diện Java Views/XML với LinearLayout/FrameLayout/ScrollView/EditText/Button/Recycler equivalent (ListView hoặc vertical dynamic), action/menu tương đương theo Android API; không gắn Compose trong Java.
- Giữ tên, vai trò và ngữ nghĩa 17 mục BFD; 7 nhóm trạng thái; các quy tắc Rx, lô, FEFO demo, checkout, POS.
- Tuyệt đối không có SQLiteOpenHelper/SQLiteDatabase, SQL/DAO, backend, DB/schema/migration trong giai đoạn này. Phần SQLite chỉ là kiến thức kỹ thuật, không cho phép triển khai vì người dùng đã loại trừ DB.
- Không bao giờ tự nói upload toa, duyệt Rx, giữ hàng, thanh toán hoặc xuất kho thật. ADMIN không có quyền dược sĩ; cashier không chỉnh danh mục. Các thao tác dùng RAM demo phải được dán nhãn.
- Giữ giao diện emerald #064D3A, forest #073D30, lime #B9F48B và cream #F6F7F2.
- Không thay đổi :app, các tệp .kt hay gradle.properties (có chỉnh sửa người dùng từ trước); không commit/push.

## Quy ước cho các subagent
Java package gốc com.pharmasiu.javaapp.
- Core: model/DemoState.java; domain/JavaRules.java; tests domain.
- Customer: ui/CustomerScreens.java và các layout customer_*.xml.
- POS: ui/CashierScreens.java và các layout pos_*.xml.
- Admin: ui/AdminScreens.java và các layout admin_*.xml.
- Auth: ui/AuthScreen.java và các layout login_*.xml.
- Prime: MainActivity.java, UiHost.java, module Gradle/manifest/theme/integration/build.
Sử dụng chung giao diện UiHost và DemoState, không tự sửa source của nhóm khác. Route chuẩn login; home/catalog/product/cart/prescription/checkout/orders/order-detail/account/favorites; pos; admin/catalog-admin/lots/audit/reports.
MainActivity là bộ điều phối route, nhãn phiên, logout và màn hình.

## Nghiệm thu
1. :app-java:assembleDebug và :app-java:testDebugUnitTest phải PASS, Java sources không cần Kotlin plugin.
2. So khớp chức năng theo màn và quyền; khi chưa chuyển xong, phải ghi rõ **CHƯA ĐẠT PARITY**.
3. :app gốc Compose vẫn build; không được tuyên bố 100% chuyển Java trước khi các màn/trạng thái/chức năng đều có ở module mới.
4. Kiểm tra runtime API 28/36, đăng nhập 3 vai trò, POS/Rx, giá 0 Rx, đơn DEMO, quản lý lô, hủy, logout.
