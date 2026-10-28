# Hướng dẫn cộng tác

## Chia sở hữu theo vùng mã
| Nhánh phụ trách | Package/đường dẫn | Review cần có |
|---|---|---|
| Android foundation | `app/build.gradle.kts`, `gradle/`, `MainActivity.kt` | Build và dependency |
| UI/UX & Brand | `ui/theme/`, `ui/components/`, `docs/android-ui-brain/` | Visual + a11y |
| Browse/catalog | `ui/features/home/`, `catalog/`, `product/` | Search, thông tin thuốc |
| Request/checkout | `ui/features/cart/`, `prescription/`, `checkout/`, `orders/` | Rx + stock/status |
| Domain + API | `domain/`, `data/`, `src/test/` | Business rules, privacy, contracts |
| Integration | `ui/PharmaSIUApp.kt`, `docs/QA_REPORT.md` | End-to-end smoke và regression |

Ownership là **đề xuất theo module**, nhóm có thể phân người sau; không gán cá nhân cứng vào source.

## Git workflow
1. `git pull --ff-only origin main` trước khi làm.
2. Tạo nhánh `feature/catalog-search`, `feature/rx-upload`, `fix/cart-quantity` hoặc `docs/android-brain`.
3. Commit thay đổi cùng test; không định dạng lại những feature không liên quan.
4. PR với mục tiêu, hình màn hình Android (nếu có), các lệnh đã kiểm tra, điều còn chưa chạy.
5. Review domain/Rx và QA trước merge. Không force-push main; rebase/conflict cần kiểm tra lại.

## Style ngắn gọn
- Tên composable `XxxScreen`, callback `onAction`, data class `Xxx`, state enum UPPER_SNAKE_CASE.
- UI không tự gọi repository trong component chung; tránh composable quá dài, tách section theo ý nghĩa.
- Nhãn tiếng Việt rõ, ghi đơn vị VND, không ẩn lỗi/điều khoản dưới animation.
- Đưa màu vào `PharmaPalette`, version vào `libs.versions.toml`; tài nguyên ảnh/animation phải có quyền sử dụng.
