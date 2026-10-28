# Kiến trúc ứng dụng

## Phân lớp
| Layer | Trách nhiệm | Không được làm |
|---|---|---|
| `ui/features` | Composables, màn hình, trạng thái nhập liệu, callbacks | Quyết định giá/tồn/Rx final |
| `ui/components` | Material3 primitives, reusable artwork, trạng thái UI | Gọi API, giữ dữ liệu đơn |
| `ui/components/MedicineMedia.kt` | Adapter Coil: ảnh HTTPS tùy chọn + fallback artwork không mạng | Biến ảnh lỗi thành dữ liệu có tồn kho; tải ảnh toa khách |
| `ui/theme` | Brand tokens Emerald, Lime, typography, shapes | Nghiệp vụ |
| `domain` | Pure Kotlin cart rules, state machine | Android Context, Retrofit, database |
| `data/model` | Data class công khai | Nhét logic UI |
| `data/repository` | Demo data; adapter API thật trong tương lai | Báo giả dữ liệu là real-time |
| `ui/PharmaSIUApp.kt` | Root composition, stack demo, launch Android picker | Persist order thật |

Hiện nay navigation stack, cart, hồ sơ demo, yêu thích, sổ địa chỉ, toa và các bản xem trước yêu cầu nằm trong RAM của root Composable để prototype dễ đọc; **chưa dùng ViewModel, Room, Hilt hoặc Navigation Compose**. Chỉ cờ onboarding được lưu vào Preferences. Trước khi kết nối backend, chuyển state sang feature ViewModel, repository interface, secure storage và xử lý process death/back stack sâu hơn.

**Mở rộng v0.2:** `data/repository/ExpandedMockCatalog.kt` bổ sung 24 sản phẩm ngoài 6 mẫu ban đầu; `domain/CatalogSearchRules.kt` sở hữu lọc/tìm kiếm/sắp xếp; `data/model/MockCommerce.kt` có MockProfile, MockAddress và MockRequest; `ui/features/account/` sở hữu auth/hồ sơ/địa chỉ/mật khẩu; `ui/features/community/` sở hữu yêu thích và lịch sử yêu cầu mẫu. Không lưu credential demo vào Prefs.

**Đồng bộ component:** Header, ProductTile, MedicineCard, FeatureButton và artwork nay thuộc `ui/components/`; feature screens chỉ import từ package này, không import component từ một feature khác. Khi nhận ảnh catalog chính thức, đặt URL ở `Medicine.imageUrl`; adapter chỉ chấp nhận HTTPS và dùng ảnh minh họa lúc đang tải/lỗi.

## Contract dữ liệu và trạng thái
- Số lô/hạn dùng, FEFO, kiểm tra tồn khả dụng và hạn giữ hàng do backend chịu trách nhiệm.
- Tạo yêu cầu → máy chủ trả `REQUEST_RECEIVED`. Nếu Rx, tiếp tục `RX_REVIEW_PENDING`, `RX_NEEDS_UPDATE` hoặc `RX_REJECTED`; chỉ sau thẩm định mới chuyển sang kiểm tra khả dụng.
- `STOCK_CONFIRMATION_PENDING` → `HELD` (có TTL) → `PREPARING` → `READY_FOR_PICKUP` → `COMPLETED`. Các trạng thái `CANCELLED`, `EXPIRED` xử lý từ backend.
- Máy khách chỉ hiển thị trạng thái đã được backend trả về; hệ thống demo **không** giả lập API xác nhận.
- Giá dự kiến không được cộng VAT lần hai; giá cuối cùng và chứng từ do backend POS xác minh.

## Khi thêm API
Ưu tiên Retrofit/OkHttp hoặc Ktor theo phiên bản Android, DTO mapper sang domain, TLS, xác thực, idempotency key cho submit, retry có kiểm soát, pagination/search và trạng thái lỗi. Ảnh toa phải có auth, phân quyền, giới hạn định dạng/dung lượng và chính sách lưu trữ rõ ràng.

## Trọng tâm test
- Domain: không skip RX review hay bước giữ hàng, cart Rx phải đính kèm toa.
- UI: search/filter, thêm/bớt giỏ, system Back, ảnh bị hủy chọn, form invalid/valid, review nội dung trước submit.
- Integration khi có backend: double submit, hết TTL, stale stock, 403, mạng chập chờn, upload mờ, kiểm soát quá hạn.
