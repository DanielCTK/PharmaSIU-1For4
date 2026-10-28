# PharmaSIU Mobile

Ứng dụng Android dành cho khách hàng nhà thuốc PharmaSIU, tập trung vào **tra cứu sản phẩm và gửi yêu cầu đặt trước – nhận tại quầy (Click & Collect)**. Giao diện xây dựng bằng **Kotlin + Jetpack Compose + Material 3**, phong cách Emerald/Lime. Mã nguồn tổ chức theo feature và layer để nhiều thành viên có thể cộng tác mà không phải sửa chung một màn hình.

> **Trạng thái: Android UI prototype / demo cục bộ.** Ứng dụng hiện dùng danh mục minh họa, giỏ hàng trong bộ nhớ và bản xem trước yêu cầu. **Chưa kết nối API nhà thuốc, chưa tạo đơn thật, chưa gửi ảnh toa, chưa xác nhận tồn, chưa thanh toán và chưa có quy trình phê duyệt dược sĩ trên máy chủ.** Giá hiển thị là dữ liệu ví dụ, không phải giá chào bán.

## Chạy trong Android Studio

1. Cài Android Studio có Android SDK **API 36** và JDK **17+** (Android Studio bundled JBR được khuyến nghị).
2. Clone repo `git clone https://github.com/DanielCTK/PharmaSIU-1For4.git`.
3. Mở **thư mục gốc repo** (chứa `settings.gradle.kts`) trong Android Studio; chọn **Trust Project** nếu đồng ý, đợi **Gradle Sync**.
4. Chọn cấu hình chạy **app** và emulator/thiết bị Android **8.0 / API 26 trở lên**.
5. Nhấn **Run**. App hiện chưa yêu cầu API key hay tài khoản.

Chạy kiểm tra từ terminal:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Trên Windows thay `./gradlew` bằng `gradlew.bat`. APK debug được tạo tại `app/build/outputs/apk/debug/` khi build thành công. **Không commit** `local.properties`, file ký ứng dụng hay thông tin khách hàng thật.

## Những luồng đã có trong bản demo

| Màn hình | Nội dung |
|---|---|
| Home | Editorial hero, danh mục, sản phẩm minh họa, thông báo demo |
| Catalog | Tìm kiếm theo tên, lọc theo nhóm, danh sách sản phẩm |
| Product | Mô tả, quy cách, giá ví dụ hoặc yêu cầu duyệt Rx, cảnh báo tồn kho |
| Cart | Thêm/bớt số lượng, tạm tính minh họa, nhận diện mặt hàng Rx |
| Prescription | Chọn ảnh trong thiết bị bằng trình chọn tệp Android, **chưa upload** |
| Checkout | Nhập thông tin liên hệ tạm thời, xem tóm tắt, tạo bản xem trước |
| Preview | Mã `DEMO-xxxx`, mô tả luồng tiếp nhận–duyệt–giữ–soạn–nhận **chưa được thực thi** |

**Quy tắc an toàn:** `REQUEST_RECEIVED` không phải `HELD`; toa gửi lên không phải `RX_APPROVED`; `HELD` không phải `READY_FOR_PICKUP`. Chính sách xuất lô/hạn FEFO, tính giá cuối cùng và phân quyền phải được thực thi bởi backend; mobile không tự tạo giả xác nhận.

## Cấu trúc dự án

```text
PharmaSIU-1For4/
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/pharmasiu/mobile/
│       │       ├── MainActivity.kt
│       │       ├── data/
│       │       │   ├── model/                 # Các kiểu dữ liệu công khai
│       │       │   └── repository/            # Demo catalog, sau này API
│       │       ├── domain/                    # Cart rules, order state machine
│       │       └── ui/
│       │           ├── PharmaSIUApp.kt        # Điều hướng & demo state
│       │           ├── theme/                 # Palette, type, shapes
│       │           ├── components/            # Shared UI primitives
│       │           └── features/
│       │               ├── home/
│       │               ├── catalog/
│       │               ├── product/
│       │               ├── cart/
│       │               ├── prescription/
│       │               ├── checkout/
│       │               └── orders/
│       └── test/java/com/pharmasiu/mobile/domain/
├── docs/
│   ├── android-ui-brain/                      # Bản quy tắc native có thể mang theo repo
│   ├── ARCHITECTURE.md
│   ├── TEAM_GUIDE.md
│   └── QA_REPORT.md
├── gradle/libs.versions.toml                   # Phiên bản dependency tập trung
├── .github/workflows/android.yml              # CI build & unit tests
└── AGENTS.md                                   # Hướng dẫn coding agent / contributor
```

Đây là **một Gradle app module, code chia package theo feature/layer**. Tránh tạo nhiều module Gradle nhỏ khi chưa cần thiết. Khi tích hợp API thật, có thể tách `core:model`, `core:network`, `feature:catalog`, `feature:checkout` mà không đổi hợp đồng domain.

## Làm việc nhóm

Đọc [TEAM_GUIDE.md](docs/TEAM_GUIDE.md) trước khi phân chia đầu việc, [ARCHITECTURE.md](docs/ARCHITECTURE.md) trước khi thay state/API, và [AGENTS.md](AGENTS.md) khi dùng coding agent. Mỗi feature có package riêng; pull request phải nêu luồng kiểm thử, screenshot Android nếu sửa UI, và không thay đổi schema/domain ngầm.

**Android UI Brain:** các router, registry, 9 layer và quy tắc quality gate nằm ở [docs/android-ui-brain](docs/android-ui-brain/README.md). Thư mục này là bản đồng hành nằm trong Git; bản extension gốc đồng bộ với UI UX Brain v4.1.

## Công nghệ & giới hạn

- Kotlin, Android Gradle Plugin, Compose Compiler và Compose BOM khóa tại [version catalog](gradle/libs.versions.toml).
- Material 3 / Compose cho UI; Coil 3 được khai báo để hỗ trợ hình ảnh trong các bước sau. Hiện artwork dựng nội bộ; **Lottie, Rive và Vico được đánh giá trong registry, chưa thêm dependencies không sử dụng**.
- Min SDK 26; compile SDK 36; định hướng tối ưu cho điện thoại, dùng semantic labels và màn hình có thể cuộn.
- Để thành ứng dụng thật còn cần: API backend, authentication, quyền truy cập toa, quản lý attachment an toàn, luồng dược sĩ, availability/reservation TTL, đồng bộ giá, test device, chính sách bảo mật, release signing.

Giấy phép sử dụng: chưa lựa chọn license mã nguồn công khai. Mặc định **không suy diễn rằng repository cho phép tái sử dụng thương mại**.
