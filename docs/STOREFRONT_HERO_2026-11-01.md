# Hình nền PharmaSIU cho các banner xanh

Ngày cập nhật: 01/11/2026.

## Phạm vi chỉnh giao diện

Ảnh nhà thuốc PharmaSIU do người dùng cung cấp được đưa vào ứng dụng dưới dạng tệp WebP:
`app/src/main/res/drawable-nodpi/pharmasiu_storefront.webp`.

Drawable trung tâm:
`app/src/main/res/drawable/pharmasiu_storefront_banner.xml`.

Các drawable dùng trong phần banner/khối hero chuyển sang lớp nền ảnh này:

- `catalog_v2_hero.xml` — trang chủ và trang danh mục.
- `customer_account_hero.xml` — phần đầu trang tài khoản / giới thiệu khách.
- `auth_hero.xml` — khu vực tiêu đề xác thực; `res/layout/auth_unified.xml` ẩn vòng tròn xanh che ảnh.
- `bg_hero.xml` — tiêu đề các trang đơn hàng, quản trị và POS.

Nền ảnh dùng lớp phủ gradient tối vừa phải để giữ tương phản chữ trắng; hình tròn trang trí cũ trên banner xác thực được ẩn nhằm không che ảnh. Phần hình sản phẩm, mặt hàng, ảnh media, giỏ hàng, card sản phẩm, danh mục, SQLite và các trình xử lý sự kiện **không bị chỉnh sửa**.

Ảnh được tối ưu dung lượng cho Android và chỉ sử dụng làm nền banner; bản ảnh gốc có kích thước lớn hơn. Khi cần hình sắc nét hơn, thay riêng `drawable-nodpi/pharmasiu_storefront.webp` bằng tệp WebP cùng tên nhưng chất lượng cao hơn.

## Cách kiểm tra

```powershell
cd C:\PharmaSIU\PharmaSIU-1For4
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline
```

Sau đó mở ứng dụng để xem Home, Danh mục, Tài khoản, Đơn hàng và POS.

Bản sao drawable gốc trước khi chỉnh được lưu tại:
`C:\UI UX Brain\pharmasiu_storefront_banner_backup_20261101`.

Không thay đổi applicationId, schema, dữ liệu mẫu hay giao dịch người dùng.
