# PharmaSIU — Biên bản kiểm toán cơ sở dữ liệu độc lập

**Ngày:** 29/10/2026 (phiên tiếp tục sang đêm).  
**Phạm vi:** `C:\PharmaSIU\PharmaSIU-1For4\database` và bản ZIP.  
**Tình trạng Git:** Mã sửa tại working tree local; không commit, không push.

## Phân công kiểm tra

Đã giao 6 hướng đánh giá: SQLite integrity/attack, PostgreSQL, dữ liệu mẫu & báo cáo, đối chiếu mô hình 6 bảng, mở file/đóng gói Navicat, quyền Rx/kiểm kê. Có worker khởi động lỗi; hai worker có sẵn được giao lại những phần thất bại. Kết quả hợp nhất gồm các kiểm toán đọc-only độc lập và bước prime tái kiểm thử/sửa mã.

## Kết quả nghiệm thu

| Hạng mục | Bằng chứng | Kết quả |
|---|---|---|
| Khớp mô hình | 6 bảng, 75 cột đúng tên và thứ tự (9+9+13+24+8+12), FK ghép lot-product, lot nullable | PASS |
| SQLite dữ liệu | 93 bản ghi, đúng 15 tài khoản,15 sản phẩm,15 lô,15 đơn,18 chi tiết,15 kiểm tra | PASS |
| SQLite lưu trữ | `PRAGMA integrity_check` = ok, `foreign_key_check` = [] | PASS |
| SQLite nghiệp vụ | `verify_database.py`: 9 phép sửa sai bị chặn, 1 phép giữ hợp lệ đạt | PASS |
| Luồng thanh toán/giao | `verify_workflow.py`: nhận hàng và POS thanh toán→xuất→bàn giao→hoàn tất, 7 trường hợp bất hợp lệ bị chặn | PASS |
| Bảo toàn kho/Rx | `verify_integrity_guards.py`: điều chỉnh tồn không duyệt, giả duyệt toa, chuyển đơn, giữ quá tồn, quyền duyệt sai bị chặn; kiểm kê được quản trị duyệt cập nhật tồn | PASS |
| Hạn dùng/thu tiền | `verify_expiry_payment.py`: cấm xuất quá hạn vào ngày hiện tại, cấm hoàn tất thiếu tiền DEMO, không cho hoàn nguyên xuất đã ghi | PASS |
| PostgreSQL QA | Server PostgreSQL 18 tạm chỉ nghe `127.0.0.1:55432`: chạy 00..04, tạo 6 bảng + 93 dòng + 4 views + 26 trigger entries; kiểm thử luồng nhận hàng/POS và 4 trường hợp không hợp lệ | PASS |
| Dữ liệu PostgreSQL thực tế | Server QA `ROLLBACK` toàn bộ ca test, `pg_ctl stop` thành công, không triển khai service thường trực | PASS |
| ZIP bàn giao | `verify_package.py`: CRC, danh sách file, byte-by-byte so với thư mục hiện tại, mở SQLite embedded từ ZIP và đếm trigger | PASS khi chạy nghiệm thu cuối |

## Những lỗi đã tìm và sửa

