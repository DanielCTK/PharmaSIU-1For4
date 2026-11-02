# PharmaSIU — Kiến trúc Android / SQLite / giao diện phẳng

**Ngày cập nhật:** 2026-11-01.

## Mục tiêu đợt chỉnh sửa

Áp dụng các kỹ thuật Android thông dụng ngay trong cấu trúc Java/XML, tiếp tục dùng SQLite trên emulator, có 15 sản phẩm + 15 lô mẫu, và loại bỏ các khung bo góc của giao diện. Không thêm module, không đổi applicationId, không cài đặt thêm một app khác. Không chuyển sang Kotlin/Compose.

## Truy vết kỹ thuật và đường dẫn

| Kỹ thuật | Vị trí code | Trách nhiệm |
|---|---|---|
| Activity & vòng đời | `app/src/main/java/com/pharmasiu/mobile/MainActivity.java` | Màn hình khởi chạy, mở/chọn tài liệu bằng Intent, điều phối route |
| Android Intent | `MainActivity.java`, `Intent.ACTION_OPEN_DOCUMENT` | Chọn file tại thiết bị, không giả lập màn phụ chỉ để trình diễn |
| Điều hướng | `navigation/PharmaBottomBar.java` | Material Bottom Navigation, listener chuyển route |
| Gán sự kiện `OnClickListener` | `ui/auth/AuthFormController.java`, `ui/customer/catalog/CustomerHomeScreen.java`, `ui/customer/catalog/CustomerBrowseScreen.java` | Xử lý nhấn nút tách khỏi business DB |
| Sự kiện `TextWatcher`, `OnItemSelectedListener` | `ui/customer/catalog/CustomerBrowseScreen.java` | Tìm kiếm, lọc, sắp xếp; Spinner dùng `ArrayAdapter` |
| LinearLayout/FrameLayout/ScrollView | `app/src/main/res/layout/catalog_v2_home.xml`, `catalog_v2_catalog.xml`, `customer_account_page.xml`, `activity_main.xml` | Bố cục XML full-width |
| View dùng chung | `ui/design/PharmaUi.java` | Card, button, input, thông báo; góc vuông |
| SQLiteOpenHelper | `data/local/PharmaDatabase.java` | Mở DB Android, kiểm tra đúng sáu bảng, bật FK; chặn schema upgrade chưa định nghĩa |
| SQLite CRUD/Cursor | `data/local/AccountQueries.java`, `CatalogQueries.java`, `OrderQueries.java` | Đọc/ghi DB có transaction và kiểm tra nghiệp vụ |
| Repository façade | `data/local/PharmaRepository.java` | API dữ liệu tập trung cho UI, tránh cho screen truy vấn thẳng DB |
| Seed dữ liệu mẫu | `data/local/CatalogSeed.java`; `app/src/main/assets/database/catalog_seed.csv` | Nạp chính xác 15 sản phẩm + 15 lô vào DB Android chỉ khi bảng sản phẩm rỗng |
| File SQLite PC | `database/PharmaSIU_Main.sqlite3` | Dữ liệu mẫu cho Navicat (không tự đồng bộ ngược) |
| Snapshot SQLite Android ra PC | `database/Start_SQLite_AutoSync.bat` và `database/live/PharmaSIU_Live.sqlite3` | Bản sao một chiều để kiểm tra dữ liệu app |

**Không ép đưa Navigation Drawer hoặc một Activity giả vào app** chỉ để có ví dụ. Ứng dụng hiện dùng bottom navigation; các màn chuyển route trong MainActivity. Khi xuất hiện một luồng cần Activity mới, khai báo Manifest và Intent đúng nghiệp vụ.

## Cấu trúc duy trì

```text
app/src/main/
├── AndroidManifest.xml
├── assets/database/
│   ├── pharmasiu_schema.sqlite3
│   └── catalog_seed.csv
├── java/com/pharmasiu/mobile/
│   ├── MainActivity.java
│   ├── navigation/PharmaBottomBar.java
│   ├── core/                # session + UiHost + trạng thái tương thích
│   ├── data/local/          # SQLiteOpenHelper, repository, account/catalog/order queries, seed
│   ├── domain/              # quy tắc Java độc lập UI
│   └── ui/
│       ├── design/          # thành phần chung
│       ├── auth/
│       ├── customer/
│       │   ├── account/
│       │   ├── catalog/
│       │   └── orders/
│       └── staff/
│           ├── pos/
│           ├── admin/
│           └── media/
└── res/layout,res/drawable,res/values
```

Không phát sinh package rỗng, không để Java View thực hiện SQL trực tiếp.

## SQLite: hai địa điểm rõ ràng

1. **Mở bằng Navicat trên máy tính:** `C:\PharmaSIU\PharmaSIU-1For4\database\PharmaSIU_Main.sqlite3`; đã nạp 15 sản phẩm, 15 lô giả lập và giữ bốn bảng khác rỗng. Đây là database mẫu độc lập.
2. **Dữ liệu app thực tế:** `/data/data/com.pharmasiu.mobile/databases/pharmasiu.db` trong emulator, dùng SQLite cục bộ, không cần server. Khi mở app debug lần đầu và bảng sản phẩm rỗng, `CatalogSeed` thêm dữ liệu mẫu; nếu đã có sản phẩm thì giữ nguyên.
3. **Để xem database của app trên Navicat:** chạy `database/Start_SQLite_AutoSync.bat` và mở `database/live/PharmaSIU_Live.sqlite3`; một chiều **Android → PC**, 10 giây/lần chỉ khi BAT + emulator hoạt động; thao tác sửa Navicat trên bản sao không tự cập nhật Android.

Mẫu chỉ dùng để chạy thử và kiểm tra giao diện; hai lô RX ở trạng thái `CHO_KIEM_TRA`, không tự duyệt/cho bán. Không tự tạo tài khoản nhân viên hoặc mật khẩu thật.

## Giao diện góc vuông và full-width

- Drawable shape trong `res/drawable` dùng radius `0dp`.
- `PharmaUi` dùng CardView radius 0; button radius 0; input corners 0.
- Các MaterialButton được set `app:cornerRadius="0dp"`.
- Hero banner Home/Danh mục là `match_parent` và không còn lề ngang ngoài; phần chữ vẫn có padding 18–24dp.
- Trang Tài khoản khách chưa đăng nhập được hiển thị thành header + khối hero toàn chiều ngang.
- Không bỏ status/navigation bar an toàn; `full-width` là nội dung tới cạnh ngang thiết bị, không phải vẽ đè lên khu vực hệ thống.

## Giới hạn thực tế

- `DemoState` vẫn là façade trạng thái tương thích trong UI, không được hiểu là database. Các tính năng còn mang thông báo demo/chưa vận hành vẫn chưa được coi là hoàn chỉnh.
- Không tự động đồng bộ hai chiều với Navicat; PostgreSQL không được dùng trong app ở đợt này.
- Chưa xác nhận thử nghiệm toàn bộ nghiệp vụ Rx/FEFO/đơn hàng trên nhiều thiết bị.
- Các vị trí mã bên ngoài bảng trên chỉ là mã sản phẩm hiện hữu, không được coi là đã áp dụng mọi ví dụ trong từng bài giảng.

**Bộ tài liệu giải thích độc lập:** `C:\PharmaSIU_Docs\` — nằm ngoài thư mục dự án; xem các file 01–04.
