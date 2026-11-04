# Kiến trúc PharmaSIU Android Java/XML

## Một ứng dụng duy nhất

- Root project: `settings.gradle` (Groovy), `include ':app'` duy nhất.
- Module: `app/build.gradle`, Android Gradle Plugin 8.13.2; `namespace` và `applicationId` cùng là `com.pharmasiu.mobile`.
- Main entry: `app/src/main/java/com/pharmasiu/mobile/MainActivity.java`, Android native `Activity`; `AndroidManifest.xml` chỉ khai báo launcher này.
- Tất cả mã nguồn trong ứng dụng là Java 17; các layout/theme/drawable ở `app/src/main/res/*.xml`. Không sử dụng Kotlin, Compose, Kotlin DSL hoặc module Java thử nghiệm.

## Các lớp chính

| Package | Trách nhiệm |
|---|---|
| `com.pharmasiu.mobile.data.model` | Model Java dùng chung hiện hành (`Medicine`, `CartLine`) |
| `com.pharmasiu.mobile.data.local` | SQLite local database, repository và các query account/catalog/order |
| `com.pharmasiu.mobile.domain` | Luật Java thuần như `CartRules`, `CatalogSearchRules`, `CounterRules`, `OrderStateMachine` và trạng thái đơn |
| `com.pharmasiu.mobile.core.DemoState` | Façade/UI-state adapter cho 3 vai trò; production mode nối tới `PharmaRepository` SQLite |
| `com.pharmasiu.mobile.core.UiHost` | Giao tiếp navigation, message, chọn tệp và logout |
| `com.pharmasiu.mobile.ui` | Màn đăng nhập, UI foundation, tuyến chuyển đổi UI |
| `com.pharmasiu.mobile.ui.customer` | Home/Catalog/Product, Cart/Rx/Checkout/Orders, Account/Profile/Address/Onboarding |
| `com.pharmasiu.mobile.ui.staff` | POS, lot/expiry/FEFO, inventory, shift reconciliation, media preview, admin |
| `src/test/.../legacy` | Hợp đồng tương thích chỉ dùng cho JVM test; không đóng gói vào APK |

## Đảm bảo trạng thái

Dữ liệu production-demo của tài khoản, catalog, lô/tồn và đơn hàng đi qua `PharmaRepository` trên SQLite nội bộ sáu bảng. `DemoState` vẫn là façade/UI-state adapter và còn giữ một số state phiên trong RAM; `SharedPreferences` chỉ giữ cờ onboarding. Chưa có backend/server hoặc đồng bộ đa thiết bị. Khi Activity đổi cấu hình, session hiện tại được giữ bằng retained non-configuration state; khi process restart vẫn về login.

Quyền được kiểm tra ở logic state: CUSTOMER không thể ghi staff data; CASHIER không thể chỉnh danh mục/ảnh/kiểm kê; ADMIN không được làm dược sĩ, không có trạng thái duyệt toa thực. Các bản ghi Rx chỉ là yêu cầu cục bộ và chưa upload.

Bán POS mô phỏng chặn Rx, mức tồn/lô không hợp lệ; ghi bản chụp bất biến tên/giá từng dòng, phiếu/giờ/ca/method/tiền nhận/tính thối DEMO; đối soát ca chỉ sau đóng ca, mỗi ca một lần. Ảnh sản phẩm là URI `content://` cục bộ với fallback Canvas minh họa; không khẳng định nguồn ảnh hay tồn thật.

## Test và giới hạn

JUnit Java trong `app/src/test/java`; chạy `gradlew.bat :app:clean :app:assembleDebug :app:testDebugUnitTest`. Kiểm thử backend thật, xét duyệt Rx, stock hold TTL, thanh toán thực, TalkBack và toàn bộ các kịch bản trên thiết bị thật chưa được hoàn tất. Không đánh đồng build thành công với parity từng pixel/animation.

Đối với layout, mở `app/src/main/res/layout/*.xml` bằng Android Studio Design/Split. Các ảnh minh họa Java Canvas không thể xem đầy đủ như layout thuần XML; phải chạy app/emulator để kiểm tra trực quan.

## As-built sau modular refactor (29/10/2026)

Cấu trúc thực tế đã được chia nhỏ hơn bảng khái quát ban đầu:

```text
com.pharmasiu.mobile/
  MainActivity.java
  navigation/PharmaBottomBar.java
  core/DemoState.java
  core/state/{CustomerAccountStore,InventoryStore}.java
  data/{model,local}/
  domain/
  ui/AuthScreen.java                    # façade
  ui/auth/{AuthFormBase,AuthUiState,AuthFormController,AuthMode}.java
  ui/design/PharmaUi.java               # Material components/typography/tokens
  ui/customer/{CustomerCatalogViews,CustomerOrderViews,CustomerAccountViews}.java
  ui/customer/catalog/                  # home, browse, product, artwork, filters
  ui/customer/orders/                   # cart, Rx, checkout, orders, receipts
  ui/customer/account/                  # profile, address, favorites, onboarding
  ui/staff/{StaffPosViews,StaffAdminViews,StaffMediaViews}.java
  ui/staff/pos/                          # shift/controller, catalog, cart, receipt
  ui/staff/admin/                        # dashboard, catalog, lots, audit, reports
  ui/staff/media/                        # URI store, preview, bounded local decoder
```

Public façade giữ chữ ký `create(Activity, UiHost, String)` cho những luồng tương ứng. `DemoState` giữ phương thức public đã có nhưng account và inventory được ủy quyền xuống `core/state`. `MainActivity` giữ lifecycle + picker, `PharmaBottomBar` quyết định cách hiển thị thanh nav phù hợp vai trò; không được dùng UI để cấp quyền thật.

Mã XML trải qua theme Material 3 `Theme.PharmaSIUJava`, token ở `ui_tokens.xml`, dùng MaterialButton/Card/TextField/BottomNavigationView qua `com.google.android.material:material:1.14.0`, không phải Compose. Các XML như `staff_pos_*`, `order_v2_*`, `catalog_v2_*` là màn hoạt động. Những layout thử nghiệm `customer_page`, `admin_page`, `pos_page`, `login_page` đã được dọn khỏi source active.

**Tình trạng source hiện hành:** 72 file Java production, 39 file Java trong cây test, 72 resource XML, 0 Kotlin. Kết quả build/test phải lấy từ lần CI/local verification gần nhất thay vì giữ một con số PASS dễ lỗi thời trong tài liệu. Emulator/device QA và phần lớn kịch bản kinh doanh sâu/E2E, accessibility vẫn cần bằng chứng riêng. Bản nghiệm thu chi tiết: `docs/JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md`.