1. **P0 — Vòng đời đơn bị khóa vòng:** trigger đòi `HOAN_TAT` trước xuất lô, trái quy trình. Đã đổi thành ghi nhận thanh toán DEMO → xuất (giảm tồn vật lý tự động) → bàn giao → `HOAN_TAT`.
2. **P0 — Không có cập nhật tồn vật lý khi xuất:** đã bổ sung trigger tự trừ tồn trong cùng transaction, chặn xuất vượt tồn.
3. **P0 — Quản lý hết hạn dùng sai:** đã chặn xuất khi lô hiện đã hết hạn, không dùng ngày trả tiền cũ để vượt hạn.
4. **P0 — Nhả/khôi phục giữ vượt tồn:** đã chặn kích hoạt lại giữ hàng đã hết TTL nếu tổng giữ vượt tồn của lô.
5. **P0 — Sửa tồn/duyệt kiểm kê:** đã ràng buộc tồn vật lý bằng nhập ban đầu trừ các dòng xuất cộng các điều chỉnh có bản ghi được duyệt; tự áp dụng điều chỉnh đã duyệt, chặn sửa bản ghi kiểm kê đã duyệt.
6. **P0 — Duyệt Rx chỉ bằng trạng thái:** đã đòi `DA_DUYET` có người duyệt hợp lệ, thời điểm duyệt, quyền `co_quyen_duyet_toa=1`, tài khoản `HOAT_DONG` và role phù hợp. Dữ liệu demo không có người được xác nhận quyền dược sĩ.
7. **P1 — Thêm/chuyển dòng vào đơn hoàn tất:** đã bảo vệ cả đơn nguồn và đơn đích, đóng băng các dòng đã ghi xuất.
8. **P1 — Hoàn tất thiếu tiền:** đã đòi tổng tiền ghi nhận DEMO đủ giá chốt dòng đơn, đủ số xuất, giao_luc, thanh_toan_luc và không còn hàng giữ.
9. **P1 — Công cụ dựng lại làm mất dữ liệu/ghi seed khi chưa cho phép:** xây .sqlite3 tạm và thay thế sau khi test thành công; từ chối hoàn toàn khi file DB hiện hữu mà thiếu `--force`; đã test trên môi trường sandbox.
10. **P0 — ZIP không đồng bộ:** bản ZIP cũ thiếu script và chứa trigger cũ. Đã làm mới ZIP và tạo `verify_package.py` để chống tái diễn.

## Những giới hạn bắt buộc phải công bố

- **Đây là CSDL DEMO đã kiểm thử, không phải hệ thống vận hành thật.** Người có toàn quyền SQL/DBA vẫn có thể sửa tài khoản, ghi `DEMO_DA_GHI_NHAN`, đổi quyền hoặc drop trigger; DB không xác minh giấy phép hành nghề, giấy tờ thuốc hay giao dịch ngân hàng. Cần xác thực API, phân quyền server, chứng từ và audit riêng.
- SQLite yêu cầu bật `PRAGMA foreign_keys=ON` **từng kết nối**. Navicat có thể không tự bật; không thể đảm bảo FK khi người dùng chủ động tắt bằng công cụ DBA.
- FEFO là thuật toán chọn lô theo hạn; view hiển thị lot/expiry nhưng chưa có stored procedure bắt buộc luôn chọn lô sớm nhất. Backend phải chọn lô theo FEFO và kiểm tra lại giao dịch trước khi xác nhận.
- Dòng giữ hàng hết TTL bị loại khỏi view tồn khả dụng nhưng chưa có lịch chạy tự động cập nhật trạng thái/nhả số lượng; cần job backend.
- `kiem_tra_lo` không có chứng từ lịch sử độc lập cho mọi lần chỉnh tồn (vì cố định sáu bảng). Các biến động nghiêm túc cần nhật ký/audit backend.
- Server PostgreSQL đã được thử trên cổng QA tạm **không phải** server bạn sẽ kết nối qua Navicat. Database ứng dụng Android hiện vẫn là RAM demo, chưa có API đồng bộ.
- Sản phẩm Rx/dữ liệu bệnh nhân/hóa đơn mẫu không xác nhận thuốc đã bán thật, không có thanh toán/đơn thuốc thật.

## Chạy kiểm tra lại

Từ thư mục dự án:

```powershell
python .\database\tools\verify_database.py
python .\database\tools\verify_workflow.py
python .\database\tools\verify_integrity_guards.py
python .\database\tools\verify_expiry_payment.py
python .\database\tools\verify_package.py
```

Mỗi phép thử ghi trên bản sao RAM, không làm thay đổi `PharmaSIU_Demo.sqlite3`. Hướng dẫn mở bằng Navicat: `database/README.md`.
