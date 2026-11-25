# Nâng cấp giao diện PharmaSIU

Nhánh: `codex/ui-redesign`. Ứng dụng: `com.pharmasiu.mobile`, Java Views/XML, một module `:app`.

## Thay đổi

- Đồng bộ màu xanh đậm, nền sáng, thẻ bo góc, khoảng cách và phân cấp chữ.
- Đăng nhập có nhãn trường rõ ràng, nút chính nổi bật và tài khoản demo thu gọn.
- Trang chủ có tìm kiếm, lối tắt danh mục và thẻ sản phẩm dễ đọc; nút thêm/yêu thích có vùng chạm tối thiểu 48dp.
- Danh mục chuyển về một cột khi màn hình hẹp hoặc cỡ chữ từ 130%; giảm hình trang trí để dành chỗ cho nội dung.
- Giỏ hàng có điều khiển số lượng rõ ràng; thông tin demo và trạng thái cần xử lý được giữ trong luồng yêu cầu.
- Trang cá nhân dùng các hàng thao tác trực tiếp, có phản hồi khi chạm.
- Tổng quan quản trị dùng số liệu thực từ trạng thái demo trên máy, lối tắt và cảnh báo tồn/lô.
- POS có thẻ sản phẩm, trường nhập và nút lớn; ca đang mở hiển thị tóm tắt, mở rộng chi tiết khi cần.
- Đăng xuất từ thanh điều hướng có xác nhận; hủy giữ nguyên tab đang chọn.

## Kiểm tra

Lệnh build/test đã chạy thành công với JBR 21:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline --daemon --max-workers=2 --console=plain
```

Kết quả: **144 tests, 30 suites, 0 failures, 0 errors, 0 skipped**.

Đã chạy bản debug trên giả lập Medium Phone API 37.2, gồm:

- Đăng nhập khách hàng, nhân viên và quản trị viên bằng tài khoản demo.
- Trang chủ, tìm kiếm Acemuc, chi tiết sản phẩm, trang cá nhân.
- Thêm giỏ, tăng/giảm số lượng 1 → 2 → 1 và kiểm tra tổng tiền; mở biểu mẫu yêu cầu.
- Tổng quan quản trị; hủy xác nhận đăng xuất vẫn giữ tab.
- Mở/đóng ca demo số dư 0, mở rộng chi tiết ca, danh sách sản phẩm POS.
- Bố cục ở 1080×2400, chữ 100%; kiểm tra chữ 130% ở 900×2000. Trả cỡ chữ/kích thước về ban đầu sau kiểm tra.

Không gửi yêu cầu mua hàng, lập giao dịch hay xóa dữ liệu để kiểm thử. Không đổi domain, schema, quyền vai trò, quy tắc Rx/FEFO hoặc thêm dependency.

## Giới hạn

Đây vẫn là ứng dụng demo cục bộ. Chưa kiểm tra trên điện thoại vật lý hoặc mọi kích thước màn hình. Giá, ảnh, thông tin mặt hàng và tồn kho tiếp tục dùng dữ liệu sẵn có; bản nâng cấp này không xác minh nội dung y tế, thanh toán hay đồng bộ máy chủ.

APK: `app/build/outputs/apk/debug/app-debug.apk`. Log build/test và ảnh kiểm tra cục bộ nằm trong `build/ui-qa/` (không đưa vào Git).
