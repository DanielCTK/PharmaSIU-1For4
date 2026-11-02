# PharmaSIU — Kiến trúc thực tế và định hướng dữ liệu (30/10/2026)

## 1. Quyết định kiến trúc

PharmaSIU là **ứng dụng Android native đơn module**, viết **Java 17 + XML Views (Material Components)**, cấu trúc theo **feature + layer**: presentation (UI) → domain (business rules) → data (repository/DAO) → SQLite local persistence. Một màn hình không được coi là kết nối CSDL chỉ vì có file .sqlite3 bên ngoài repository. PostgreSQL trong `database/postgresql` là **bộ lệnh máy chủ**, chưa phải backend/API Android.

Điều kiện bất biến: chỉ `:app`, package/appId `com.pharmasiu.mobile`, không phát sinh Kotlin/Compose, không dùng dữ liệu được cấy tự động trong Java như nguồn dữ liệu chính. Database chính đang có 6 bảng `tai_khoan`, `san_pham`, `lo_thuoc`, `kiem_tra_lo`, `don_hang`, `chi_tiet_don_hang` cùng khóa/trigger, hỗ trợ chính thức các nghiệp vụ cốt lõi. Giỏ, yêu thích, địa chỉ, ca và hình ảnh không có bảng riêng trong schema 6 bảng: không thể gọi chúng là **được lưu DB** nếu chưa có lược đồ/giải pháp phù hợp. Không sinh dữ liệu giả để lấp màn trống.

## 2. Các thư mục thực sự đang được dùng

```text
app/src/main/
  java/com/pharmasiu/mobile/
    MainActivity.java                    # Activity, lifecycle, routing
    navigation/                           # Role-aware navigation
    ui/
      auth/                               # xác thực UI
      customer/{catalog,orders,account}/  # UI theo từng feature
      staff/{pos,admin,media}/            # UI theo từng feature
      design/                             # Material component styles
    core/                                 # UiHost, state façade (đang di chuyển)
      state/                              # state operations (đang di chuyển)
    domain/                               # quy tắc thuần Java
    data/{model,repository}/              # lớp dữ liệu (đang chuẩn hóa)
    data/local/                           # SQLiteOpenHelper/DAO, migration, bootstrap
  assets/database/                        # SQLite schema dành cho app (không chứa thông tin thật)
  res/                                    # layouts, drawable, values
app/src/test/java/                        # JUnit Java
database/
  PharmaSIU_Demo.sqlite3                  # DỮ LIỆU MẪU dùng cho Navicat/kiểm tra
  sqlite/                                 # schema / seed / views / guard
  postgresql/                             # server schema / seed / views / guard
  tools/                                  # QA, tạo database, xuất/nhập dữ liệu
```

**Lưu ý:** `database/PharmaSIU_Demo.sqlite3` có **93 bản ghi DEMO** để xem trong Navicat, không được hiểu là dữ liệu kinh doanh thật. Android không được mặc định dùng 93 bản ghi đó nếu người dùng yêu cầu bỏ mock; phải dùng database khởi tạo rỗng theo cùng schema. Dữ liệu nhập vào app sau đó nằm trong vùng lưu trữ riêng của Android. Mở file trong Navicat ở máy tính không có nghĩa đang trực tiếp xem database nằm trong điện thoại.

## 3. Quy ước cho nhóm

- **Data/SQLite:** các file `data/local/**`, mapping 6 bảng, schema version, query parameterized, transaction. Không viết SQL trong Activity/View.
- **Domain:** `domain/**`, các quy tắc FEFO/Rx/quyền; không phụ thuộc Context/View. Không dùng `DemoState` như nơi xác nhận bán hàng thật.
- **Customer:** `ui/customer/**`, chỉ đọc qua UiHost/repository và hiển thị trạng thái rỗng/loading/error thật.
- **Staff:** `ui/staff/**`, xử lý status lô/kiểm kê/bán qua transaction data, không chỉ sửa Collections.
- **Auth:** `ui/auth/**` chỉ giao diện; hashing/check quyền trong lớp data/auth, không hardcode pass demo.
- **Integration:** `MainActivity`, `UiHost`, repository façade và manifest/Gradle. Chỉ người phụ trách tích hợp sửa.
- **QA:** JUnit kiểm thử nghiệp vụ + SQLite schema/trigger; emulator 3 vai trò, restart kiểm tra persistence.

## 4. Phân biệt trạng thái triển khai

`DB-backed` chỉ khi người dùng tạo/đổi một record, thoát hẳn ứng dụng, mở lại và record đó vẫn tồn tại qua SQLite; kiểm thử phải truy vấn được bản ghi tương ứng. `Chờ backend` cho toa thật, duyệt dược sĩ, giữ kho đồng thời đa thiết bị, thanh toán, đồng bộ PostgreSQL. Cấm gắn nhãn thành công thật nếu chỉ có UI/SQLite prototype.

## 5. Điều kiện nghiệm thu

- Chỉ xóa các thư mục rỗng sau khi xác nhận không có file ẩn, reference hoặc source bên trong; xóa thư mục rỗng không thay đổi Gradle build.
- Không còn `seed(...)` catalog và tài khoản `demo1234/1111/2222` trong runtime Android.
- SQLite `PRAGMA foreign_keys=ON` trên kết nối; schema/trigger phải được đối chiếu với nguồn trong database/sqlite.
- Unit tests và assembleDebug pass, smoke màn không dữ liệu không crash; có kết quả kiểm tra lưu trữ qua khởi động lại.
- Chưa có bảng thì không tuyên bố đã có persistence và không ghi nhận giao dịch thật.
