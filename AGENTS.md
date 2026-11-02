> Đọc `docs/PROJECT_STRUCTURE.md` trước khi sửa: một :app, một applicationId, phân quyền ownership, không tạo các thư mục trống và không đưa mock/legacy test code vào APK.

# Agent & contributor working agreement

1. Đọc README → docs/ARCHITECTURE.md → docs/android-ui-brain/ANDROID_ROUTER.md trước khi sửa.
2. Không tự khẳng định đã thanh toán, đã duyệt Rx, đã giữ hàng hoặc đã xác nhận giá/tồn; luôn phân biệt demo và dữ liệu máy chủ.
3. Giữ duy nhất namespace/applicationId `com.pharmasiu.mobile`, source Java trong `app/src/main/java/com/pharmasiu/mobile` và XML trong `res/`. Chia code theo `data/`, `domain/`, `ui/`.
4. Với feature mới: UI thuần Android Java Views/XML nhận state và callbacks; business rules thuần Java trong domain, data fetching tách repository. Không đưa Kotlin/Compose hoặc module app song song trở lại.
5. Không thêm thư viện vì hiệu ứng hình thức; tham chiếu registry, lock dependency version và build/test trước khi merge.
6. Nếu sửa nhiều feature, chia ownership tránh trùng files và giao rõ API chữ ký.
7. Không commit ảnh toa/thông tin khách thật, token, signing key, local.properties hoặc file build.
8. Xác minh: `./gradlew :app:assembleDebug :app:testDebugUnitTest`; native visual QA chỉ PASS khi đã thực sự chạy emulator/device.
9. Một thay đổi = một PR mô tả mục đích, ảnh trước/sau nếu UI, test đã chạy, rủi ro/rollback. Không đẩy thẳng main nếu nhóm đang yêu cầu review.

## Ownership sau refactor Java/XML — 29/10/2026

- **Integration/Navigation:** chỉ người phụ trách chính sửa `MainActivity.java`, `navigation/PharmaBottomBar.java`, manifest, Gradle, theme và API `UiHost`. Navigation sẽ giữ guard quyền; không tạo Activity thứ hai.
- **Customer:** mỗi nhóm một folder `ui/customer/catalog`, `ui/customer/orders`, `ui/customer/account` với XML có tiền tố `catalog_v2_*`, `order_v2_*`, `customer_account_*`.
- **Staff:** mỗi nhóm một folder `ui/staff/pos`, `ui/staff/admin`, `ui/staff/media`; XML tương ứng `staff_pos_*`, `staff_admin_*`, `staff_media_*`.
- **Auth:** `ui/auth` + `auth_unified.xml`; quyền đăng nhập xác định bởi `DemoState.login`, không bằng lựa chọn UI.
- **State/Data:** `core/state`, `data`, `domain`; mọi thay đổi ở `DemoState` cần test role, Rx, FEFO, shift, order và account. Không đưa Android view/resource vào domain.
- **Design system:** `ui/design/PharmaUi`, `res/values/ui_tokens.xml`, `res/drawable/component_*` và icon nav; giữ Material 3 Views và Android XML, pin dependency version.
- **Không sửa chồng lên package người khác.** Thảo luận API façade trước nếu cần liên thông; PR đính kèm build/test và ảnh emulator thực; không gọi thao tác Rx/tồn/thanh toán là thật khi chỉ RAM demo.
- Hướng dẫn chi tiết và ma trận file: `docs/JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md`.

## Database ownership
- SQL files and .sqlite3 fixtures live only in database/. Keep exactly six tables and sync SQLite/PostgreSQL changes.
- No real personal/medical data or plaintext passwords; Rx, FEFO, reservations and payment are demos only until a backend is integrated.
- Execute python database/tools/verify_database.py before merge; PostgreSQL migrations require a real-server test.
