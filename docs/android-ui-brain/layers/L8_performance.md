# L8 — Performance

- Tránh state bất biến để recomposition lan tỏa; giữ side effect khỏi composable renderer.
- Ảnh qua Coil với kích thước phù hợp; lazy lists có stable keys.
- Không cài dependencies animations/charts không sử dụng; kiểm tra APK size và startup trước/sau.
- Đo bằng emulator/devices được công bố, không suy diễn FPS. Các luồng xử lý giao dịch thuộc backend, UI không thể tự đảm bảo ACID.
- Không lưu ảnh toa vào log, clipboard, Preferences.
