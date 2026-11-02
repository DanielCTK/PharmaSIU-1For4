# PharmaSIU — Biên bản rà soát và bàn giao đồng bộ (29/10/2026)

## 1. Phạm vi thực tế
Đối chiếu repository Android Compose với SRS v1.2, BFD 5 nhóm/17 chức năng, 7 trang DFD, State 7 trạng thái và hai Activity. Bản này **không triển khai cơ sở dữ liệu**: không SQL/SQLite/Room, schema, DAO, migration, API/backend. Tham chiếu ERD 6 thực thể chỉ dùng để bảo toàn ngữ nghĩa. Tất cả quy trình mô phỏng luôn ghi rõ DEMO và không thay thế quyết định dược sĩ hoặc nhà thuốc.

## 2. Tài liệu chuẩn tạo trước code
- ALIGNMENT_SOURCE_OF_TRUTH.md — danh mục BFD, thứ tự và giới hạn.
- ALIGNMENT_DOMAIN_CONTRACT.md — State/Activity và các cổng nghiệp vụ.
- ALIGNMENT_WORKER_HANDOFF.md — ownership, quy trình giao nhiệm vụ và các cổng nghiệm thu.
- ALIGNMENT_TRACEABILITY.md — bảng 17/17 hạng mục với trạng thái thực hiện/CHỜ TÍCH HỢP.

## 3. Phần đã sửa và mã nguồn
- Khách: ProductDetailScreen/Cart/Checkout/Order/History chuyển thông điệp trạng thái sang "chưa xác minh tồn", "chỉ chọn tệp toa trên máy"; tách phần giá đã biết với sản phẩm chưa báo giá, tránh coi hàng Rx là miễn phí; hủy bản demo phải xác nhận/lý do.
- Điều phối Phiên: root PharmaSIUApp phòng trạng thái checkout lưu qua đổi tài khoản; ràng buộc file toa cục bộ với giỏ Rx hiện hành; kiểm tra tên/số điện thoại/chọn điểm nhận ở callback xử lý thực, không chỉ trên nút UI.
- Domain: PrescriptionSelectionRules, MockPriceCalculator, CanonicalOrderStage và canTransitionForOrder không được bỏ qua Rx review; không có hoạt động xác thực thật.
- Vận hành: StaffPermission enforce lệnh quản lý tại StaffDemoStore; người bán không có quyền dược sĩ; tạo/sửa tên/giá hàng demo; nhận lô RAM có ngày hết hạn, phân bổ FEFO ở lô mẫu hợp lệ, cách ly hàng; ghi số đếm và chênh lệch (không tự điều chỉnh tồn); đối soát tiền mẫu theo ca; POS đóng vai trò DEMO; xem yêu cầu khách trong cùng tiến trình chỉ để xem, chưa phải yêu cầu đến quầy.
- Đồng bộ tài liệu: sửa luồng đăng nhập chung, danh sách màn/route, thống kê 34 Compose Preview và các cảnh báo không có backend. Không xóa lịch sử QA trước đó.

## 4. Kết quả kiểm chứng
- Chạy ở repository thật: `gradlew.bat :app:testDebugUnitTest :app:assembleDebug --offline --no-daemon`.
- **BUILD SUCCESSFUL** (29/10/2026), APK debug được tạo tại `app/build/outputs/apk/debug/app-debug.apk`.
- XML JUnit: **16 suites, 58 test cases, 0 failures, 0 errors, 0 skipped**.
- `git diff --check`: không có lỗi whitespace sau khi xử lý.
- Mọi thay đổi thuộc code Kotlin, giao diện, unit tests và docs. `gradle.properties` có thay đổi từ trước, được giữ nguyên; không commit/push.
- Không chạy được E2E Android instrumented/emulator/TalkBack/fontScale/FPS trong lần rà soát này: adb tìm được qua Android SDK nhưng lệnh `adb devices` không liệt kê thiết bị/emulator kết nối. Không chấm PASS các hạng mục đó.
- Gradle đưa cảnh báo deprecation Gradle 8.13 so với Kotlin phiên bản tương lai và một số Android Icons/Locale; chưa ảnh hưởng build.

## 5. Ranh giới chưa thể hoàn tất nếu cấm database/backend
**BLOCKED_BY_BACKEND / KHÔNG PHẢI PASS:** xác thực/phân quyền server; Rx tải lên và thẩm định bởi dược sĩ; đơn khách chuyển tới hệ thống nhà thuốc thực; xác nhận giá/tồn khả dụng; giữ đủ toàn đơn + TTL, rollback/idempotency đồng thời; nhập/xuất/kiểm kê theo chứng từ thực, FEFO đa lô persisted và lịch sử kiểm toán; xác thực chuyển khoản/QR, hóa đơn thuế, giao hàng; đối soát kế toán thật; thông báo đặt trước.
Các chức năng tương ứng chỉ có giao diện demo, quy tắc thuần Kotlin và/hoặc nhãn CHỜ TÍCH HỢP. Dữ liệu RAM không được xem là bảo đảm chống bán trùng ở môi trường nhiều thiết bị.

## 6. Trước nghiệm thu trên thiết bị
Chạy smoke cho ba tài khoản qua đăng nhập chung; kiểm Rx khi chưa chọn toa/đổi giỏ; giá Rx 0 tham khảo; tạo/hủy có lý do, logout/back stack/đổi tài khoản; POS bán OTC hợp lệ, bán Rx bị chặn, lô hết hạn/cách ly, thiếu lô, kiểm kê chênh và đối soát ca; màn hẹp 320/390/412dp, font 1.5× và TalkBack; đo hiệu năng trên thiết bị thực. Không coi 58 JVM tests thay cho các bước này.

