# QA log — PharmaSIU Mobile v0.2 (28/10/2026)

| Gate | Trạng thái | Bằng chứng/việc còn thiếu |
|---|---|---|
| G0 Truth & scope | REVIEWED | Dữ liệu mock phân loại thuốc, mỹ phẩm, thực phẩm; không giả xác nhận tồn, duyệt toa hoặc thanh toán |
| G1 User flow | STATIC REVIEW | 5 tab, onboarding, auth demo, profile, addresses, favorites, search/filter/sort, cart, Rx, checkout, history, cancel/reorder được nối callbacks; cần device end-to-end |
| G2 Accessibility | NOT RUN | Cần TalkBack, 48dp targets và font scale trên emulator |
| G3 Visual | NOT RUN | Cần ảnh chụp 320/390/412dp và review thủ công |
| G4 Error/recovery | PARTIAL | Search empty, invalid login/profile/checkout, no Rx file và canceled mock request đã xử lý; network/backend chưa có |
| G5 Runtime | PASS (Gradle), DEVICE NOT RUN | Gradle 8.13 debug APK và JUnit tests đạt. Đã build và chạy JUnit sau khi chia mỗi màn hình ra file riêng, kết quả thành công |
| G6 Consistency | REVIEWED | Màu, typography và shared components dựa PharmaPalette |

Không tuyên bố production ready từ kết quả review mã nguồn. Các gate NOT RUN không phải PASS.

**Kiểm tra tĩnh:** XML parser không phát hiện lỗi ở `app/src/main`; `git diff --check` được chạy. Số lượng: 30 sản phẩm mẫu, 26 Compose @Preview ở hai gallery (14 + 12), 14 unit tests đã chạy thành công sau lần chia nhỏ file cuối.

**Toolchain đã dùng:** AGP 8.13.2, Gradle Wrapper 8.13, Kotlin/Compose Compiler 2.4.10, Compose BOM 2025.12.01 và Coil 3.3.0 theo môi trường máy test (phải khóa lại những phiên bản này trong repository). Các @Preview mới chưa được Android Studio render/duyệt screenshot thực tế nên G3 vẫn NOT RUN.

**Rà soát chi tiết từng màn hình:** [UI audit 28/10/2026](UI_AUDIT_2026-10-28.md).

**Đặc tả flow mở rộng:** [Mobile Customer Flows](MOBILE_CUSTOMER_FLOWS.md). Các bước đăng nhập demo, gửi yêu cầu và điều hướng chưa được tự động hóa bằng Android instrumented UI tests; unit test không thay thế full user journey.
