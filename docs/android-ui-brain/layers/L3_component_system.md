# L3 — Component system

- Compose Material 3 làm primitive, tách `theme/`, `components/`, `features/`.
- Reuse: `PharmaTopBar`, `ProductTile`, `ProductArtwork`, `StatusPill`, `DemoDisclosure`; chưa cần tạo component library riêng qua Gradle.
- Mỗi component có parameter data/callback; không gọi API trong composable hiển thị.
- `LazyColumn` có stable item keys; shape/corners/icon consistent; `@Preview` dùng dữ liệu demo đúng nhãn.
- Buttons nhấn được 48 dp; state disabled phải có giải thích khi là bước quan trọng.
