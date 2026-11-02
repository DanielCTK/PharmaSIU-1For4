## CSDL SQLite dành cho Navicat (01/11/2026)

- Mẫu để mở trực tiếp: C:\PharmaSIU\PharmaSIU-1For4\database\PharmaSIU_Main.sqlite3 (15 sản phẩm + 15 lô; kiểm tra integrity/FK OK).
- App Android có CSDL riêng trong emulator. Khi danh mục trống, app bổ sung 15 sản phẩm + 15 lô; nếu đã có sản phẩm thì giữ nguyên.
- Muốn xem dữ liệu thực tế của app: chạy Start_SQLite_AutoSync.bat rồi dùng live\PharmaSIU_Live.sqlite3 với Navicat. Đây là bản sao một chiều, không tự đồng bộ thay đổi từ Navicat ngược vào app.
- Dữ liệu mẫu cùng một nguồn: app/src/main/assets/database/catalog_seed.csv, công cụ nạp Navicat tại tools/load_sample_catalog.py.
- Hướng dẫn kiến trúc: docs/ANDROID_SQLITE_UI_2026-11-01.md và C:\PharmaSIU_Docs.

## Dữ liệu SQLite đang chạy trên Android (đồng bộ về Navicat)

**Mở `database/live/PharmaSIU_Live.sqlite3` trong Navicat.** Để có và tự cập nhật file này, chạy `database/Start_SQLite_AutoSync.bat` khi emulator + ứng dụng debug đang hoạt động. Đây là **snapshot một chiều Android → máy tính**, cập nhật khoảng 10 giây/lần khi công cụ đang chạy, không phải cùng một file hai bên. Xem [live/README.md](live/README.md) để biết cách dùng và giới hạn.

# PharmaSIU — Cơ sở dữ liệu DEMO (6 bảng)

**Tình trạng:** Đã có file database SQLite thực tế, có thể mở và duyệt dữ liệu trực tiếp bằng **Navicat Premium (kết nối SQLite)**, DB Browser for SQLite hoặc DBeaver. Bộ SQL PostgreSQL đi kèm để triển khai trên máy chủ sau. Mọi bản ghi đều là **dữ liệu giả lập**, không phải thông tin bệnh nhân, toa hay giao dịch thật.

## 1. Mở database ngay bằng Navicat

**File:** `database/PharmaSIU_Demo.sqlite3` (trong thư mục gốc dự án `C:\PharmaSIU\PharmaSIU-1For4`).

1. Mở Navicat có hỗ trợ SQLite → **Connection / New Connection → SQLite**.
2. Chọn **Existing database file** (hoặc tùy chọn tương đương) → Browse đến `C:\PharmaSIU\PharmaSIU-1For4\database\PharmaSIU_Demo.sqlite3`.
3. Đặt tên kết nối `PharmaSIU_Demo`, lưu và mở danh sách **Tables**.
4. Có **đúng 6 bảng**; nhấp đúp `san_pham` / `don_hang` / `lo_thuoc` để xem 15 bản ghi mỗi bảng.
5. Trước khi chỉnh dữ liệu bằng SQL, chạy `PRAGMA foreign_keys = ON;` **trong mỗi kết nối SQLite** (vì SQLite có thể tắt FK mặc định theo connection). Các trigger kiểm soát giữ hàng vẫn được lưu trong file.
6. Mở `sqlite/05_queries.sql` trong Navicat Query Editor và chạy từng câu `SELECT` để xem dữ liệu liên kết/tồn dự kiến/cảnh báo lô/đối soát DEMO.

**Không cần host, port, username hay password** đối với file SQLite này. Nếu bản Navicat của bạn không hỗ trợ SQLite, mở thẳng file đó với **DB Browser for SQLite** hoặc **DBeaver**.

## 2. Cấu trúc database/

```text
database/
├── PharmaSIU_Demo.sqlite3          # FILE DATABASE THẬT, mở trực tiếp Navicat SQLite
├── README.md                        # Hướng dẫn này
├── sqlite/
│   ├── 01_schema.sql                # 6 bảng, PK/FK và CHECK
│   ├── 02_seed.sql                  # dữ liệu mẫu giả lập
│   ├── 03_views.sql                 # 4 VIEW báo cáo
│   ├── 04_guards.sql                # trigger giữ hàng, nhả giữ, tồn lô
│   └── 05_queries.sql               # các truy vấn để chạy trong Navicat
├── postgresql/
│   ├── 00_create_database.sql       # tạo DB server tên pharmasiu_demo
│   ├── 01_schema.sql                # 6 bảng với BIGINT, TIMESTAMPTZ, FK ghép
│   ├── 02_seed.sql                  # dữ liệu mẫu PostgreSQL
│   ├── 03_views.sql
│   ├── 04_guards.sql                # trigger và khóa hàng theo giao dịch
│   └── 05_queries.sql
└── tools/
    ├── build_database.py           # tạo file SQLite từ bộ SQL; có --force bảo vệ
    ├── verify_database.py          # kiểm tra bảng/FK, số dòng và SQL guard cơ bản
    ├── verify_workflow.py          # vòng đời trả tiền, xuất lô, bàn giao, hoàn tất
    ├── verify_integrity_guards.py  # kiểm soát duyệt kiểm kê/Rx, chỉnh tồn/hold
    ├── verify_expiry_payment.py    # hết hạn hiện tại và số tiền DEMO tối thiểu
    └── verify_package.py           # CRC và so sánh toàn bộ ZIP byte-by-byte
```

