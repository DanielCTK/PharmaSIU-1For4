# Android Router

## 0. Phân loại trước khi code
Xác định stack **Compose** hay **XML** từ build.gradle(.kts), mức Product/Prototype, minSdk/targetSdk, design system hiện tại và feature đang thay. Không chuyển toàn bộ XML → Compose nếu chỉ sửa một màn hình. Nếu mới, ưu tiên Kotlin/Compose.

## 1. Điều hướng
| Yêu cầu | Layer bắt buộc | Tool gợi ý |
|---|---|---|
| Màn hình catalog, thương mại, chi tiết | L1 L2 L3 L4 L7 L9 | Compose Material 3; Coil khi tải ảnh |
| Giỏ hàng, checkout, toa/đặt trước | L1 L3 L4 L7 L8 L9 | Compose + domain state machine; backend authority |
| Dashboard/báo cáo | L1 L2 L3 L6 L7 L9 | Vico nếu có biểu đồ |
| Animation hiệu ứng/empty/success | L4 L5 L7 L8 | Compose animation; Lottie/Rive khi có asset/hành vi đặc thù |
| Nâng cấp UI hiện có | L1 L2 L3 L7 L9 | Giữ tokens/behavior đã duyệt |
| Lỗi cuộn, keyboard, accessibility | L4 L7 L8 L9 | Insets, semantics, TalkBack, Layout Inspector |

## 2. Decision steps
1. Route màn hình sang mode **operate/read/persuade/experience** của Brain gốc.
2. Kiểm tra quyền, thời điểm xác nhận backend, dữ liệu giả, thiết kế hiện có.
3. Chọn một hệ component nhất quán, không thêm dependencies chỉ để trang trí.
4. Viết 5 trạng thái khi liên quan: **default, loading, empty, error, success**; bổ sung denied/expired cho nghiệp vụ.
5. Tạo preview kèm trường hợp tiếng Việt dài; build/unit/emulator/QA bằng chứng.

## 3. Giao ước tích hợp
- UI: `ui/features/<name>/` và `ui/components/`.
- Domain: quy tắc trạng thái, không gọi Android framework.
- Data: repository/API/DTO; không trả `stockConfirmed=true` từ cache hiển thị.
- State: UI không tự động chuyển từ `REQUEST_RECEIVED` sang `HELD`.
- Trước khi thêm library: kiểm tra [Registry](LIBRARY_REGISTRY.md), giấy phép, size, minSdk, metadata, BOM, Gradle.

## 4. Output contract
Nêu file thay đổi, lý do, screenshots đã xem (nếu có), trạng thái từng gate PASS/FAIL/NOT RUN và việc còn thiếu. Không gắn nhãn production-ready khi chưa có backend và device QA.
