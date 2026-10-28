# PharmaSIU Mobile — tối ưu giao diện và cảm giác thao tác

## Các vùng tràn ngang

- Banner Trang chủ, Danh mục và khối chào khách Tài khoản dùng **full-bleed chiều ngang**: không bo góc, không còn lề nền bao quanh mảng màu lớn.
- Chữ và CTA bên trong vẫn cách mép 20–24dp. Status bar / navigation bar hệ thống không bị tràn lên.
- Các điều khiển nhỏ (chip, input, thẻ hàng hóa) vẫn bo góc vừa phải để làm rõ vùng bấm.

## Thay đổi kỹ thuật

1. Home giữ một LazyVerticalStaggeredGrid và Catalog giữ một LazyVerticalGrid xuyên suốt, không lồng hai danh sách cuộn dọc.
2. Lazy items có ID ổn định và contentType theo loại item; Compose có thể tái sử dụng slot phù hợp.
3. Loại bỏ tìm chỉ số sản phẩm lặp lại O(N²). Chiều cao ảnh được lập một lần, không tạo danh sách trong mỗi cell.
4. rememberSaveableStateHolder, rememberLazyGridState, rememberLazyStaggeredGridState giữ lại trạng thái và vị trí cuộn giữa các tab.
5. derivedStateOf dùng cho dữ liệu dẫn xuất của giỏ, yêu thích, lịch sử và địa chỉ, giảm các phép lọc không cần thiết.
6. Crossfade alpha 130 ms giữa các route, 0 ms khi hệ thống tắt animations. Không tạo hiệu ứng chuyển động hay đổi kích thước nặng trong danh sách.
7. Coil 3 đã đảm nhận tải ảnh và cache khi catalog có URL hợp lệ. Mock data dùng artwork Compose offline. Không thêm thư viện ngoài chưa cần thiết.

## Giới hạn và tiêu chí kiểm tra

- Không cam kết 60/120 FPS trên mọi máy chỉ từ một build. Để xác nhận cần Perfetto, gfxinfo framestats, cold/warm startup và thử thiết bị yếu.
- Nếu số hàng tăng mạnh, phân trang từ backend, không đổ toàn bộ catalog vào RAM.
- Khi hoàn thiện backend cần cân nhắc Navigation Compose và ViewModel theo feature để xử lý deep-link, lifecycle và process death đầy đủ.
- Regression: Home cuộn rồi chuyển tab và quay lại; Catalog lọc/cuộn/mở PDP/Back; tài khoản, yêu thích, giỏ và checkout; màn hình 320/390dp, fontScale 1.5; tùy chọn tắt animation.

Tài liệu Android: https://developer.android.com/develop/ui/compose/performance/bestpractices và https://developer.android.com/develop/ui/compose/animation/quick-guide

**Thực đo (môi trường hiện tại):** Android Emulator API 28, 95/95 frames janky, median khoảng 125ms sau cuộn và đổi tab; host còn ~1,14GB RAM khả dụng trong lần đo. Đây là kết quả chưa đạt tiêu chí mượt, không được che giấu bằng việc build PASS. Cần benchmark lại trên máy ít tải hơn và thiết bị thật.
