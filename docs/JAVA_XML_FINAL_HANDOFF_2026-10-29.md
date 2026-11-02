> **Lưu ý phiên bản:** Kết quả bàn giao bên dưới ghi nhận giai đoạn chuyển từ Kotlin sang Java trước khi tái cấu trúc. Cấu trúc module mới và số lượng file hiện hành nằm tại [JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md](JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md).

# PharmaSIU — Biên bản chuyển toàn bộ mã nguồn ứng dụng sang Java/XML
**Ngày:** 29/10/2026  
**Phạm vi:** Dự án Android gốc, một module `:app`, appId `com.pharmasiu.mobile`.

## 1. Kết quả thực tế

Đã thay nguồn ứng dụng Kotlin/Compose trong repository bằng Java/Android Views + XML. Đích cuối cùng là `C:\PharmaSIU\PharmaSIU-1For4\app`, **không có một ứng dụng Java chạy song song**.

| Tiêu chí | Kết quả trên project cuối |
|---|---|
| Module Gradle | Chỉ `:app` |
| Namespace / applicationId | `com.pharmasiu.mobile` |
| Source chính | **50 tệp Java** |
| Unit test | **29 tệp JUnit Java** |
| XML tài nguyên app | **52 tệp XML** |
| Tệp `.kt` hoặc `.kts` trong repo | **0** |
| Plugin Kotlin, Compose | **Không còn** |
| Bộ build | `build.gradle`, `settings.gradle` Groovy |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |

Đã xóa khỏi repository hai module/thư mục thử nghiệm `app-java` và `java-only`. Các file Gradle Kotlin DSL cũ đã được thay bằng Groovy. Launcher `MainActivity.java` ở `com.pharmasiu.mobile`; ứng dụng gốc được giữ đúng package, không tạo một app khác.

## 2. Chuyển đổi theo nhóm

- **15 Kotlin data/model/repository/domain** chuyển thành **34 Java files** do nhiều Kotlin data class, enum, top-level type cần tách thành từng file Java; có `Medicine`, `MockRequest`, `CatalogSearchRules`, `CounterRules`, `MockLotRules`, `OrderStateMachine`, `PrescriptionSelectionRules`, `UnifiedDemoAuth`... 
- **16 Kotlin test classes** (58 `@Test`) chuyển thành **16 lớp Java JUnit** với 58 phương thức kiểm thử tương ứng; giữ các lớp kiểm thử Java cũ.
- **35 Kotlin UI/Compose/MainActivity/Theme/Components/Previews** được thay theo chức năng bằng màn Android Java Views, XML layout, Android Canvas, theme/colors/styles XML; module có những màn Khách hàng, Auth, POS, Quản trị, Media và các tuyến điều hướng. Các Compose Preview của môi trường phát triển được thay bằng xem XML Layout Editor và smoke emulator, không còn module preview Compose.
- **Core demo** có snapshot lịch sử yêu cầu, snapshot giá/tên POS, FEFO, lô cách ly/hết hạn, kiểm kê, đối soát một lần sau đóng ca, guard quyền tại store. Ảnh staff là ảnh cục bộ `content://`; chưa có upload.

## 3. Test và cài thử

Lệnh thực thi trên root:

```powershell
.\gradlew.bat :app:clean :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon --console=plain
```

Kết quả: **BUILD SUCCESSFUL**, JUnit **141/141 PASS** (29 suites, 0 failures, 0 errors, 0 skipped). Kiểm tra xây dựng APK từ `:app` trên Android SDK API 36. Đã cài trên emulator API 28 và khởi động lớp `com.pharmasiu.mobile.MainActivity`; thử cả **3 vai trò**: customer DEMO → Trang chủ, cashier DEMO → POS, admin DEMO → dashboard/Hàng hóa, đều vào đúng route. Không thấy `FATAL EXCEPTION` trong logcat đã kiểm tra. QA agent trước khi đổi package từng đi qua luồng Khách hàng, POS và Quản trị; tuy nhiên smoke này **không thay thế** full E2E cho APK cuối trên mọi thiết bị.

## 4. Điều đã bảo toàn

- Toàn bộ **50 Kotlin main + 16 Kotlin tests** được đối chiếu SHA256 với bản sao lưu trước khi xóa. Các sửa đổi chưa commit được ghi lại trong một patch ngoài repository. Backup riêng chỉ để khôi phục khi cần, **không nằm trong source project**.
- Còn đúng một bộ source, một launcher/appId và một module chạy chính.
- Không thay đổi SQL/Room/DAO/schema/backend, không nhập dữ liệu cá nhân hay toa thật, không commit/push.

## 5. Giới hạn chưa được xác nhận

Đây là **chuyển mã toàn bộ sang Java/XML ở mức source tree**, không phải chứng minh từng dòng Compose cũ tương đương 100% về pixel, animation hay mọi màn hình. Một số UI component/previews được thay thế theo chức năng bằng Java/XML, không duy trì cấu trúc Kotlin file 1:1. Các kiểm thử font 1.5×, TalkBack, 320/412dp, performance đầy đủ, Android phiên bản mới/Safari không liên quan, và tất cả thao tác picker/permissions trên thiết bị thật vẫn cần xác minh bổ sung.

**Tất cả nghiệp vụ hiện ở mức DEMO trên RAM.** Không có hệ thống xác minh tồn, giữ hàng theo TTL, thẩm định Rx, gửi toa, xác nhận thanh toán hoặc hóa đơn sản xuất; không nên hiểu các cảnh báo/chức năng demo là kết quả vận hành thật.

## 6. Hướng dẫn mở dự án

Android Studio → **Open** → `C:\PharmaSIU\PharmaSIU-1For4` → chọn module **app** → **Run**. Không mở các thư mục `app-java`, `java-only` (đã loại khỏi repository).

**Tình trạng Git:** thay đổi đang ở local working tree trên `main`, chưa commit hoặc push.
