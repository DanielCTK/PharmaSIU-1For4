# Native Android library registry

Đây là **registry lựa chọn**, không có nghĩa tất cả đã được cài. Phiên bản phải được khóa tại `gradle/libs.versions.toml`, sau khi kiểm tra tương thích tại thời điểm triển khai.

| Thành phần | Khi dùng | Khi không dùng | Trạng thái trong PharmaSIU Mobile |
|---|---|---|---|
| Jetpack Compose + Material 3 | Nền UI native Kotlin mới, design tokens | Dự án XML cũ chỉ sửa tối thiểu | Đã cấu hình |
| Coil 3 | Ảnh sản phẩm qua URI/URL, cache và loading | Chỉ có vector/Canvas nội bộ | Dependency đã khai báo; chưa cần tải ảnh mạng |
| Lottie Compose | Animation JSON có asset gốc, đã kiểm tra licensing | Chỉ cần progress/transition đơn giản | Chưa thêm |
| Rive | State machine đồ họa tương tác phức tạp | Không có asset .riv hoặc kinh phí runtime | Chưa thêm |
| Vico | Dashboard chart nhiều series có dữ liệu thực | Catalog không có chart | Chưa thêm |
| Navigation Compose | Deep link/phức tạp/back stack nhiều flows | Prototype rất nhỏ | Chưa thêm; demo dùng state stack |

Tham khảo chính thức: https://developer.android.com/develop/ui/compose , https://coil-kt.github.io/coil/ , https://airbnb.io/lottie/#/android , https://rive.app/docs/runtimes/android , https://github.com/patrykandpatrick/vico

## Quy trình thêm thư viện
1. Xác định nhu cầu/thử nghiệm rủi ro; xem API và license.
2. Kiểm tra version, Compose BOM, AGP, Kotlin, Android API hỗ trợ.
3. Thêm qua version catalog, không gắn `+` hoặc `latest.release`.
4. Tạo một adapter `ui/media/` hoặc `ui/charts/` thay vì rải API thư viện.
5. Tối ưu fallback và reduced-motion; đo size/startup, build unit tests.
6. Cập nhật registry/README, bằng chứng QA và hạn chế.
