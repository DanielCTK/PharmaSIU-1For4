# PharmaSIU Mobile — luồng khách hàng tương tác (v0.2 mock)

**Trạng thái:** Bản thử nghiệm native Android, dùng dữ liệu và tài khoản **mô phỏng cục bộ**. Không có backend, không gửi OTP/email, không tải toa lên nhà thuốc, không kiểm tra tồn thật, không thanh toán. Lịch sử đơn là bản xem trước lưu trong RAM, không phải đơn đã được xác nhận.

## Điều hướng

- Khởi động lần đầu → Giới thiệu (Bỏ qua/Tiếp tục) → Trang chủ.
- Tab chính: Trang chủ / Danh mục / Yêu thích / Yêu cầu / Tài khoản.
- Danh mục → Tìm kiếm/Lọc/Sắp xếp → Chi tiết → Yêu thích hoặc Giỏ → Rx nếu có → Checkout → Lưu bản mẫu → Lịch sử.
- Yêu cầu → Chi tiết → Hủy trên thiết bị hoặc Thêm sản phẩm vào giỏ lại.
- Tài khoản → Đăng nhập/Đăng ký/Đặt lại mật khẩu demo → Chỉnh sửa hồ sơ / Địa chỉ / Đổi mật khẩu.

## Màn hình đã có mã tương tác

| Nhóm | Flow |
|---|---|
| Onboarding | Ba thẻ giới thiệu, tiếp tục và bỏ qua; chỉ tự hiện lần đầu bằng cờ local |
| Đăng nhập | Tài khoản thử 0900000000 / demo1234, kiểm tra trong RAM, không xác thực server |
| Đăng ký | Nhập tên, điện thoại, mật khẩu mẫu; kiểm tra trùng trong phiên, tạo hồ sơ RAM |
| Quên/đổi mật khẩu | Đặt lại, đổi mật khẩu demo trong phiên; không gửi OTP hoặc email |
| Hồ sơ | Xem/chỉnh sửa tên, điện thoại, email, ngày sinh ghi chú |
| Sổ địa chỉ | Thêm/sửa/xoá, đặt mặc định địa chỉ liên hệ; khác với điểm nhận tại quầy |
| Danh mục | 30 sản phẩm mẫu: thuốc, mỹ phẩm, thực phẩm, thực phẩm bổ sung, chăm sóc, thiết bị, vật tư |
| Tìm kiếm | Không phân biệt hoa/thường hoặc dấu tiếng Việt, tách từ, lọc nhóm, sắp tên/giá |
| Yêu thích | Lưu và bỏ ở card/PDP, xem trong tab; tài khoản demo có danh sách riêng |
| Giỏ hàng | Thêm, tăng, giảm, xoá, giá tạm tính; mock quantity cap |
| Rx | Chọn ảnh toa từ thiết bị chỉ để trải nghiệm; chưa tải lên hoặc duyệt |
| Click & Collect | Điểm nhận mẫu A/B, liên hệ, tóm tắt, lưu yêu cầu mô phỏng vào lịch sử |
| Lịch sử | Xem chi tiết, hủy bản cục bộ, chọn lại sản phẩm để thử checkout |

## Nguyên tắc nghiệp vụ

1. Rx khác OTC; phải chọn ảnh toa trước checkout nếu có thuốc kê đơn. Không có trạng thái approved từ app.
2. Thực phẩm khác thuốc; thực phẩm bổ sung có cảnh báo không thay thế thuốc, mỹ phẩm và thực phẩm không gắn nhãn Rx.
3. Đặt trước không có nghĩa đã giữ hàng. Không có giá/tồn real-time.
4. Giá là dữ liệu minh họa; mặt hàng chưa báo giá không được trình bày như giá thanh toán thật.
5. Lịch sử local chỉ có CREATED_LOCALLY và CANCELLED_LOCALLY; không có PAID/RESERVED/READY.
6. Người dùng không nhập mật khẩu hoặc thông tin thật. Demo accounts, favorites, requests nằm RAM, mất khi kết thúc tiến trình. Chỉ cờ đã xem onboarding lưu trên máy.

## Acceptance tests trọng tâm

| Mã | Thao tác | Kết quả |
|---|---|---|
| F01 | Cài mới → Bỏ qua giới thiệu → chạy lại | Không hiện onboarding lần nữa |
| F02 | Guest → Danh mục → Chi tiết → Back | Quay lại danh mục |
| F03 | Lọc “Mỹ phẩm” | Chỉ hiện mỹ phẩm |
| F04 | Tìm “sua rua mat” và “SỮA RỬA MẶT” | Cùng kết quả |
| F05 | Tìm không thấy | Có empty state |
| F06 | Sắp giá tăng/giảm | Hàng chưa giá nằm cuối |
| F07 | Lưu yêu thích từ card → tab Yêu thích → bỏ lưu | Đồng bộ |
| F08 | Guest đăng nhập sai mật khẩu | Không tạo session |
| F09 | Đăng nhập demo → hồ sơ | Hiện tên và điện thoại |
| F10 | Đăng ký trùng điện thoại | Không ghi đè tài khoản |
| F11 | Reset mật khẩu demo → login bằng mật khẩu mới | Thành công trong RAM |
| F12 | Tạo địa chỉ và đổi mặc định | Tối đa một địa chỉ mặc định |
| F13 | Hồ sơ với số điện thoại sai | Không lưu |
| F14 | Giỏ OTC + mỹ phẩm → thay số lượng | Đúng số dòng và tổng |
| F15 | Giỏ có Rx thiếu ảnh toa | Phải qua bước chọn ảnh |
| F16 | Chọn ảnh toa → checkout | Hiện “đã chọn”, không “đã duyệt” |
| F17 | Checkout thiếu thông tin | Không lưu yêu cầu |
| F18 | Lưu checkout mock → tab Yêu cầu | Một bản xem trước, không có đơn thật |
| F19 | Chi tiết yêu cầu → mua lại | Giỏ tái tạo, Rx cần ảnh mới |
| F20 | Hủy bản local / Đăng xuất | Không phát sinh backend, hồ sơ ẩn với guest |

## Chưa triển khai và không được giả thành hoàn chỉnh

Xác thực server/OTP, upload toa có phân quyền, dược sĩ duyệt, giá/tồn từ hệ thống, reservation TTL, thanh toán thực, chọn nhà thuốc có địa chỉ thật, lưu trữ an toàn thông tin khách, đo trải nghiệm trên thiết bị, E2E và release signing. Đây là backlog cho bản vận hành sau.
