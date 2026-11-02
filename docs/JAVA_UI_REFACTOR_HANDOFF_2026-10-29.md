# PharmaSIU — Bàn giao tái cấu trúc Java/XML & Material 3 (29/10/2026)

**Dự án gốc:** `C:\PharmaSIU\PharmaSIU-1For4`  
**Duy nhất:** Gradle `:app`, package/appId `com.pharmasiu.mobile`, Java 17, XML.  
**Trạng thái:** code trực tiếp ở working tree `main`, **chưa commit/push**.

## 1. Phần thực hiện

1. **Phân vùng source:** thay 6 lớp UI lớn (~350–720 dòng trước đây) thành các màn và controller theo package. Public facades trong `ui/customer`, `ui/staff` vẫn giữ chữ ký để router và test cũ hoạt động.
2. **Customer:** `ui/customer/catalog` gồm home, browse, product, search/filter/artwork; `ui/customer/orders` gồm giỏ, toa cục bộ, checkout, history, receipt, common components; `ui/customer/account` gồm account home, profile/password, address book, favorites/onboarding, UI base.
3. **POS:** `ui/staff/pos` tách binding/ca, catalog FEFO, cart/payment DEMO và history/receipt.
4. **Admin:** `ui/staff/admin` tách Dashboard, CatalogEditor, Lots, Audit, Requests, Reports, Media, UI primitives. Nhận lô/quarantine và đối soát giữ guard trong RAM store.
5. **Auth và ảnh:** `ui/auth` chia form binding, state presentation, event/controller, mode; `ui/staff/media` chia store URI, thumbnail/preview và decoder/placeholder.
6. **State:** `core/state/CustomerAccountStore.java` giữ hồ sơ, địa chỉ, favorites, mật khẩu RAM; `core/state/InventoryStore.java` giữ stock/lot/audit/reconciliation. `DemoState` giữ các phương thức public delegating để tránh thay caller.
7. **Design:** thêm `ui/design/PharmaUi.java` dùng MaterialCardView, MaterialButton, TextInputLayout, warning/empty/typography; `navigation/PharmaBottomBar.java` điều hướng theo vai trò và trạng thái chọn. Thay 26 nút XML cũ bằng MaterialButton, thêm bộ token và icon vector nav. Chủ đạo emerald/forest/lime/mint, thẻ bo tròn, bố cục rõ cấp bậc.
8. **Dọn trùng:** loại khỏi active source `CustomerScreens`, `AdminScreens`, `CashierScreens`, `UiKit` cũ và bốn layout thử nghiệm không còn sử dụng; đã sao lưu bên ngoài repo.

## 2. Sơ đồ mã nguồn thực tế

```text
app/src/main/java/com/pharmasiu/mobile/
├── MainActivity.java                       # lifecycle, picker, state, route delegation
├── navigation/
│   └── PharmaBottomBar.java                # nav role-aware / Material 3
├── core/
│   ├── DemoState.java                      # RAM façade & legacy contract
│   ├── UiHost.java
│   └── state/
│       ├── CustomerAccountStore.java
│       └── InventoryStore.java
├── data/
│   ├── model/                              # immutable/value models
│   └── repository/                         # data mẫu tại thiết bị
├── domain/                                 # thuần Java: FEFO, POS, Rx, role, trạng thái
├── legacy/                                 # đối chiếu hợp đồng đang được unit test
└── ui/
    ├── AuthScreen.java                     # public façade
    ├── auth/                               # FormBase / UiState / Controller / Mode
    ├── design/PharmaUi.java                # dùng chung cả vai trò
    ├── customer/
    │   ├── CustomerCatalogViews.java       # façade
    │   ├── CustomerOrderViews.java         # façade, giữ API test
    │   ├── CustomerAccountViews.java       # façade
    │   ├── catalog/                        # Home, Browse, Product, Artwork, Search
    │   ├── orders/                         # Cart, Rx, Checkout, History, Receipt
    │   └── account/                        # Profile, Addresses, Favorites, Onboarding
    └── staff/
        ├── StaffPosViews.java              # façade
        ├── StaffAdminViews.java            # façade
        ├── StaffMediaViews.java            # façade
        ├── pos/                            # Presenter, shift, cart, catalog, receipt
        ├── admin/                          # Dashboard, Catalog, Lots, Audit, Reports
        └── media/                          # PhotoStore, Preview, Decoder
```

