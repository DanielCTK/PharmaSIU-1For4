# PHARMASIU — PHÂN CÔNG THỰC THI (TÀI LIỆU TRƯỚC CODE)

Đọc ALIGNMENT_SOURCE_OF_TRUTH.md và ALIGNMENT_DOMAIN_CONTRACT.md trước khi thay đổi mã. Không thêm database, SQL, Room/DAO, migration, backend/API thật. Không sửa gradle.properties hiện đang có chỉnh sửa sẵn. Không commit hay push.

## Ownership độc lập
- Nhóm CUSTOMER: ui/features/home,catalog,product,cart,prescription,checkout,orders,community,account; data/model/MockCommerce.kt. Thực hiện BFD 1.1–2.3 và kiểm tra thông điệp demo/khả dụng/ảnh toa/hủy đơn. Nếu phải chạm ui/PharmaSIUApp.kt (file chung) thì báo prime để prime tự xử lý.
- Nhóm STAFF: ui/features/staff/ và domain/CounterRules.kt; bổ sung test liên quan staff. Thực hiện BFD 3.1–5.3 trong giới hạn mock, chặn chuyên môn không có quyền, phân biệt các thao tác chỉ có thể chờ backend.
- Nhóm DOMAIN/QA: domain/OrderStateMachine.kt, tests tương ứng; docs hướng dẫn và ma trận kiểm thử. Không đụng file của CUSTOMER hoặc STAFF.
- PRIME: xử lý file chung, xung đột, kiểm tra truy vết đủ 17 chức năng, chạy build và tests.

## Cổng nghiệm thu
P0: không giả kết quả duyệt Rx, giữ hàng, thanh toán hoặc giao hàng; không cho quyền cashier/admin vượt quyền dược sĩ.
P1: 17/17 BFD có màn/thao tác hoặc trạng thái 'Chờ tích hợp' trung thực. DFD/State/Activity nhất quán; không có dữ liệu mock tự nhận kho thật.
P2: tài liệu theo code, màn hình và accessibility không giảm chất lượng.

Mỗi nhóm bàn giao: các file sửa; mã BFD liên quan; tình huống dương/âm đã test; lỗi còn lại và BLOCKED_BY_BACKEND. Tests Gradle và kiểm thử emulator chỉ được đánh PASS nếu đã thực chạy.

