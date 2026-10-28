# Xem giao diện PharmaSIU không cần Android Emulator

PharmaSIU Mobile có **14 Compose Previews** tại:

`app/src/main/java/com/pharmasiu/mobile/ui/previews/PharmaScreenPreviews.kt`

## Cách xem trong Android Studio

1. Mở thư mục gốc repository bằng Android Studio và **Gradle Sync** lần đầu.
2. Dùng **Project** panel mở `app > src > main > java > com.pharmasiu.mobile > ui > previews > PharmaScreenPreviews.kt`.
3. Chọn tab **Split** hoặc **Design** ở góc phải editor; nếu chưa xuất hiện ảnh, nhấn **Build & Refresh** trong Preview.
4. Ở thanh công cụ Preview chọn **Focus** để xem một màn hình (nhẹ hơn), hoặc **Grid** để xem nhiều màn hình.
5. Có thể chuột phải ảnh xem trước → **Copy Image** để chia sẻ; bật **Interactive Mode** trên các preview có ô nhập liệu để xem thao tác đơn giản.

Các preview dùng dữ liệu mẫu, **không cần emulator, thiết bị Android hay backend**. Ảnh toa trong preview là trạng thái mô phỏng; không upload hay truy cập tệp thực. Kích thước mặc định 390×844dp; có bản 320×700dp và fontScale 1.5 để rà soát accessibility.

## Cần lưu ý

- Lần đầu mở có thể mất thời gian do Android Studio phải tải dependency/Gradle và biên dịch module. Sau đó Preview thường nhẹ hơn khởi động emulator.
- Preview để xem bố cục, màu, typography và một số tương tác cục bộ. Nó **không thay thế** kiểm thử navigation thật, quyền hệ thống, tải ảnh, hiệu năng hoặc các luồng backend.
- Nếu chỉ muốn xem một màn hình, bật **Focus Mode** để tránh render 14 bản cùng lúc.
- Nếu hiện *Unresolved reference* hoặc không render, kiểm tra Gradle Sync/build trước. Không cần tạo AVD chỉ để sửa lỗi sync.

Tài liệu Android chính thức: <https://developer.android.com/develop/ui/compose/tooling/previews>