## 3. Phân quyền làm việc nhóm

| Thành viên/phân hệ | Sở hữu Java | Sở hữu XML chính | Tránh sửa |
|---|---|---|---|
| Nền tảng | `MainActivity`, `navigation` | `activity_main.xml` | Domain rules trừ khi có review |
| Customer Catalog | `ui/customer/catalog` | `catalog_v2_*` | Orders/Account/Staff |
| Customer Orders | `ui/customer/orders` | `order_v2_*` | POS/StaffState |
| Customer Account | `ui/customer/account` | `customer_account_*` | Auth và Staff |
| POS | `ui/staff/pos` | `staff_pos_*` | Admin, data tài khoản |
| Admin | `ui/staff/admin` | `staff_admin_*` | POS, Customer |
| Media | `ui/staff/media` | `staff_media_*` | Thay đổi API picker không thông qua host |
| Auth | `ui/auth` | `auth_unified.xml` | Tự cấp role/real auth |
| Data/Domain | `domain`, `data`, `core/state` | Không sở hữu UI | Không thêm Android Context vào domain |
| Design | `ui/design`, `res/values/ui_tokens.xml`, `res/drawable/nav_*` | Component/theme | Không tự sửa nghiệp vụ |

**Quy tắc:** mỗi PR một phần, không sửa `DemoState` hoặc `MainActivity` nếu không phải integration owner; giữ chữ ký façade; pull/rebase trước khi merge; không tạo app/module bên cạnh.

## 4. Thư viện ngoài đã tích hợp

`com.google.android.material:material:1.14.0` — Material Components for Android (Google, Apache-2.0), dùng được trong Java + Android XML, pin phiên bản và đã được Gradle resolve. Link chính thức: https://github.com/material-components/material-components-android

Material Android Views hiện ở chế độ bảo trì, nhưng vẫn phù hợp với ràng buộc Java/XML. Không dùng Compose, thư viện mạng ảnh hoặc lấy code repo ngoài không kiểm duyệt.

## 5. Kiểm chứng thực tế

- `:app:assembleDebug :app:testDebugUnitTest --offline --no-daemon` → **BUILD SUCCESSFUL**.
- JUnit XML: **141/141 test, 29 suites**, 0 fail, 0 error, 0 skipped.
- Source hiện hành: **87 Java chính**, **29 Java test**, **59 XML resources**, **0 `.kt`/`.kts`**.
- Emulator: APK sau refactor được cài, login và dashboard/route Customer Home/Account/Cart, Cashier POS, Admin Dashboard hoạt động; sau tách Auth/Media cuối đã cài lại và mở Auth, Admin, POS thành công. Không thấy FATAL/InflateException trong các log đã xem.
- Build hiện còn cảnh báo deprecated API / Gradle 9 compatibility; **không phải báo cáo không-warning**.

## 6. Phạm vi chưa đạt hoặc chưa kiểm chứng

- **Chưa xác nhận parity 1:1** với UI cũ trên mọi màn, ảnh, animation và font scale.
- UI vẫn có một phần layout XML cũ và Java Canvas, nên không có cơ sở nói *mọi* màn đã được thiết kế mới hoàn toàn.
- Chưa chạy đầy đủ accessibility/TalkBack, font scale 1.5×, 320dp, 412dp, Android 14/15, benchmark và E2E mọi chức năng.
- Các yêu cầu Rx, tồn, phiếu, thu tiền, kiểm kê chỉ DEMO RAM: **không có** duyệt dược sĩ thật, giao/giữ hàng, thanh toán, backend hoặc database.
- Subagent đã được giao ownership song song nhưng một số phiên khởi tạo lỗi, **không tính công việc chưa thực hiện**; prime đã chủ động làm và test mã tại root.
- Các lớp façade `DemoState`, `LegacyDomainRules` vẫn tương đối dài vì giữ hợp đồng/kiểm thử đã có; refactor sâu tiếp cần test riêng, không nên gộp với đợt thay UI.

## 7. Build và khôi phục

Android Studio → Open **root** `C:\PharmaSIU\PharmaSIU-1For4`, chọn `app`.

```powershell
.\gradlew.bat :app:clean :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Sao lưu snapshot source/Gradle ở `C:\UI UX Brain\pharmasiu_refactor_backup_2026-10-29` **ngoài repository**; chỉ dùng nếu cần kiểm tra/rollback. Không có commit/push, không đổi database.
