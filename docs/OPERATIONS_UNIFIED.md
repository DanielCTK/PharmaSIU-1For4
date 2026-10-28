# PharmaSIU — Khu vận hành thống nhất

## Phạm vi

Gộp hai màn Quản trị và Nhân viên thành **một giao diện Vận hành nhà thuốc**. Vào từ Tài khoản → Vào khu vận hành. Cổng PIN demo vẫn hỗ trợ vai trò Nhân viên (1111) và Quản lý (2222), nhưng sau đó dùng cùng header, bottom navigation và các màn chính.

| Tab | Nhân viên | Quản lý |
|---|---|---|
| Tổng quan | Ca bán, thống kê ca, lối tắt bán hàng | Cùng giao diện |
| Bán hàng | Mở ca → chọn hàng → giỏ → thanh toán demo → hóa đơn | Cùng luồng |
| Hàng hóa | Xem sản phẩm, tìm, lọc tồn thấp/Rx | Được tăng/giảm tồn giả lập |
| Hóa đơn | Xem lịch sử hóa đơn và chi tiết | Cùng lịch sử |

Phần quản trị giữ mức căn bản, không thêm các màn ERP, nhân sự, mua hàng hoặc báo cáo phức tạp. Luồng khách hàng hoàn toàn tách riêng.

## Giao diện

- Hero Emerald full-bleed không bo góc, Lime làm điểm nhấn, khoảng đệm an toàn.
- Header đồng nhất PharmaSIU, hiển thị vai trò và nút thoát.
- Tổng quan có 4 chỉ số nhỏ, thao tác nhanh, trạng thái và nút mở/đóng ca.
- Bán hàng có thanh tìm sản phẩm, hình minh họa theo đúng adapter hình ảnh của gian hàng khách, nút thêm và giỏ. Chạm hình trong danh sách/giỏ để phóng lớn, có tên và giá sản phẩm. Ảnh chính thức sẽ hiện nếu dữ liệu danh mục có HTTPS imageUrl hợp lệ.
- Màn kết thúc bán hiển thị phiếu hóa đơn có số tiền, phương thức, số khách đưa và tiền thối.
- Hàng hóa phân loại Rx / tồn thấp, hiển thị thumbnail và mở ảnh lớn; quản lý được chỉnh tồn demo, nhân viên chỉ xem.
- Lịch sử có danh sách gọn và bottom sheet chi tiết hóa đơn.

## Ràng buộc nghiệp vụ không thay đổi

- Rx vẫn không thể được thu ngân tự bán trong bản demo.
- Không cho bán khi chưa mở ca, không đủ tiền mặt, vượt tồn mẫu hoặc không có giá hợp lệ.
- Sau hóa đơn thành công, tồn mock trừ theo số lượng, giỏ được làm trống; quản lý nhìn được cùng dữ liệu.
- Không đóng ca khi giỏ còn hàng.
- Không tạo SQL, SQLite, Room, DAO hoặc đồng bộ backend.
- PIN demo công khai chỉ để thử UI, không phải xác thực bảo mật thật.
- Hóa đơn, doanh số, tồn kho đều minh họa và được giữ trong RAM; không có giao dịch tiền thật hoặc FEFO theo lô.

## Kiểm tra quan trọng

1. Hai vai trò đều mở chung một giao diện với 4 tab.
2. Nhân viên không có nút sửa tồn; quản lý có nút điều chỉnh tồn mock.
3. Mở ca → thêm một hàng OTC → thanh toán → phiếu bán → lịch sử; số tiền và tồn mẫu phải khớp.
4. Mã PIN sai không truy cập được; Rx bị khóa; không cho bán vượt tồn hoặc thanh toán thiếu tiền.
5. Bấm Back trên giỏ/thanh toán quay đúng bước; chuyển tab không làm mất giỏ bán.
6. Thoát về khu khách hàng không ảnh hưởng đơn của khách.

## Cấu trúc

- OperationsHub.kt: shell và module Tổng quan/Hàng hóa/Hóa đơn.
- StaffWorkspace.kt: một cổng đăng nhập demo theo vai trò.
- CashierScreen.kt: quy trình bán hàng, thanh toán, hóa đơn.
- StaffDemoStore.kt: trạng thái trong RAM dùng chung.
- CounterRules.kt: quy tắc POS độc lập giao diện.
- StaffPreviews.kt: xem trước cổng và khu vận hành theo vai trò mà không cần emulator.

**Hình ảnh trong bản mock:** Hiện 30 sản phẩm chỉ có hình minh họa offline, chưa có ảnh chụp thực tế hoặc imageUrl. Không coi các hình đó là ảnh bao bì thật; bán hàng và quản lý dùng cùng MedicineMedia của gian hàng khách, có fallback khi tải ảnh thất bại.

## Thêm mặt hàng, nhập tồn mẫu, thay ảnh (bổ sung 28/10/2026)

- Đăng nhập vai trò **Quản lý** (PIN demo `2222`) → tab **Hàng hóa** → **Thêm hàng**: nhập tên, nhóm (Mỹ phẩm, Thực phẩm, Thực phẩm bổ sung, Chăm sóc, Thiết bị, Vật tư y tế), đơn vị, giá VNĐ và tồn đầu. Có thể chọn ảnh qua trình chọn hình của Android. Sau khi lưu, mặt hàng lập tức tìm được ở tab Bán hàng nội bộ.
- Với mặt hàng có sẵn, chọn **Đổi ảnh** để thay thumbnail; chạm thumbnail để xem lớn và có thể thay/bỏ ảnh. Ảnh được chọn chỉ là nội dung thiết bị dùng trong phiên app, không tải lên máy chủ.
- Chọn **Nhập thêm** để nhập số lượng dương (1–999), cộng vào tồn demo; tổng tồn không được vượt 999. Nút +/- cũ vẫn giữ để thử các thay đổi nhỏ.
- Nhân viên bán hàng chỉ xem hình/tồn, không thấy nút tạo hàng, thay ảnh hoặc cộng tồn; quản lý có đủ nút theo vai trò UI demo. Đây chưa phải xác thực/phân quyền backend thật.
- Giữ an toàn: không tạo thuốc kê đơn bằng form nhanh; sản phẩm Rx vẫn bị khóa ở POS; không lưu phiếu mua hàng, nhà cung cấp, lô, hạn FEFO, hóa đơn nhập hoặc SQL. Danh mục khách hàng không bị thay đổi.
- **Dữ liệu và ảnh mới mất khi tiến trình app bị khởi tạo lại.** Muốn lưu dài hạn, cần làm cùng hệ thống dữ liệu ở giai đoạn tiếp theo. Hình đã chọn từ điện thoại không phải hình xác minh bao bì/chứng nhận thuốc.