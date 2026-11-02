# PHARMASIU — CẤU TRÚC MÃ NGUỒN HIỆN HÀNH

**Ngày chốt:** 30/10/2026  
**Phạm vi đợt này:** DỌN CẤU TRÚC và PHÂN TÁCH MÃ; **không** thêm tính năng, không thay DB/schema, không tự đánh dấu nghiệp vụ đã hoàn thiện.  
**Nguồn thao tác:** \`C:\PharmaSIU\PharmaSIU-1For4\` (working tree của dự án chính).

## 1. Quyết định kiến trúc

PharmaSIU là **một ứng dụng Android native**, một module Gradle \`:app\`, sử dụng **Java 17 + Android XML Views/Material Components**. Không còn module APK preview hay mã Kotlin trong source Android đang build.

**Package / applicationId duy nhất:** \`com.pharmasiu.mobile\`.

**Nguyên tắc phân lớp:**

\`\`\`text
MainActivity + Navigation
        |
        v
UI theo feature: auth / customer / staff  + UI components dùng chung
        |
        v
UiHost / DemoState (facade trạng thái hiện hữu; cần tiếp tục thay thế về sau)
        |
        v
PharmaRepository (facade dữ liệu)
  |---- AccountQueries
  |---- CatalogQueries
  |---- OrderQueries (bao gồm ghi POS)
        |
        v
PharmaDatabase + SQLite (6 bảng, khóa ngoại / trigger)
\`\`\`

\`domain/\` chứa quy tắc Java không phụ thuộc View. \`src/test/\` giữ các phép thử và lớp tương thích cũ, không đóng gói vào APK.

## 2. Cấu trúc thư mục được giữ

\`\`\`text
PharmaSIU-1For4/
├── app/                               # MODULE ANDROID DUY NHẤT
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/database/
│       │   │   └── pharmasiu_schema.sqlite3
│       │   ├── res/
│       │   │   ├── layout/            # XML giao diện sử dụng thực
│       │   │   ├── drawable/          # hình nền, icon và token
│       │   │   └── values/            # string, theme, color, dimension
│       │   └── java/com/pharmasiu/mobile/
│       │       ├── MainActivity.java
│       │       ├── navigation/
│       │       │   └── PharmaBottomBar.java
│       │       ├── core/
│       │       │   ├── UiHost.java
│       │       │   ├── DemoState.java   # API tương thích tạm; KHÔNG phải DB
│       │       │   └── state/
│       │       │       ├── CustomerAccountStore.java
│       │       │       └── InventoryStore.java
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   ├── PharmaDatabase.java
│       │       │   │   ├── PharmaRepository.java
│       │       │   │   ├── AccountQueries.java
│       │       │   │   ├── CatalogQueries.java
│       │       │   │   ├── OrderQueries.java
│       │       │   │   └── PasswordCodec.java
│       │       │   └── model/         # model thực tế còn sử dụng
│       │       ├── domain/            # luật xử lý thuần Java
│       │       └── ui/
│       │           ├── AuthScreen.java   # facade màn xác thực
│       │           ├── auth/              # form + controller
│       │           ├── design/            # PharmaUi: Material shared UI
│       │           ├── customer/
│       │           │   ├── catalog/       # duyệt / tìm kiếm / chi tiết
│       │           │   ├── orders/        # giỏ / checkout / lịch sử
│       │           │   └── account/       # hồ sơ / địa chỉ / yêu thích
│       │           └── staff/
│       │               ├── pos/           # bán tại quầy / ca / hóa đơn
│       │               ├── admin/         # bảng điều khiển / lô / kiểm kê
│       │               └── media/         # preview/giải mã hình
│       └── test/java/com/pharmasiu/mobile/
│           ├── domain/                    # test và các hỗ trợ test legacy
│           ├── legacy/                    # LegacyDomainRules KHÔNG vào APK
│           └── ...                        # các JUnit còn lại
├── database/
│   ├── PharmaSIU_Main.sqlite3            # mẫu Navicat: 15 sản phẩm + 15 lô
│   ├── PharmaSIU_Demo.sqlite3            # 93 bản ghi demo phục vụ thử SQL
│   ├── sqlite/                            # DDL, seed, views, guards
│   ├── postgresql/                        # phương án server
│   └── tools/                             # kiểm thử/xuất dữ liệu
├── docs/
│   └── PROJECT_STRUCTURE.md   # file đang đọc
└── settings.gradle                         # chỉ include ':app'
\`\`\`

**Không tạo thêm package/folder rỗng.** Adapter mới chỉ được tạo trong feature tương ứng (ví dụ \`ui/customer/catalog/\`) **khi thật sự có RecyclerView/ListAdapter**. Không sinh một nhánh \`features/\` mới chỉ để chứa thư mục chưa dùng.

## 3. Những gì đã xóa hoặc di chuyển

- Gỡ khỏi emulator **APK preview cũ** \`com.pharmasiu.mobile.java\`. Giữ nguyên APK chính \`com.pharmasiu.mobile\`; thao tác gỡ dùng \`pm uninstall -k\` (giữ dữ liệu package cũ), đồng thời sao lưu file shared preferences của preview.
- Loại bỏ cây \`ui/features/\` gồm 11 thư mục trống; đã kiểm tra không có file thật.
- Loại bỏ các folder trống \`ui/components\`, \`ui/previews\`, \`ui/theme\`, \`legacy\`, \`data/repository\` (sau khi dời lớp).
- Dời \`LegacyDomainRules\` và các lớp \`MockLot...\`, \`MockPrice...\`, \`DemoLot...\` **sang \`src/test/java\`**, giữ test, không đóng gói runtime.
- Đưa các lớp không có tham chiếu (nhóm mock data model, repository demo cũ, một số state/access cũ) **ra bản backup ngoài repo**, thay vì xóa vĩnh viễn.
- Chia \`PharmaRepository\` từ một file lớn thành facade + \`AccountQueries\` + \`CatalogQueries\` + \`OrderQueries\`; chữ ký công khai và đường gọi hiện hữu vẫn giữ nguyên.

**Bản sao lưu trước sửa** đặt ngoài repository tại:
\`C:\UI UX Brain\pharmasiu_structure_backup_20261030\`.

**Vì sao KHÔNG xóa DemoState?** Hiện nhiều màn và test còn gọi API của lớp đó. Xóa ngay sẽ phá ứng dụng; trong bước cấu trúc chỉ ghi nhận đây là **facade tạm có nợ kỹ thuật**, không phải thiết kế chuẩn hoàn tất. Phần constructor có fixture chỉ dùng cho test cũ còn phải được chuyển hoàn toàn khỏi production ở đợt tiếp theo, trước khi có thể khẳng định mã runtime không chứa mock.

## 4. Chia ownership để dễ làm việc nhóm

| Nhóm | Phạm vi sửa chính | Không tự ý sửa |
|---|---|---|
| App Integration | \`MainActivity\`, \`UiHost\`, \`navigation\` | Schema, domain, dữ liệu của người dùng |
| Auth | \`ui/auth\` và hợp đồng \`data/local/AccountQueries\` | Quyền tài khoản bằng kiểm tra UI |
| Customer Catalog | \`ui/customer/catalog\` | POS, Admin |
| Customer Order | \`ui/customer/orders\` | Cập nhật tồn ngoài transaction |
| Customer Account | \`ui/customer/account\` | Schema thêm bảng trái thiết kế |
| Staff POS | \`ui/staff/pos\` | Bỏ kiểm tra Rx/FEFO |
| Admin | \`ui/staff/admin\` | Sửa DB trực tiếp trong View |
| Data/SQLite | \`data/local\`, \`database/\` | Thay API công khai không phối hợp |
| Domain + QA | \`domain\`, \`src/test\` | Đưa test fixture vào sản phẩm |
| Design System | \`ui/design\`, \`res\` | Thay đổi nghiệp vụ trong component |

**Quy tắc PR:** một owner/nhóm cho mỗi phần, không sửa song song \`DemoState\` hoặc \`MainActivity\` nếu chưa có người tích hợp. Không đổi \`applicationId\`, không sinh thêm module Android, không đưa Kotlin/Compose trở lại.

## 5. Ranh giới và việc chưa làm

Đợt này là **chuẩn hóa cấu trúc**, không phải xác nhận đáp ứng đầy đủ mọi màn/luồng nghiệp vụ. Những vấn đề cần được kiểm toán riêng ở lần tiếp theo:

- \`DemoState\` còn lưu trạng thái UI lớn, chứa constructor fixture dành cho JUnit; chưa tách hoàn toàn thành session/state/domain service.
- Một số giao diện tạo View động; **chưa đồng loạt chuyển sang RecyclerView + Adapter/ListAdapter**, nên không nên quảng cáo là adapter đã hoàn chỉnh.
- CSDL SQLite trong Android là file nằm trong sandbox thiết bị; file \`database/PharmaSIU_Main.sqlite3\` trên máy tính và bản mẫu 93 dòng **không tự đồng bộ**. Chưa có server backend PostgreSQL cho app.
- Địa chỉ, yêu thích, Rx thật, thanh toán, ca và audit đầy đủ vẫn cần kiểm tra theo lược đồ 6 bảng / phương án mở rộng; không gọi là đã vận hành.
- Kiểm toán chi tiết việc bám sát quy trình nghiệp vụ, hiệu năng màn, accessibility và toàn bộ điều kiện Rx/FEFO được tách thành đợt thực hiện sau khi cấu trúc được chốt.

## 6. Kiểm chứng và điều kiện chốt

Chạy từ root:

\`\`\`powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon
adb shell pm list packages -3 | findstr pharmasiu
\`\`\`

Kỳ vọng: **một APK \`:app\`, một package được cài \`com.pharmasiu.mobile\`**, Android Java/XML không còn Kotlin nguồn chính; các unit tests đạt, màn Login chạy trên emulator. Giữ \`database/\` đúng ở root và **không xóa dữ liệu trên ứng dụng chính**.

Đây là **tài liệu cấu trúc chuẩn sau cleanup**. Nếu các file cũ có số lượng file hoặc package khác, dùng tài liệu này làm bảng định vị hiện hành; không suy ra rằng nghiệp vụ đã được kiểm nghiệm 100%.

**Kết quả cleanup:** 69 file Java production, 39 file Java trong cây test (bao gồm lớp hỗ trợ fixture), 1 module Android và 1 applicationId. Các con số này là số lượng source, không đại diện số chức năng đã hoàn thành.

## 7. Biên bản nghiệm thu cấu trúc (30/10/2026)

- **Clean build**: `:app:clean :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon` → **BUILD SUCCESSFUL**.
- **JUnit**: 144 bài test / 30 suite, **0 failed, 0 errors, 0 skipped**.
- **Emulator**: đã cài lại `app-debug.apk` bằng `adb install -r` thành công, mở `com.pharmasiu.mobile/.MainActivity`, PID xuất hiện, **không có FATAL/InflateException** trong log thử.
- **App trùng**: `adb pm list packages -3` hiện chỉ có **`com.pharmasiu.mobile`**, không còn `com.pharmasiu.mobile.java`.
- **Dữ liệu app chính**: `run-as com.pharmasiu.mobile ls databases` vẫn có `pharmasiu.db`, `pharmasiu.db-shm`, `pharmasiu.db-wal` sau cài cập nhật; **không chạy pm clear**, không xóa database của ứng dụng chính.
- **Resources**: đã quét các tham chiếu, 29 layout và 24 drawable đang được sử dụng; giữ nguyên. Không tự xóa hình/UI chỉ dựa vào tên cũ.
- **Cảnh báo còn lại**: Gradle/Android vẫn phát cảnh báo deprecated API và Gradle 9 compatibility; clean build/test đạt nhưng chưa là kiểm thử UI đầy đủ, chưa chứng minh Rx/FEFO/API hoàn chỉnh.

**Trạng thái bàn giao:** Hoàn tất **đợt dọn cấu trúc, một APK, và phân tách repository**. **Chưa** commit/push Git và **chưa** mở đợt sửa tính năng/adapter mới.

### SQLite Android → Navicat (một chiều)
- File để mở trong Navicat: `database/live/PharmaSIU_Live.sqlite3`.
- Chạy `database/Start_SQLite_AutoSync.bat` khi emulator được kết nối; đây là bản sao một chiều và không tự đẩy thay đổi từ Navicat lên Android.
