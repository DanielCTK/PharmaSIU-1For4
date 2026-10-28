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

**Kiểm tra tĩnh:** XML parser không phát hiện lỗi ở `app/src/main`; `git diff --check` được chạy. Số lượng: 30 sản phẩm mẫu, 31 Compose @Preview ở hai gallery (14 + 12 + 2 + 3), 14 unit tests đã chạy thành công sau lần chia nhỏ file cuối.

**Toolchain đã dùng:** AGP 8.13.2, Gradle Wrapper 8.13, Kotlin/Compose Compiler 2.4.10, Compose BOM 2025.12.01 và Coil 3.3.0 theo môi trường máy test (phải khóa lại những phiên bản này trong repository). Các @Preview mới chưa được Android Studio render/duyệt screenshot thực tế nên G3 vẫn NOT RUN.

**Rà soát chi tiết từng màn hình:** [UI audit 28/10/2026](UI_AUDIT_2026-10-28.md).

**Đặc tả flow mở rộng:** [Mobile Customer Flows](MOBILE_CUSTOMER_FLOWS.md). Các bước đăng nhập demo, gửi yêu cầu và điều hướng chưa được tự động hóa bằng Android instrumented UI tests; unit test không thay thế full user journey.

**Lưới sản phẩm nổi bật (28/10/2026):** Sử dụng LazyVerticalStaggeredGrid 2 cột với chiều cao ảnh so le; đã chạy Gradle 8.13 `:app:assembleDebug :app:testDebugUnitTest` **PASS**. Hai preview 390dp/320dp mới đã khai báo; chưa render screenshot hoặc thử nghiệm accessibility/emulator. Hình minh họa được dựng trong Compose, chưa thay bằng ảnh sản phẩm xác thực.

**Rà soát Danh mục (28/10/2026):** Thay khoảng trắng trong vùng header bằng banner Emerald có minh họa 3 nhóm hàng, ghép thanh kết quả và sắp xếp thành một hàng có phân cấp, chỉnh card hình ảnh theo đúng nhóm hàng, chuyển toàn trang sang một LazyVerticalGrid với full-span hero/search/filter/header. Đã cài APK debug và **chụp/quan sát thực tế** trên Android Emulator 1080×2400, density 420. Đã xem ảnh chụp emulator ở 320dp và điều chỉnh tiêu đề ngắn hơn để tránh chạm minh họa; đã build lại thành công. TalkBack và fontScale=1.5 vẫn chưa được thử trực tiếp.
**Bằng chứng giao diện Danh mục:** [Ảnh chụp emulator sau chỉnh sửa](screenshots/catalog-emerald.png), màn hình 1080×2400, có thanh tìm kiếm, banner mới, hàng kết quả/sắp xếp, card sản phẩm. Ảnh là screenshot app debug với dữ liệu mock, không phải thiết kế Figma hoặc ảnh dựng tay.