## 3. Các bảng và dữ liệu có sẵn

| Bảng | Dòng mẫu | Mục đích |
|---|---:|---|
| `tai_khoan` | 15 | Tài khoản giả lập: 8 khách, 4 nhân viên, 3 quản trị; tài khoản KHÔNG đăng nhập được |
| `san_pham` | 15 | Một phần danh mục giả lập đang có trên app, gồm vật tư/chăm sóc/OTC/Rx minh họa |
| `lo_thuoc` | 15 | Lô theo sản phẩm, mã lô, nhập, hạn, vị trí kệ, tồn và tình trạng |
| `don_hang` | 15 | Đơn web/app/POS DEMO với các trạng thái theo vòng đời |
| `chi_tiet_don_hang` | 18 | Dòng đơn, giá chốt, lô tùy chọn, số giữ và số xuất |
| `kiem_tra_lo` | 15 | Kết quả kiểm tra hạn/chất lượng của các lô |

Tổng cộng **93 bản ghi**. Tất cả mã đơn, mã lô, mã chứng từ và ca đều bắt đầu bằng `DEMO-`. Email dùng miền dành riêng `example.invalid`, điện thoại giả lập, trường `mat_khau_hash = NULL` (không phải tài khoản đăng nhập). Không có tệp toa hoặc giấy tờ thật. Mọi giá, tồn và kết quả ghi nhận chỉ để test.

**Đúng 6 bảng theo sơ đồ CSDL**, không phát sinh bảng tài khoản phụ, lịch sử thanh toán hoặc các bảng ngoài phạm vi. Bốn `VIEW` chỉ là truy vấn ảo, không phải bảng dữ liệu thứ bảy.

## 4. Quy tắc quan trọng trong dữ liệu

- PK/FK cho cả sáu bảng. `chi_tiet_don_hang(lo_thuoc_id,san_pham_id)` tham chiếu FK ghép tới `lo_thuoc(lo_thuoc_id,san_pham_id)`, do đó dòng đơn **không thể gán lô thuộc sản phẩm khác**.
- `lo_thuoc_id` được để **NULL khi chưa chọn lô**. Không ép gán lô cho mọi yêu cầu đang chờ.
- `so_luong_ton` là tồn vật lý. `so_luong_giu` nằm ở dòng đơn, **không giảm tồn vật lý**. View `v_ton_kha_dung` tính lượng khả dụng dự kiến bằng tồn vật lý trừ giữ hàng còn hiệu lực.
- Trigger chặn giữ vượt tồn, giữ lô cách ly/hết hạn và giữ Rx chưa có quyền/phê duyệt hợp lệ. Hủy/từ chối/hoàn tất phải nhả hết giữ hàng trước trong **cùng transaction**. Khi ghi `so_luong_xuat`, trigger DEMO cập nhật `lo_thuoc.so_luong_ton` nguyên tử; không sửa tồn vật lý thủ công. Việc xác nhận giao/thu tiền THẬT vẫn cần backend và nhân viên có quyền.
- `kiem_tra_lo` giữ biên bản/lượng thực đếm. Khi **chưa được quản trị viên duyệt**, không sửa tồn; khi có biên bản kiểm kê `CHENH_LECH` hợp lệ và phê duyệt, trigger DEMO mới điều chỉnh tồn theo `ton_sau_dieu_chinh`, đồng thời giữ lịch sử bất biến.
- Không lưu mật khẩu rõ, hồ sơ thật, toa y tế hoặc bằng chứng thanh toán. Ví dụ `DEMO_DA_GHI_NHAN` không phải giao dịch ngân hàng/tiền mặt thật.

**Giới hạn:** SQLite yêu cầu `PRAGMA foreign_keys=ON` trên TỪNG kết nối (Navicat có thể mặc định tắt); DB owner có quyền SQL tùy ý vẫn có thể thay đổi flag tài khoản/thanh toán hoặc hủy trigger. CSDL demo không thể xác minh giấy phép chuyên môn hoặc chứng từ thanh toán thật. Trigger kiểm tra điều kiện khi bạn sửa record trực tiếp, nhưng việc kiểm duyệt dược sĩ, auth và thanh toán thật vẫn cần backend, quy trình/phân quyền thực, ghi log và kiểm thử giao dịch. Ứng dụng Android hiện vẫn là **RAM DEMO**; file này chưa tự đồng bộ với ứng dụng.

