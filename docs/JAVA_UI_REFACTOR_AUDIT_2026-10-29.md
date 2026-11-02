# Audit sau modular refactor — PharmaSIU Java/XML

**Ngày:** 29/10/2026. Đánh giá source và những luồng đã chạy trên emulator API28; những mục chưa thử trên thiết bị thật không được coi là PASS.

## Đã xử lý bằng code

- Loại 4 Java màn legacy và 4 XML layout không còn nằm trong luồng chính; chỉ còn router/facade theo role.
- Tách 7 nhóm feature (catalog, orders, account, POS, admin, auth, staff media) và core state (account/inventory).
- Dùng design system chung `PharmaUi` + Material 3, 26 XML MaterialButton, bottom bar vector-icons; POS input có outline và trạng thái focus.
- Ngăn việc UI thay đổi quyền, xác nhận Rx, giữ hàng hay tiền thật. Giữ cảnh báo RAM DEMO và quyền tại store/lựa chọn ảnh.
- 141 JUnit pass, 3 role route smoke trên APK refactor (login, Customer Home/Account/Cart, Staff POS, Admin Dashboard). Chưa có full E2E.

## Rủi ro và backlog ưu tiên

| Mức | Vấn đề / giới hạn | Hướng xử lý |
|---|---|---|
| P1 | `DemoState` vẫn ~470 dòng, giữ nhiều mutable public collections | Tách tiếp POS/Order stores, thêm tests snapshot/ownership; không vội vì ảnh hưởng parity |
| P1 | `LegacyDomainRules` ~335 dòng đang được test dùng, tồn tại cùng domain Java mới | Lập mapping chức năng và chuyển test trước khi loại bỏ, tránh phá hồi quy |
| P1 | Bộ ảnh sản phẩm phần lớn là Canvas minh họa | Với media có bản quyền/nguồn xác minh, bổ sung pipeline ảnh thật được duyệt; không bịa thông tin thuốc |
| P1 | Danh mục và POS có thao tác tạo View theo từng item, chưa dùng RecyclerView | Khi tăng quy mô dữ liệu hoặc kết nối server, dùng RecyclerView + ListAdapter/diff để tối ưu |
| P1 | Nhiều chuỗi UI hardcode ở Java/XML | Tách ra `strings.xml`, hỗ trợ i18n/tìm kiếm accessibility |
| P1 | Chưa chạy 320dp/412dp, fontScale 1.5, landscape và TalkBack trên mọi screen | Lập ma trận smoke và screenshot regression, sửa clipping/target/tương phản |
| P1 | File picker vẫn dùng API `startActivityForResult` cũ | Chuyển sang Activity Result API Java khi có thời gian QA trên Android phiên bản mới |
| P1 | Nhận hàng, Rx/pharmacist, tồn/giữ hàng/tiền đều demo RAM | Chỉ dựng UI trạng thái chờ tích hợp, **không** tự tuyên bố vận hành |
| P1 | Admin tạo mặt hàng RAM bằng API UI façade rồi thêm vào state | Đưa mutation sang store/domain khi tích hợp, enforce quyền/tham chiếu tồn ở một chỗ |
| P2 | Vẫn còn một số Android native EditText, alert/Toast ở các màn ngoài POS | Thống nhất Material outlined fields, Snackbar và error support text theo design system |
| P2 | Phần lớn UI animations/transitions chưa đánh giá trên máy yếu | Profile frame time, giữ reduced motion và tránh hiệu ứng nặng |
| P2 | Dependency Material 1.14.0 đang ở chế độ bảo trì | Pin version, kiểm soát license/vulnerability, không quay lại Kotlin/Compose nếu chưa có quyết định kỹ thuật mới |

## Ma trận review PR bắt buộc

- [ ] Không sửa file sở hữu của module khác; không đổi appId, không thêm Kotlin.
- [ ] Có test nghiệp vụ phù hợp; `:app:clean :app:assembleDebug :app:testDebugUnitTest` PASS.
- [ ] Smoke màn chính với đúng role và trạng thái chưa đăng nhập/không quyền.
- [ ] Kiểm tra thông báo `DEMO / CHỜ TÍCH HỢP` với Rx, POS, tồn, phiếu.
- [ ] Kiểm tra 320dp và 390dp, fontScale, TalkBack, focus/keyboard, empty/loading/error states.
- [ ] Ảnh/screenshot UI sau sửa phải do emulator chạy code thực; không thay bằng mock ảnh sinh tự động.
