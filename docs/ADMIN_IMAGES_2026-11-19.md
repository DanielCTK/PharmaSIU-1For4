# Ảnh hàng hóa quản lý — 19/11/2026

## Thay đổi

- **Danh mục:** ưu tiên danh sách sản phẩm với ảnh nhỏ, tên, nhóm hiển thị, SKU, giá và tồn trên thiết bị. Biểu mẫu thêm/sửa chỉ mở khi chọn thao tác; đóng biểu mẫu không xóa nội dung đang nhập. Trình sửa có ảnh của mặt hàng được chọn.
- **Ảnh dùng chung:** ảnh mặc định lấy từ `ProductImageLoader` theo đúng SKU. Ảnh người quản lý chọn trên thiết bị tiếp tục dùng cơ chế ghi đè riêng của nhân viên trong phiên hiện tại; không bổ sung lưu ảnh lâu dài hoặc đồng bộ máy chủ.
- **Lô hàng:** nối `lot.productId` với đúng sản phẩm để hiện ảnh và mở xem trước. Biểu mẫu nhận lô có ảnh của sản phẩm đang chọn; trạng thái lô được hiển thị rõ.
- **Bộ nạp ảnh:** giới hạn cạnh dài 1.280 px, bộ nhớ đệm 8 MiB; ảnh thiếu dùng trạng thái trung tính thay vì minh họa dễ gây nhầm sản phẩm.

## Giới hạn ảnh và dữ liệu

Có 98 SKU dùng ảnh có sẵn theo ánh xạ mã hàng hiện tại. `OTC-015` hiển thị trạng thái thiếu ảnh vì tệp trước đó trùng ảnh `OTC-004`, sai biến thể sản phẩm. Không suy ra rằng toàn bộ bao bì đã được kiểm chứng độc lập.

Không thay đổi cơ sở dữ liệu, schema hoặc quy tắc domain. Quyền sửa ảnh, khóa Rx và các kiểm tra giá/lô được giữ nguyên. QA trong đợt này không ghi thay đổi tồn kho hoặc cách ly lô.

## Kiểm chứng

| Kiểm tra | Trạng thái |
| --- | --- |
| Build APK debug | PASS |
| Unit tests | PASS — 150/150 |
| Hàng hóa trên emulator | PASS — 7UP, Acemuc, Acnes dùng ảnh SKU; tìm `OTC-015` chỉ trả về Hapacol C với trạng thái thiếu ảnh |
| Lô hàng trên emulator | PASS — ảnh Pepsi thường/không đường đúng từng lô; mở preview giữ đúng tên và SKU |
| Biểu mẫu nhập lô | PASS — mở/thu gọn; chọn từ 7UP sang Acemuc cập nhật ảnh và mã, không lưu lô |
| Chữ lớn 130% | PASS — danh sách Hàng hóa/Lô hàng và preview không chồng chữ; đổi cấu hình khi preview mở không giữ cửa sổ cũ; đã khôi phục 100% |

Build và unit tests không thay thế kiểm tra trực quan trên thiết bị.

Máy kiểm tra: emulator `emulator-5554`, API 37.2, 1080 × 2400. Bằng chứng chụp từ app thật nằm trong `output/ui-redesign/` ở thư mục workspace cha: `PharmaSIU-admin-catalog.png`, `PharmaSIU-admin-lots.png`, `PharmaSIU-admin-lot-preview.png`. APK: `PharmaSIU-admin-images-debug.apk`. Log build và QA nội bộ: `build/admin-images-qa/` (không commit).