## 5. PostgreSQL trong Navicat (nếu muốn host/port)

Bộ PostgreSQL dùng kiểu `TIMESTAMPTZ` đúng như sơ đồ. Cần cài và khởi động **PostgreSQL server** riêng (chưa có server/credentials được cung cấp; không tự tạo mật khẩu cho bạn).

1. Kết nối Navicat → PostgreSQL đến server của bạn (thường `localhost:5432`, dùng tài khoản được bạn quản lý).
2. Trong database `postgres`, chạy `postgresql/00_create_database.sql` (không đặt trong transaction).
3. Chuyển kết nối/Query Editor sang database **`pharmasiu_demo`**. Chạy theo thứ tự `01_schema.sql`, `02_seed.sql`, `03_views.sql`, `04_guards.sql`.
4. Mở `05_queries.sql` để xem mẫu các bảng, tồn khả dụng và báo cáo.

**PostgreSQL 18 đã được kiểm thử trực tiếp** trên máy chủ QA tạm chỉ nghe tại `127.0.0.1:55432`: tất cả SQL tạo bảng/dữ liệu/view/trigger chạy thành công; kiểm tra luồng POS và Click & Collect/rollback thành công. **Máy chủ QA đã dừng ngay sau test**, không phải database PostgreSQL cố định cho Navicat của bạn. SQLite vẫn là database có thể mở trực tiếp không cần host.

## 6. Kiểm thử và xây lại dữ liệu mẫu

Chạy từ thư mục gốc dự án:

```powershell
python .\database\tools\verify_database.py
```

Thực hiện thêm `python .\database\tools\verify_workflow.py` và
`python .\database\tools\verify_integrity_guards.py` để kiểm tra vòng đời đơn và trường hợp giả mạo quyền/kiểm kê trong bản sao RAM. Thực hiện tiếp `python .\database\tools\verify_expiry_payment.py` và sau khi đóng gói `python .\database\tools\verify_package.py`.

Kiểm thử kiểm tra 6 bảng, 93 dòng, 4 view, `PRAGMA integrity_check`, `foreign_key_check`, kiểm tra giá chốt và 9 phép sửa sai bị chặn (FK ghép, quá số giữ, Rx, hủy khi còn giữ, giảm tồn dưới số giữ, cách ly khi còn giữ, xuất bán chưa thanh toán, xuất Rx chưa duyệt và quay lại trạng thái chờ khi đã xuất). Các phép thử ghi chỉ chạy trên **bản sao trong RAM**, không thay đổi file database.

`build_database.py` **không ghi đè .sqlite3 đã có**. Chỉ khi muốn chủ động hủy dữ liệu và tạo mới từ số 0, sao lưu file cũ rồi chạy:

```powershell
python .\database\tools\build_database.py --force
```

Tác vụ `build_database.py` từ chối chạy nếu database đã có và thiếu `--force`, không động đến script seed; khi có `--force` chương trình xây database tạm rồi thay thế sau khi kiểm thử, giúp giảm nguy cơ mất DB cũ nếu schema lỗi. Vẫn nên sao lưu trước khi `--force`.

Không chạy lại seed trên một DB đã có dữ liệu nếu không muốn bị trùng khóa. Nếu bạn bắt đầu từ SQLite database trống, chạy theo thứ tự 01 → 02 → 03 → 04. Trên mỗi connection phải bật `PRAGMA foreign_keys=ON`.

## 7. Trạng thái tích hợp ứng dụng

Việc tạo và kiểm thử CSDL **không đồng nghĩa** Android đã kết nối database. Để dùng khi vận hành thực, cần thiết kế backend/API (đặc biệt Rx, giữ tồn đồng thời, FEFO, quyền, giao nhận và thanh toán). Không truy cập trực tiếp server PostgreSQL bằng tài khoản quản trị từ APK.

## 8. Kiểm toán độc lập và các giới hạn cần lưu ý

Kết quả kiểm toán theo từng hạng mục và khắc phục lỗi: `database/QA_SUBAGENTS_2026-10-29.md`.

- Bản SQLite đang có **29 trigger**, dữ liệu mẫu vẫn **6 bảng và 93 dòng**. Xuất lô giảm tồn trong cùng transaction; kiểm kê được quản trị duyệt mới điều chỉnh tồn; hoàn tất phải đủ số xuất, tiền DEMO và bàn giao.
- FEFO hiện có thể xem theo hạn dùng nhưng chưa cưỡng chế thuật toán chọn lô sớm nhất ở mọi kênh. Cần backend trước vận hành thật.
- Database không thể xác minh thanh toán hay quyền dược sĩ thật từ một cờ SQL; người có toàn quyền DBA có thể sửa chính cờ. Không dùng làm bằng chứng thanh toán/dược sĩ chuyên môn.
- Gói `PharmaSIU_Database_6Tables.zip` được kiểm tra byte-by-byte bằng `verify_package.py` sau mỗi lần thay đổi SQL hoặc .sqlite3.
