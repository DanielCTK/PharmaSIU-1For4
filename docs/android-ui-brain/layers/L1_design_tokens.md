# L1 — Design tokens

- Ghi semantic pairs: primary/onPrimary, background/onBackground, success, warning, error; không dùng màu làm tín hiệu duy nhất.
- Hạn chế palette: Emerald `#064D3A`, Lime `#B9F48B`, warm neutral; Lime không mặc định dùng làm chữ trên trắng.
- Tạo `MaterialTheme(colorScheme, typography, shapes)`; tránh hardcode 30 màu trong màn hình.
- Spacing scale 4/8/12/16/24/32 dp; typography theo thứ bậc, kiểm thử tiếng Việt dài và fontScale 1.3–2.
- Ghi nguồn token và phương án dark theme; đừng tự tuyên bố dark mode có nếu chưa kiểm tra tương phản.
