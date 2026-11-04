## Mục tiêu

<!-- Mô tả ngắn gọn vấn đề và kết quả mong muốn. -->

## Phạm vi thay đổi

<!-- Nêu feature/package/file ownership chính. Không sửa chồng ownership khi chưa phối hợp API. -->

## Kiểm chứng

- [ ] `./gradlew :app:assembleDebug :app:testDebugUnitTest`
- [ ] `python database/tools/verify_database.py`
- [ ] `git diff --check`
- [ ] UI/emulator/device QA đã chạy nếu thay đổi giao diện; nếu chưa, ghi rõ `NOT RUN` và lý do bên dưới.

**QA / bằng chứng:**

<!-- Ghi PASS/FAIL/NOT RUN. Đính kèm ảnh trước/sau khi PR thay đổi UI. -->

## An toàn dữ liệu và nghiệp vụ

- [ ] Không thêm dữ liệu người thật, ảnh toa thật, token, signing key, `local.properties` hoặc build output.
- [ ] Không mô tả dữ liệu DEMO là tồn/giá/thanh toán/Rx đã được backend hoặc dược sĩ xác nhận.
- [ ] Giữ một module `:app`, namespace/applicationId `com.pharmasiu.mobile`, Java 17 + Android Views/XML.

## Rủi ro và rollback

<!-- Nêu rủi ro còn lại và cách hoàn tác an toàn nếu cần. -->
