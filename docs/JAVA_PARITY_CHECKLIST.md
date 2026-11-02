# PharmaSIU — Java Views/XML parity checklist (29/10/2026)

## Kết luận hiện hành
- app-java là một **bản triển khai Java độc lập chạy được**, không phải chuyển đổi 1:1 hoàn toàn từ 50 tệp Kotlin/Compose.
- 9 tệp .java, 0 tệp .kt trong app-java/src/main. Giao diện dựng qua Android Views/Activity và layout XML; mỗi route dùng Java listeners. Gradle project vẫn dùng Kotlin DSL .gradle.kts cho cấu hình; mô-đun gốc :app vẫn giữ Kotlin để không làm mất các chức năng chưa chuyển.
- Chỉ đổi launcher/module mặc định sau khi xác nhận feature parity. Không được xóa :app hoặc tuyên bố toàn bộ dự án đã chuyển Java.
- Backend/SQLite/schema vẫn CHƯA triển khai đúng phạm vi đã chốt.

## Ma trận 17 chức năng
| BFD | Java module hiện tại | Mức độ |
|---|---|---|
| 1.1 Tìm kiếm sản phẩm | CustomerScreens.catalog: tìm tên/mã/mô tả; chưa có full category sort | Một phần |
| 1.2 Chi tiết và giá | CustomerScreens.product: giá tham khảo, điều kiện Rx; thiếu hình ảnh chi tiết | Một phần |
| 1.3 Xem khả dụng dự kiến | Hiện chưa xác minh tồn, không tự nhận hàng thực | Trung thực; chưa tích hợp |
| 2.1 Tạo đơn đặt trước | cart/checkout tạo DEMO-xxxx tại máy; tên, phone, điểm nhận | Demo |
| 2.2 Gửi toa khi cần | ACTION_OPEN_DOCUMENT chỉ chọn ảnh/PDF local; không upload/phê duyệt | Demo, chờ tích hợp |
| 2.3 Theo dõi/hủy đơn | orders/order-detail; hủy cần lý do và xác nhận UI | Demo |
| 3.1 Bán hàng POS | CashierScreens: mở ca, thêm OTC, số lượng, tính tiền, lưu phiếu demo | Demo; chưa có quét mã/lô thực |
| 3.2 Xử lý đơn đặt trước | Admin tổng hợp yêu cầu cùng thiết bị; chưa có work queue/giữ hàng | Chờ tích hợp |
| 3.3 Thẩm định toa theo quyền | Không tài khoản DEMO nào có quyền dược sĩ; không được duyệt | Chờ tích hợp |
| 3.4 Thu tiền và giao thuốc | Cash/OTHER_DEMO ghi phiếu RAM; chưa xác nhận tiền/giao thật | Demo |
| 4.1 Cập nhật sản phẩm và giá | AdminScreens.catalog: đổi tên/giá RAM; chặn Rx | Demo |
| 4.2 Ghi nhận nhập theo lô | AdminScreens.lots: mã lô, ngày hết hạn, số lượng RAM | Demo |
| 4.3 Kiểm tra hạn & chất lượng | Hiện danh sách hết hạn/cách ly; JavaRules FEFO và POS từ chối thiếu lô hợp lệ | Một phần; chưa có quy trình xác nhận |
| 4.4 Kiểm kê chênh lệch | AdminScreens.audit: số đếm, lý do, báo lệch lưu RAM không tự điều chỉnh tồn | Demo |
| 5.1 Tài khoản/phân quyền | AuthScreen + DemoState role gate, nhân viên không sửa danh mục, không có quyền dược sĩ | Demo; chưa quản lý người dùng thật |
| 5.2 Báo cáo tổng hợp | AdminScreens.reports: số phiếu/tiền/lô trong phiên | Demo |
| 5.3 Đối soát đơn theo ca | AdminScreens.audit: kỳ vọng/kiểm đếm, lý do trong RAM | Demo |

## Các khoảng cách lớn với bản Compose cũ
- Chưa chuyển đầy đủ ảnh/media sản phẩm, hero/chuyển động, onboarding, bộ lọc/sort nâng cao và danh mục mở rộng.
- Chưa chuyển màn đăng ký khách, chỉnh hồ sơ, sổ địa chỉ, đổi mật khẩu, quản trị tài khoản chi tiết.
- Chưa dựng workflow staff xử lý đặt trước, thẩm định toa có backend, trạng thái giữ đủ toàn đơn/TTL, thanh toán xác minh và bằng chứng giao hàng.
- State machine hiện là lớp JavaRules hỗ trợ chuẩn; chưa có server nên không tự chuyển REQUEST_RECEIVED sang HELD/READY.
- Chưa đạt kiểm thử accessibility 1.5×, TalkBack, 320dp và toàn bộ E2E theo vai trò.
- :app Kotlin vẫn được giữ để tránh mất tính năng; không thay app gốc bằng Java khi chưa đạt parity.

## Tiêu chuẩn hoàn thành chuyển đổi toàn bộ
Chỉ đánh dấu 100% khi tất cả màn/luồng cũ có Java/XML tương đương, các test từ :app được chuyển Java, 17/17 luồng được xác minh hoặc gắn đúng CHỜ TÍCH HỢP, app-java qua UI E2E và role tests, sau đó mới đổi launcher và loại bỏ Kotlin theo kế hoạch an toàn.
