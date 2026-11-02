> **CẤU TRÚC HIỆN HÀNH (30/10/2026):** [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md). Tài liệu lịch sử trong docs/ không thay thế bản mô tả này.

# PharmaSIU Mobile — Android Java & XML

**Ứng dụng gốc duy nhất**: package, namespace và application ID là `com.pharmasiu.mobile`. Dự án Android dùng **Java 17 + Android Views/XML**. Không có Kotlin, Jetpack Compose, hoặc các module chạy song song. Gradle dùng Groovy DSL; `settings.gradle` chỉ khai báo `:app`.

PharmaSIU hiện là ứng dụng **RAM DEMO** dành cho Khách hàng, Nhân viên bán hàng và Quản trị viên. Các thao tác tra cứu, tạo yêu cầu, đơn hàng, giá, tồn, lô/hạn, FEFO, POS, kiểm kê và đối soát đều chỉ mô phỏng tại thiết bị. Chưa có cơ sở dữ liệu/backend vận hành, xác minh giá/tồn thật, dược sĩ xét duyệt Rx, upload toa, giữ hàng, thanh toán hoặc hóa đơn thật.

## Mở dự án trong Android Studio

1. Mở **nguyên thư mục gốc** `C:\PharmaSIU\PharmaSIU-1For4` (có `settings.gradle`) qua **Android Studio → File → Open**.
2. Dùng Android SDK API 36 và JDK 17+ (JBR Android Studio).
3. Đợi Gradle Sync; chọn cấu hình duy nhất **app**; chọn thiết bị Android 8.0+ rồi nhấn **Run**.
4. Ứng dụng mở màn đăng nhập chung. Không có màn chọn vai trò; vai trò xác định từ tài khoản DEMO.

Chạy kiểm thử trên Windows:

