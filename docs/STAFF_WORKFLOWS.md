# PharmaSIU — Nhân viên bán hàng & Quản trị (phiên bản demo)

## Phạm vi

Ứng dụng giữ nguyên toàn bộ phần Khách hàng. Cổng riêng ở **Tài khoản → Vào khu vận hành → Đăng nhập nội bộ** sử dụng dữ liệu RAM, không phải hệ thống phân quyền hoặc API thật.

Email và mật khẩu demo công khai để kiểm tra giao diện: **Nhân viên bán hàng `nhanvien@pharmasiu.demo` / `1111`**, **Quản lý `quanly@pharmasiu.demo` / `2222`**. Không dùng những mã này trong môi trường thực.

## Luồng nhân viên bán hàng

1. Nhập email/mật khẩu demo của Nhân viên hoặc chọn tài khoản trong mục **Tài khoản dùng thử** → Đăng nhập → vào khu vận hành → tab Bán hàng.
2. **Mở ca bán** trước khi chọn sản phẩm.
3. Tìm theo tên/nhóm hàng → chọn **Thêm**. Chỉ bán sản phẩm có giá, không phải Rx, và chưa vượt tồn mẫu.
4. **Xem giỏ** → tăng/giảm số lượng, kiểm tra tạm tính, có thể hủy giỏ đang lập.
5. **Thanh toán** → **Tiền mặt** (nhập số tiền khách đưa, tự tính tiền thối) hoặc **Khác (demo)**.
6. **Hoàn tất hóa đơn demo** khi ca mở, giỏ hợp lệ, hàng đủ và tiền mặt không thấp hơn tổng tiền.
7. Xem hóa đơn **POS-DEMO-xxxx**, các dòng hàng, tổng, phương thức và tiền thối; **Bán tiếp** hoặc xem lịch sử.
8. **Ca bán** → xem số hóa đơn và tổng giá trị bán demo → **Đóng ca**. Nếu giỏ chưa xử lý thì không đóng được.

### Giới hạn bắt buộc

- **Không cho thu ngân tự bán thuốc kê đơn (Rx)** qua demo: chưa có xác thực toa hoặc dược sĩ duyệt.
- **Không giả FEFO hoặc xuất theo lô** chỉ từ số tồn tổng. Tồn hiển thị đơn thuần là giá trị giả lập để thử luồng.
- Không in hóa đơn thật, xuất hóa đơn điện tử, thu tiền, kết nối ngân hàng/máy POS hoặc máy quét.
- Đơn Click & Collect của khách **chưa đồng bộ vào bán tại quầy** cho đến khi hoàn thiện backend, kho và phân quyền.

## Quản trị — giữ ở mức cơ bản

| Màn hình | Chức năng |
|---|---|
| **Tổng quan** | Số mặt hàng có thể bán, số hóa đơn, tổng giá trị bán, số mặt hàng tồn mẫu thấp |
| **Hàng hóa** | Xem danh mục, giá, tồn giả lập; tăng/giảm tồn mẫu để thử UI (trừ Rx) |
| **Bán hàng** | Xem các hóa đơn POS demo đã tạo trên thiết bị |

Không mở rộng sang mua hàng, kiểm kê đầy đủ, quản lý nhân sự, báo cáo nâng cao, hoặc duyệt thuốc kê đơn trong giai đoạn này.

## Cấu trúc source

- **domain/CounterRules.kt**: chính sách bán hàng, số lượng, phương thức, tiền thối, vai trò demo; thuần Kotlin.
- **ui/features/staff/StaffDemoStore.kt**: dữ liệu tồn mẫu, giỏ, ca bán, lịch sử hóa đơn trong RAM.
- **ui/features/staff/StaffWorkspace.kt**: màn vào khu vực nhân viên và chọn vai trò.
- **ui/features/staff/CashierScreen.kt**: các bước POS tại quầy.
- **ui/features/staff/AdminScreen.kt**: các trang quản trị căn bản.
- **ui/previews/StaffPreviews.kt**: ba bản xem trước Android Studio 390×844.

Không tạo SQL, SQLite/Room, bảng hay migration ở giai đoạn này.

## Kiểm tra hồi quy trọng tâm

- Không nhập đúng mã demo thì không vào được vai trò.
- Chưa mở ca, sản phẩm Rx, hàng chưa giá hoặc vượt tồn đều không thể bán.
- Số tiền mặt nhập nhỏ hơn tổng phải bị chặn thanh toán.
- Hóa đơn demo hợp lệ tạo một lần, kho mẫu giảm số lượng, giỏ trống sau khi hoàn thành.
- Quản trị và thu ngân xem cùng dữ liệu demo trong phiên.
- Không được đóng ca nếu đang có giỏ bán chưa hoàn tất.
- Quay về app khách hàng vẫn giữ nguyên các luồng người mua, không dùng chung số tồn giả lập với luồng khách.
