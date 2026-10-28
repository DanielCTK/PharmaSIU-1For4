# PharmaSIU Mobile

Ứng dụng Android dành cho khách hàng nhà thuốc PharmaSIU, tập trung vào **tra cứu sản phẩm và gửi yêu cầu đặt trước – nhận tại quầy (Click & Collect)**. Giao diện xây dựng bằng **Kotlin + Jetpack Compose + Material 3**, phong cách Emerald/Lime. Mã nguồn tổ chức theo feature và layer để nhiều thành viên có thể cộng tác mà không phải sửa chung một màn hình.

> **Trạng thái: ứng dụng trải nghiệm tương tác, dữ liệu mock (v0.2).** Có onboarding, đăng nhập/đăng ký demo, hồ sơ, sổ địa chỉ, danh mục 30 mặt hàng, tìm kiếm, yêu thích, giỏ, toa, checkout và lịch sử đơn hàng demo. **Chưa kết nối API nhà thuốc, chưa tạo đơn thật, chưa gửi ảnh toa, chưa xác nhận tồn, chưa thanh toán hoặc phê duyệt dược sĩ trên máy chủ.** Giá là dữ liệu ví dụ, không phải giá chào bán.

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

### Chỉ muốn xem UI, không muốn bật Emulator?

Mở [`PharmaScreenPreviews.kt`](app/src/main/java/com/pharmasiu/mobile/ui/previews/PharmaScreenPreviews.kt) hoặc [`ShopFlowPreviews.kt`](app/src/main/java/com/pharmasiu/mobile/ui/previews/ShopFlowPreviews.kt), chọn **Split** / **Design** → **Build & Refresh**. Có **31 bản Compose Preview** cho màn hình thương mại, đăng nhập, tài khoản, lịch sử, địa chỉ, màn hình nhỏ và chữ lớn. Chọn **Focus Mode** để nhẹ máy hơn. Xem [hướng dẫn Compose Preview không cần emulator](docs/COMPOSE_PREVIEW.md).

### Sản phẩm nổi bật — lưới so le hai cột

Trang chủ có **LazyVerticalStaggeredGrid** hai cột như luồng khám phá sản phẩm của ứng dụng thương mại điện tử: ảnh lớn với chiều cao so le, thẻ có tên/giá tham khảo và nút yêu thích; cuộn tiếp tục để xem nhiều sản phẩm. Nhấn card mở chi tiết; các chip **Dành cho bạn / Mỹ phẩm / Thực phẩm / Thiết bị / Chăm sóc** lọc ngay trong mục nổi bật. Không dựng số lượt bán, % giảm giá hay tồn kho giả. Hình sản phẩm là minh họa offline tự thiết kế, không phải ảnh bao bì thuốc thật.

Nếu muốn xem riêng phần này mà không khởi động emulator, mở [`FeaturedShowcasePreviews.kt`](app/src/main/java/com/pharmasiu/mobile/ui/previews/FeaturedShowcasePreviews.kt) để chọn preview **390dp** hoặc **320dp**. Các ảnh minh họa dựng trực tiếp bằng Compose; khi có URL ảnh sản phẩm thực được xác minh, `MedicineMedia` sẽ dùng Coil.
### Thiết kế tràn ngang và tối ưu thao tác

Các banner Emerald của Trang chủ, Danh mục và Tài khoản hiện **tràn hết bề ngang màn hình**, không bo góc/để lề như thẻ sản phẩm. Danh sách có stable keys, content types, state-save cho scroll khi chuyển tab; dùng Crossfade 130ms khi hệ thống cho phép animation, các tính toán cart/favorites/history được tối ưu bằng derivedStateOf. Mã tiếp tục dựa trên Jetpack Compose + Material 3 + Coil, không thêm thư viện chỉ để có thêm hiệu ứng. Xem [chi tiết và giới hạn kiểm tra hiệu năng](docs/UX_PERFORMANCE.md).

Ảnh chụp màn hình debug: [Trang chủ](docs/screenshots/home-full-bleed.png) · [Danh mục](docs/screenshots/catalog-full-bleed.png) · [Tài khoản](docs/screenshots/account-full-bleed.png).
### Danh mục sản phẩm — bố cục Emerald nâng cấp

Trang **Danh mục** có banner giới thiệu nhiều loại hàng với hình minh họa vector tại vùng góc phải (không quảng cáo giả), thanh tìm kiếm Material3, chip nhóm hàng, thanh công cụ tóm tắt **số kết quả + sắp xếp**, và lưới sản phẩm hai cột với artwork riêng theo ngành hàng, nhãn loại và nút yêu thích. Toàn bộ màn hình dùng **một vùng cuộn**, tránh trang trên cố định chiếm quá nhiều diện tích. Đã kiểm tra trực tiếp trên emulator kích thước 1080×2400. [Ảnh chụp danh mục Emerald](docs/screenshots/catalog-emerald.png).

