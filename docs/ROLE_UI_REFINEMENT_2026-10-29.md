# PharmaSIU — Kế hoạch nâng cấp giao diện theo phân quyền 29/10/2026

## Mục tiêu
Nâng cấp đúng các màn hình đang sơ sài, không thay đổi toàn bộ luồng đã thống nhất hoặc logic phiên RAM demo. Ba trải nghiệm khác nhau nhưng thống nhất thương hiệu:
- Khách hàng: khám phá dễ hiểu → chi tiết → đặt trước → theo dõi bản demo. Trình bày giá chưa xác định và trạng thái an toàn trước CTA.
- Nhân viên: thao tác nhanh tại quầy, trạng thái ca rõ, sản phẩm và giỏ dễ quét bằng mắt, phương thức thu tiền **demo** không mạo nhận thanh toán thật.
- Quản trị: dashboard có phân cấp dữ liệu, ưu tiên hàng tồn/batch/đối soát, thao tác có quyền và mô tả CHỜ TÍCH HỢP thật.

## Design DNA được bảo vệ
Emerald #064D3A / Forest #073D30 / Lime #B9F48B / Cream #F6F7F2; nền có bề mặt rõ, phân cấp chữ và khoảng trắng. Không đổi brand icon, không thêm hình thuốc sai nguồn, không dùng icon vô nghĩa. Giữ các animation chuyển màn đang chạy, tránh hiệu ứng blur nặng. Các icon, màu status phải có nhãn chữ. Ưu tiên hiển thị tốt ở 320–412dp, fontScale=1.5, vùng chạm 48dp.

## Những điểm UI cần cải thiện
1. Customer: thông tin đơn, toa, số tiền đã biết/chưa báo, trạng thái local demo; các empty/error state và CTA; chi tiết sản phẩm dễ đọc.
2. Cashier: shift rõ trước khi bắt đầu; vùng tìm/hàng khả dụng/giỏ; giá, tình trạng Rx, tổng và payment affordances; xác nhận bán demo và lịch sử phiếu.
3. Admin: dashboard hiển thị tình hình, thẻ KPI, cảnh báo tồn/đơn và thao tác quản trị; Hàng hóa phân cấp filter, lô/kiểm kê, đối soát ca; rõ quyền ADMIN.
4. Login role gateway: màn hình chung vẫn là điểm vào, dùng cùng từ ngữ và hình thức; không được tạo các điểm vào đăng nhập phân tán.

## Quy tắc bảo toàn
- Không thêm/sửa database, DAO, schema, migration, API hay backend.
- Không biến 'đã chọn file' thành 'đã tải toa'; không biến 'đã tạo DEMO' thành 'đã xác nhận giữ hàng'.
- ADMIN không có quyền dược sĩ; CASHIER không sửa danh mục. Không thay đổi hành vi CounterRules và StaffPermission.
- Không sửa gradle.properties và không xóa những thay đổi chưa commit từ lần đồng bộ cấu trúc.
- Chỉ sửa UI, state trình bày và preview/test nếu cần; không commit/push.

## Ownership / tích hợp
- CUSTOMER: ui/features/{account,community,orders,checkout,product,prescription} (chỉ UI), không chạm Staff.
- CASHIER: ui/features/staff/CashierScreen.kt, ui/features/staff/StaffProductMedia.kt (nếu cần) và preview mới riêng.
- ADMIN: ui/features/staff/OperationsHub.kt, StaffCatalogEditor.kt; không chạm CashierScreen.
- PRIME: giao diện dùng chung nếu cần, review, ghép staging vào repo và chạy build/test.

## Tiêu chí nghiệm thu
- 3 phân quyền rõ nhiệm vụ, không màn nào để trống chức năng cần có mà không giải thích.
- UI không mắc trùng text, hidden CTA, cắt nội dung, chồng màn khi chữ lớn; nếu không có emulator ghi rõ 'chưa kiểm thử thiết bị'.
- Unit tests và assembleDebug PASS; ảnh minh họa/preview nếu đã có giữ được.
- Các chức năng chờ server phải có nhãn thay vì làm nút giả thành công.