```powershell
.\gradlew.bat :app:clean :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. App ID vẫn là `com.pharmasiu.mobile`, nên đây là bản cập nhật cho app gốc, không phải ứng dụng Java thứ hai.

## Tài khoản mẫu

| Vai trò | Tài khoản | Mật khẩu |
| --- | --- | --- |
| Khách hàng | `0900000000` | `demo1234` |
| Nhân viên bán hàng | `nhanvien@pharmasiu.demo` | `1111` |
| Quản trị viên | `quanly@pharmasiu.demo` | `2222` |

Các tài khoản trên chỉ dùng thử giao diện; không nhập dữ liệu định danh/toa thuốc/mật khẩu thật.

## Chức năng Java/XML

**Khách hàng:** onboarding, trang chủ và catalog 30 sản phẩm mẫu, tìm kiếm không dấu/lọc/sắp xếp, chi tiết mặt hàng, giỏ, chọn tệp toa cục bộ (ảnh/PDF), nhập thông tin yêu cầu và điểm nhận, lưu yêu cầu RAM, phiếu, lịch sử, chi tiết, hủy và đặt lại, hồ sơ, địa chỉ, mật khẩu và yêu thích. Mọi mục liên quan Rx phải giữ trạng thái **chờ dược sĩ/CHỜ TÍCH HỢP**.

**Nhân viên bán hàng:** xem/tìm kiếm sản phẩm, xem ảnh minh họa, mở/đóng ca, ghi nhận giỏ POS DEMO, chặn bán Rx và lô không hợp lệ, phương thức tiền mặt/khác **DEMO**, phiếu và lịch sử mô phỏng. Không chấp nhận đây là thu tiền thật.

**Quản trị viên:** danh mục (tên/giá/tạo mặt hàng demo), chọn/xem/đổi ảnh cục bộ, nhận lô và hạn dùng, cách ly/khôi phục, kiểm kê chênh lệch, đối soát sau khi đóng ca và báo cáo RAM; quyền kiểm soát được kiểm tra ở domain/store. Không thay thế phần mềm quản lý tồn/giấy phép chuyên môn thực tế.

## Cấu trúc mã nguồn

```text
PharmaSIU-1For4/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/pharmasiu/mobile/
│       │   │   ├── MainActivity.java
│       │   │   ├── core/            # RAM state, role, Rx, POS, tài khoản, ca
│       │   │   ├── data/            # Model / catalog repository Java
│       │   │   ├── domain/          # Logic nghiệp vụ Java, FEFO, trạng thái
│       │   │   ├── legacy/          # Các hợp đồng Java còn đối chiếu
│       │   │   └── ui/              # Auth, khách hàng, POS, quản trị, Media
│       │   └── res/
│       │       ├── layout/          # Toàn bộ XML màn hình
│       │       ├── drawable/        # UI assets/drawables XML
│       │       └── values/          # Emerald/Lime palette, style, strings
│       └── test/java/com/pharmasiu/mobile/  # JUnit Java
├── build.gradle
├── settings.gradle                 # include ':app'
└── gradle/
```

Các màu sắc Emerald/Forest/Lime và quy chuẩn chính của thiết kế cũ đã được chuyển thành Android XML resources và Java Views/Canvas. Để xem bố cục không cần chạy app, mở các `app/src/main/res/layout/*.xml` trong **Layout Editor (Design/Split)** của Android Studio; Compose Preview không còn tồn tại.

## Trạng thái chuyển đổi

- **Đã gỡ** toàn bộ 50 tệp Kotlin ứng dụng và 16 tệp Kotlin test khỏi project, sau khi tạo backup ngoài repository và chuyển đổi domain/tests sang Java.
- **Đã bỏ** các thư mục module thử nghiệm `app-java`, `java-only`; chỉ còn `:app`.
- Đã đổi toàn bộ tên package Java về `com.pharmasiu.mobile`.
- Build/debug và kiểm thử Java thực hiện trực tiếp trên `:app`.
- **Chưa tuyên bố parity 1:1** cho từng animation/pixel/Compose Preview, mọi kích thước màn hình và toàn bộ E2E khi backend chưa tồn tại. Xem báo cáo nghiệm thu hiện hành trong `docs/`.

Không đưa vào Git `local.properties`, file build, signing keys, ảnh toa hoặc dữ liệu người thật.

## Cấu trúc mới và quy tắc làm nhóm (29/10/2026)

**Từ bản nâng cấp này, Java được chia theo feature thay vì dồn ở 6 class lớn.** Các lớp `AuthScreen`, `CustomerCatalogViews`, `CustomerOrderViews`, `CustomerAccountViews`, `StaffPosViews`, `StaffAdminViews`, `StaffMediaViews` là điểm vào ổn định, còn chi tiết ở các package dưới đây:

| Khu vực | Package |
|---|---|
| Customer | `ui/customer/catalog`, `ui/customer/orders`, `ui/customer/account` |
| Nhân viên POS | `ui/staff/pos` |
| Quản trị | `ui/staff/admin` |
| Ảnh hàng hóa | `ui/staff/media` |
| Đăng nhập | `ui/auth` |
| Trạng thái RAM | `core/state` |
| UI dùng chung | `ui/design/PharmaUi` |
| Điều hướng | `navigation/PharmaBottomBar` |

**UI:** có Google Material Components for Android `1.14.0`, Material 3 theme, buttons/cards/textfields, bottom navigation có biểu tượng, tokens spacing/radius và trạng thái rõ ràng. Tất cả vẫn là **Java/XML**; không đưa Compose/Kotlin vào lại.

**Điểm bắt đầu cho thành viên:** đọc `docs/JAVA_UI_ARCHITECTURE_2026-10-29.md` và `docs/JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md`, chọn đúng một package và những XML liên quan, tránh đổi chữ ký façade; chạy `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` trước khi gửi PR.

Trạng thái tích hợp: 87 Java production, 29 Java JUnit tests, 59 resource XML, 0 Kotlin; 141/141 test PASS. UI chưa được xác nhận parity pixel trên mọi thiết bị; xem mục QA của tài liệu bàn giao.

## Database mẫu dùng trực tiếp trong Navicat

Mọi file liên quan SQL nằm riêng tại thư mục database/. File database/PharmaSIU_Demo.sqlite3 là SQLite thực sự đã khởi tạo, gồm 6 bảng, 93 dòng mẫu và 4 VIEW. Mở trực tiếp: Navicat → Connection → SQLite → Existing database file → C:\PharmaSIU\PharmaSIU-1For4\database\PharmaSIU_Demo.sqlite3. Hoặc dùng DB Browser for SQLite, DBeaver.

Bộ SQL PostgreSQL ở database/postgresql/ gồm schema, dữ liệu mẫu, view và guard dùng khi cài PostgreSQL server. Toàn bộ hướng dẫn, cảnh báo và kiểm thử tại database/README.md. Chạy python database/tools/verify_database.py để kiểm chứng.

Đây là database dữ liệu giả lập; ứng dụng Android hiện vẫn vận hành trên RAM demo, chưa tự kết nối DB/backend. Không lưu toa, mật khẩu, thu tiền hoặc thông tin khách thật.