Để xem riêng màn hình mới mà không cần chạy app, mở [`CatalogDesignPreviews.kt`](app/src/main/java/com/pharmasiu/mobile/ui/previews/CatalogDesignPreviews.kt): có bản 390dp, 320dp và chữ lớn 1,5×.
### Mở nhanh trên máy đã cấu hình

Máy Windows hiện tại đã có Android Studio, SDK API 36 và JDK kèm theo Studio. `local.properties` được cấu hình riêng tại `C:\PharmaSIU\PharmaSIU-1For4` và không đưa lên Git. Chỉ mở thư mục gốc bằng **File → Open**; Android Studio tự thực hiện **Gradle Sync**, không cần mở terminal. Khi sync xong, xem UI tại `ui/previews/PharmaScreenPreviews.kt` bằng **Split/Design** mà không cần emulator. **Lần mở đầu có thể phải tải Gradle và thư viện trên mạng; đã build APK debug và chạy JUnit thành công tại máy Windows này (việc xem trên thiết bị vẫn cần xác minh).**

### Luồng đặt hàng demo thành công

Người dùng thêm sản phẩm → giỏ hàng → chọn toa nếu có thuốc kê đơn → nhập thông tin liên hệ & điểm nhận mẫu → **Đặt hàng** → màn **Đặt hàng thành công!**. Đơn nhận mã `DEMO-xxxx`, xuất hiện trong tab **Đơn hàng**; [xem ảnh chụp thành công](docs/screenshots/order-success-demo.png). có thể xem chi tiết, hủy hoặc đặt lại. Giỏ được xóa sau khi hoàn tất. Không quay lại checkout trống khi bấm Back từ màn thành công.

Đây là thao tác **lưu đơn hàng thành công trong RAM của ứng dụng để trải nghiệm**, không phải đơn đã gửi/được nhà thuốc xác nhận. Không có thanh toán, giữ hàng, duyệt toa hay API quản lý. Phần quy trình quản lý sẽ triển khai riêng khi tích hợp backend.
### Khu vực nhân viên bán hàng và quản trị (mock)

Từ tab **Tài khoản → Dành cho nhân viên → Bán hàng / Quản trị (demo)**, chọn một trong hai vai trò. Nhân viên bán hàng sử dụng mã **1111**, quản trị dùng mã **2222**; đây là PIN công khai để thử giao diện, hoàn toàn không phải xác thực bảo mật thật.

**Bán hàng:** mở ca → tìm/chọn sản phẩm không kê đơn → giỏ → tiền mặt hoặc phương thức khác demo → kiểm tra thanh toán → hoàn tất hóa đơn POS-DEMO → bán tiếp/lịch sử → đóng ca. Tồn giả lập giảm khi lập hóa đơn hợp lệ; thuốc kê đơn bị khóa vì chưa tích hợp dược sĩ duyệt.

**Quản trị cơ bản:** tổng quan, danh mục và điều chỉnh tồn mẫu, danh sách hóa đơn POS trong phiên. Không phát triển quản lý phức tạp. Tất cả các số liệu chỉ nằm trong RAM, không kết nối đến dữ liệu khách, kho/lô FEFO, SQL hay backend. Xem [đặc tả staff flow](docs/STAFF_WORKFLOWS.md). [Ảnh giao diện bán tại quầy](docs/screenshots/staff-counter-demo.png), [hóa đơn demo](docs/screenshots/staff-receipt-demo.png) và [trang quản trị](docs/screenshots/staff-admin-demo.png). Có 3 [Compose Preview nhân viên](app/src/main/java/com/pharmasiu/mobile/ui/previews/StaffPreviews.kt) không cần emulator.
## Những luồng đã có trong bản demo

| Màn hình | Nội dung |
|---|---|
| Home | Editorial hero, danh mục, sản phẩm minh họa, thông báo demo |
| Catalog | Tìm kiếm theo tên, lọc theo nhóm, danh sách sản phẩm |
| Product | Mô tả, quy cách, giá ví dụ hoặc yêu cầu duyệt Rx, cảnh báo tồn kho |
| Cart | Thêm/bớt số lượng, tạm tính minh họa, nhận diện mặt hàng Rx |
| Prescription | Chọn ảnh trong thiết bị bằng trình chọn tệp Android, **chưa upload** |
| Checkout | Nhập thông tin liên hệ, chọn điểm nhận mẫu, nhấn **Đặt hàng** để hoàn tất demo |
| Thành công | Màn **Đặt hàng thành công!**, mã `DEMO-xxxx`, xem đơn hoặc tiếp tục mua sắm; chỉ thành công ở trạng thái **lưu đơn demo trên máy** |
| Onboarding | Ba màn giới thiệu, Bỏ qua/Tiếp tục, lưu cờ đã xem |
| Auth | Đăng nhập, đăng ký, khôi phục mật khẩu demo trong phiên RAM |
| Account | Hồ sơ cá nhân, cập nhật thông tin, đổi mật khẩu và sổ địa chỉ liên hệ |
| Favorites | Lưu/bỏ yêu thích từ catalog/PDP, tab danh sách sản phẩm |
| Categories | Thuốc, mỹ phẩm, thực phẩm, thực phẩm bổ sung, thiết bị, vật tư, chăm sóc |
| Search & sort | Tìm không dấu, lọc theo nhóm, sắp tên và giá |
| Demo orders | Lịch sử **Đơn hàng** với trạng thái **Đặt hàng thành công**, chi tiết, hủy đơn demo và đặt lại sản phẩm |

