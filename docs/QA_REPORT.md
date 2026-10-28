# QA log — PharmaSIU Mobile 0.1.0

| Gate | Trạng thái | Bằng chứng/việc còn thiếu |
|---|---|---|
| G0 Truth & scope | REVIEWED | Tất cả màn hình gắn nhãn demo; không giả xác nhận tồn hoặc toa |
| G1 User flow | REVIEWED | Home → Catalog → PDP → Cart → Rx (nếu cần) → Checkout → Preview có callback |
| G2 Accessibility | NOT RUN | Cần TalkBack, 48dp targets và font scale trên emulator |
| G3 Visual | NOT RUN | Cần ảnh chụp 320/390/412dp và review thủ công |
| G4 Error/recovery | PARTIAL | Search empty, form invalid, no Rx file đã được xử lý; network chưa tích hợp |
| G5 Runtime | BLOCKED | Đã thử Gradle 8.13 wrapper nhưng download chưa xong; Gradle 9.3.1 offline thiếu Kotlin 2.4.10 plugin. Bản online cũng được thử; xem kết quả sau. Chưa có bằng chứng APK/unit tests PASS |
| G6 Consistency | REVIEWED | Màu, typography và shared components dựa PharmaPalette |

Không tuyên bố production ready từ kết quả review mã nguồn. Các gate NOT RUN không phải PASS.

**Kiểm tra tĩnh đã chạy:** tất cả XML trong `app/src/main` đọc được bằng XML parser; `git diff --cached --check` không phát hiện whitespace error. Đây không phải bằng chứng app biên dịch hay chạy trên emulator.

**Toolchain dự kiến:** AGP 8.13.2, Gradle 8.13, Kotlin/Compose Compiler 2.4.10 và Compose BOM 2026.09.00. Thư viện Coil 3.6.3 + OkHttp đã tích hợp để tải ảnh từ catalog HTTPS; dữ liệu demo không có URL và không gọi mạng. Có 14 Compose Preview cho các màn hình/giá trị biên; việc render vẫn NOT RUN.

**Rà soát chi tiết từng màn hình:** [UI audit 28/10/2026](UI_AUDIT_2026-10-28.md).
