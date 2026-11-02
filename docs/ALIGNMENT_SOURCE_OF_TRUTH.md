# PHARMASIU — CẤU TRÚC CHUẨN CHO ĐỢT ĐỒNG BỘ (29/10/2026)

## 1. Căn cứ và thứ tự áp dụng
1. BFD tinh gọn: 5 nhánh, đúng 17 chức năng liệt kê bên dưới; 7 sơ đồ DFD xác định luồng dữ liệu tương ứng.
2. State Diagram 7 trạng thái chính và 2 Activity (Click & Collect, POS) xác định chuỗi công việc và ngoại lệ.
3. SRS v1.2 bổ sung ràng buộc chuyên môn/an toàn, chống âm tồn, thẩm định toa, thu tiền và truy vết. SRS rộng hơn BFD: không tự tuyên bố đã hoàn thành các chức năng ngoài phạm vi.
4. ERD và CSDL 6 bảng chỉ để so khớp ý nghĩa dữ liệu; KHÔNG triển khai database trong đợt này.
5. Code Android là ứng dụng Compose mock; không coi mock là giao dịch nhà thuốc thật.

## 2. Danh mục BFD chính xác
| Mã | Chức năng | Nhóm quyền |
|---|---|---|
| 1.1 | Tìm kiếm sản phẩm | Khách mua hàng |
| 1.2 | Xem chi tiết và giá bán | Khách mua hàng |
| 1.3 | Xem khả dụng dự kiến | Khách mua hàng |
| 2.1 | Tạo đơn đặt trước | Khách mua hàng |
| 2.2 | Gửi toa thuốc khi cần | Khách mua hàng |
| 2.3 | Theo dõi và hủy đơn | Khách mua hàng |
| 3.1 | Bán hàng tại quầy (POS) | Nhân viên bán hàng |
| 3.2 | Xử lý đơn đặt trước | Nhân viên bán hàng |
| 3.3 | Thẩm định toa theo quyền | Người có quyền dược sĩ |
| 3.4 | Thu tiền và giao thuốc | Nhân viên bán hàng |
| 4.1 | Cập nhật sản phẩm và giá | Quản trị viên |
| 4.2 | Ghi nhận nhập theo lô | Quản trị viên |
| 4.3 | Kiểm tra hạn và chất lượng | Quản trị viên |
| 4.4 | Kiểm kê, xử lý chênh lệch | Quản trị viên |
| 5.1 | Tài khoản và phân quyền | Quản trị viên |
| 5.2 | Xem báo cáo tổng hợp | Quản trị viên |
| 5.3 | Đối soát đơn theo ca | Quản trị viên |

## 3. Nguồn được người dùng cung cấp (SHA256)
- BFD_5Nhanh17ChucNang: b7500425ddfb73dca8055a93785e7a2aa27285cf63d791b5080c85dfd51c6cd0
- ERD6_DFD7: 9287cca34aab523de9e5af2cf7f9c40b5390784c1edab3d2fe8c00a71aa6ca36
- State_2Activity: 322e48c6cdf114e96413cdbb613a53541e2e52a9204577ad1dec11a5bfffda53
- CSDL_6Bang: f2c112b25c417a62919e841183fc9b323db46b2586dd9ff61b0c5f107eb3f1f0
- SRS_v1.2.pdf: 0b6baf5897b5963d5bd5204068722e55581e37e32c14e875231690c2ae88d33d

## 4. ĐIỀU KIỆN KHÔNG THỂ BỎ QUA
- Khách tạo yêu cầu không đồng nghĩa nhà thuốc đã nhận/duyệt, giữ hàng, thanh toán hoặc giao hàng.
- Chọn ảnh toa trong Android KHÔNG đồng nghĩa gửi toa; duyệt Rx chỉ người có quyền dược sĩ; quản trị viên không mặc nhiên là dược sĩ.
- FEFO chỉ trên lô hợp lệ; lô hết hạn/cách ly không bán, không giữ âm tồn. Đơn thiếu hàng phải không giữ dở dang.
- Chỉ đánh dấu HOÀN TẤT khi thanh toán được xác minh VÀ đã bàn giao; thanh toán không xác minh không trừ kho.
- Hủy/quá hạn phải có lý do, không tự nhận đã nhả giữ khi hệ thống không có quyền xác nhận.
- Giá/số lượng demo và nguồn dữ liệu khách phải phân biệt rõ dữ liệu thật.
- Các quy trình phụ thuộc server không có server phải ghi CHỜ TÍCH HỢP, không chế tạo trạng thái kết quả.

## 5. NGOẠI LỆ VÀ PHẠM VI
CẤM sửa/thêm SQL, SQLite, Room, DAO, migrations, database, API backend thật, schema hay các bảng ERD. ĐƯỢC chỉnh UI, hợp đồng domain Kotlin thuần, dữ liệu mẫu trong RAM có nhãn demo, unit tests, tài liệu/điều hướng. Không commit/push hoặc xóa chỉnh sửa ngoài phạm vi.