**Quy tắc an toàn:** `REQUEST_RECEIVED` không phải `HELD`; toa gửi lên không phải `RX_APPROVED`; `HELD` không phải `READY_FOR_PICKUP`. Chính sách xuất lô/hạn FEFO, tính giá cuối cùng và phân quyền phải được thực thi bởi backend; mobile không tự tạo giả xác nhận.

**Tài khoản demo:** `0900000000` / `demo1234`. Chỉ dùng mật khẩu mẫu, tuyệt đối không nhập mật khẩu cá nhân thực tế. Các thông tin tài khoản, yêu thích và yêu cầu mô phỏng tồn tại trong RAM rồi bị xóa khi tiến trình kết thúc; chỉ cờ đã xem onboarding được lưu. Xem [đặc tả luồng đầy đủ](docs/MOBILE_CUSTOMER_FLOWS.md).

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
│       │           ├── components/            # Shared UI + Coil 3 image adapter
│       │           ├── previews/              # 31 Compose Previews không cần Emulator
│       │           └── features/
│       │               ├── onboarding/
│       │               ├── account/
│       │               ├── community/          # Yêu thích, yêu cầu mẫu
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
│   ├── COMPOSE_PREVIEW.md
│   ├── MOBILE_CUSTOMER_FLOWS.md
│   ├── ARCHITECTURE.md
│   ├── TEAM_GUIDE.md
│   └── QA_REPORT.md
├── gradle/libs.versions.toml                   # Phiên bản dependency tập trung
├── .github/workflows/android.yml              # CI build & unit tests
└── AGENTS.md                                   # Hướng dẫn coding agent / contributor
```

Đây là **một Gradle app module, code chia package theo feature/layer**. Tránh tạo nhiều module Gradle nhỏ khi chưa cần thiết. Khi tích hợp API thật, có thể tách `core:model`, `core:network`, `feature:catalog`, `feature:checkout` mà không đổi hợp đồng domain.

## Làm việc nhóm

**Rà soát giao diện và thư viện:** xem [UI audit 28/10/2026](docs/UI_AUDIT_2026-10-28.md), bao gồm cả các trường hợp chưa được kiểm thử trên emulator.

Đọc [TEAM_GUIDE.md](docs/TEAM_GUIDE.md) trước khi phân chia đầu việc, [ARCHITECTURE.md](docs/ARCHITECTURE.md) trước khi thay state/API, và [AGENTS.md](AGENTS.md) khi dùng coding agent. Mỗi feature có package riêng; pull request phải nêu luồng kiểm thử, screenshot Android nếu sửa UI, và không thay đổi schema/domain ngầm.

**Android UI Brain:** các router, registry, 9 layer và quy tắc quality gate nằm ở [docs/android-ui-brain](docs/android-ui-brain/README.md). Thư mục này là bản đồng hành nằm trong Git; bản extension gốc đồng bộ với UI UX Brain v4.1.

## Công nghệ & giới hạn

- Kotlin, Android Gradle Plugin, Compose Compiler và Compose BOM khóa tại [version catalog](gradle/libs.versions.toml).
- Material 3 / Compose cho UI; **Coil 3.3.0 + OkHttp đã tích hợp qua `MedicineMedia`** để tải ảnh HTTPS có fallback artwork nội bộ và cache khi có URL từ backend. Demo vẫn hoạt động hoàn toàn offline. **Lottie, Rive và Vico không được thêm** vì hiện chưa có asset tương tác/biểu đồ thực cần thiết.
- Min SDK 26; compile SDK 36; định hướng tối ưu cho điện thoại, dùng semantic labels và màn hình có thể cuộn.
- Để thành ứng dụng thật còn cần: API backend, authentication, quyền truy cập toa, quản lý attachment an toàn, luồng dược sĩ, availability/reservation TTL, đồng bộ giá, test device, chính sách bảo mật, release signing.

Giấy phép sử dụng: chưa lựa chọn license mã nguồn công khai. Mặc định **không suy diễn rằng repository cho phép tái sử dụng thương mại**.
