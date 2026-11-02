# PharmaSIU — SQLite từ Android về máy tính

**File để mở trong Navicat:**

`C:\PharmaSIU\PharmaSIU-1For4\database\live\PharmaSIU_Live.sqlite3`

Đây là bản sao được cập nhật từ **database đang hoạt động trong Android** (`/data/data/com.pharmasiu.mobile/databases/pharmasiu.db`), không phải nguồn dữ liệu chính. Bản sao chỉ xuất hiện sau lần đồng bộ thành công đầu tiên.

## Cách dùng mỗi khi mở Android Emulator

1. Mở Android Emulator và chạy bản ứng dụng debug `com.pharmasiu.mobile`.
2. Bấm đúp `database\Start_SQLite_AutoSync.bat` ở project. Giữ cửa sổ đó chạy khi sử dụng emulator.
3. Công cụ tự kiểm tra khoảng **10 giây/lần**. Sau mỗi thay đổi thành công trên app, nó cập nhật file `live\PharmaSIU_Live.sqlite3` khi snapshot mới hợp lệ.
4. Trong **Navicat → New Connection → SQLite → Existing database file**, chọn đường dẫn trên. Khi muốn xem dữ liệu mới, nhấn **Refresh** trong Navicat. Công cụ dùng SQLite Online Backup để cập nhật cùng một file khi không có giao dịch ghi xung đột; nếu Navicat đang chỉnh dữ liệu, hoàn tất thao tác đó trước.

Muốn cập nhật một lần trước khi mở Navicat: chạy `database\Refresh_SQLite_Once.bat`.

## Bắt buộc hiểu đúng

- Dữ liệu **chính** thuộc về file SQLite bên trong emulator, KHÔNG nằm trực tiếp tại `C:\...\database\live`.
- Đây là luồng **một chiều Android → PC**. Không chỉnh dữ liệu trực tiếp trên file `PharmaSIU_Live.sqlite3`: bản cập nhật sau có thể ghi đè mọi sửa đổi. Nếu muốn nhập dữ liệu cho app, thực hiện trên ứng dụng hoặc xây chức năng nhập/đồng bộ riêng.
- Đồng bộ chỉ hoạt động **khi emulator đang mở, ADB kết nối và công cụ đồng bộ đang chạy**. Đóng app hay ngắt emulator thì file PC vẫn giữ trạng thái lần xuất cuối, **không còn cập nhật**.
- Cần **debuggable APK** để ADB `run-as` đọc file. Bản Android release không hỗ trợ kiểu truy cập này. Công cụ không truy cập backend hoặc PostgreSQL.
- SQLite có thể dùng chế độ WAL. Công cụ thu thập `.db`, `-wal`, `-shm`, tạo snapshot SQLite riêng, kiểm tra integrity/FK/đúng sáu bảng **trước khi thay file**. Nếu đọc trúng lúc app đang ghi hoặc Navicat đang khóa file, nó báo lỗi và thử lại chu kỳ sau. Không xóa/cắt/chỉnh database Android đang hoạt động.
- File live có thể có dữ liệu riêng tư của người dùng thử. Đã cấu hình Git **không theo dõi** file `.sqlite3` trong thư mục `live`; không gửi công khai.

## Thư mục khác trong database

- `PharmaSIU_Main.sqlite3`: database trống dùng làm mẫu, **không phải bản live**.
- `PharmaSIU_Demo.sqlite3`: 93 bản ghi ví dụ, **không phải bản live**.
- `sqlite/`, `postgresql/`: schema, script, trigger.
- `tools/sync_android_sqlite.py`: công cụ đồng bộ, chỉ dùng Python chuẩn, cần ADB.

**Chạy nâng cao (PowerShell):**

```powershell
python .\database\tools\sync_android_sqlite.py --once
python .\database\tools\sync_android_sqlite.py --interval 10
```

Nếu có nhiều thiết bị Android nối ADB, thêm `--serial emulator-5554` (thay theo thiết bị của bạn).
