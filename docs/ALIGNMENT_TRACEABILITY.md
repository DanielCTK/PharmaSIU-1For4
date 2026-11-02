# PHARMASIU — MA TRẬN TRUY VẾT 17 CHỨC NĂNG (29/10/2026)

Trạng thái trong bảng là đánh giá kết nối code hiện tại, KHÔNG khẳng định nghiệp vụ server. Bản build v0.2 hiện là dữ liệu mô phỏng; trạng thái sẽ được cập nhật sau tích hợp test.

| BFD | Điểm vào/phần code | Trạng thái / giới hạn nghiệm thu |
|---|---|---|
| 1.1 Tìm kiếm sản phẩm | CatalogScreen / CatalogSearchRules | DEMO có tìm kiếm, lọc, sắp xếp |
| 1.2 Xem chi tiết và giá bán | ProductDetailScreen | DEMO; giá tham khảo/chờ báo giá |
| 1.3 Xem khả dụng dự kiến | ProductDetailScreen, catalog | CHỜ TÍCH HỢP: chưa xác minh tồn qua server |
| 2.1 Tạo đơn đặt trước | CartScreen, CheckoutScreen, PharmaSIUApp | DEMO mã DEMO; chưa gửi nhà thuốc |
| 2.2 Gửi toa thuốc khi cần | PrescriptionScreen / picker | CHỌN FILE LOCAL, không upload, không duyệt |
| 2.3 Theo dõi và hủy đơn | RequestHistoryScreen, MockRequestDetailScreen | DEMO lịch sử, hủy cục bộ; không hủy server |
| 3.1 Bán hàng tại quầy | CashierScreen, CounterRules | DEMO POS, ca, số lượng giả lập |
| 3.2 Xử lý đơn đặt trước | OperationsHub | CHỜ TÍCH HỢP; không giữ thật/toàn đơn |
| 3.3 Thẩm định toa theo quyền | CounterRules, OperationsHub | CHỜ DƯỢC SĨ/BACKEND; cashier/admin không được duyệt |
| 3.4 Thu tiền và giao thuốc | CashierScreen, StaffDemoStore | DEMO phiếu tiền mặt; không có xác nhận thu/giao thật |
| 4.1 Cập nhật sản phẩm và giá | StaffCatalogEditor, StaffDemoStore | DEMO danh mục staff độc lập |
| 4.2 Ghi nhận nhập theo lô | MockLotRules, StaffDemoStore, khu Hàng hóa | DEMO trong RAM có mã lô/hạn; không có phiếu nhập hoặc tồn thật |
| 4.3 Kiểm tra hạn và chất lượng | MockLotRules, khu Hàng hóa | DEMO chặn lô hết hạn/cách ly; server vẫn chờ tích hợp |
| 4.4 Kiểm kê, xử lý chênh lệch | StaffDemoStore, khu Hàng hóa | DEMO lưu số đếm/chênh lệch/lý do; chưa duyệt điều chỉnh kho thật |
| 5.1 Tài khoản và phân quyền | UnifiedDemoAuth, AppEntryState | DEMO phân vai trò, không có auth/RBAC server |
| 5.2 Xem báo cáo tổng hợp | OperationsHub | DEMO số liệu cùng phiên |
| 5.3 Đối soát đơn theo ca | StaffDemoStore, OperationsHub | DEMO đối chiếu tiền kiểm đếm mẫu với tiền mặt kỳ vọng; chưa có đối soát kế toán thật |

## Vùng mã kiểm tra
- Khách: app/src/main/java/com/pharmasiu/mobile/ui/features/{catalog,product,cart,prescription,checkout,orders,community}.
- Nhân viên: app/src/main/java/com/pharmasiu/mobile/ui/features/staff.
- Quy tắc: app/src/main/java/com/pharmasiu/mobile/domain.
- Điều phối session và tích hợp: app/src/main/java/com/pharmasiu/mobile/ui/PharmaSIUApp.kt.
- Tự động: app/src/test/java/com/pharmasiu/mobile/.

## Kiểm soát cuối
- Unit tests cho chuyển trạng thái, kiểm Rx, chống dùng toa cho giỏ khác, checkout, cashier/ca, role.
- Dữ liệu demo hiển thị DEMO, không coi là server nhận đơn/đã giữ/duyệt/toa đã tải lên.
- Gradle build và JVM tests phải chạy lại sau mọi sửa đổi.
- UI trên emulator/thiết bị thực, TalkBack, font scale, hiệu năng chưa được gọi PASS nếu không có bằng chứng.
- Không triển khai database ở đợt này: KHÔNG thêm/sửa SQL, Room, DAO, migrations hoặc schema.

