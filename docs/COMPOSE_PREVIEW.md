# Xem trước giao diện Java/XML trong Android Studio

Dự án PharmaSIU hiện sử dụng Java Views và XML trong module `:app`. Các Kotlin Compose Preview trước đây không còn trong project.

Mở Android Studio → **Project → app/src/main/res/layout/** → chọn một `.xml` → tab **Design** hoặc **Split**. Dùng Preview Device/Configuration để xem kích thước màn hình, fontScale và orientation. Với các view tạo theo dữ liệu bằng Java, cần chạy ứng dụng trên emulator/thiết bị để xem đúng dữ liệu động và ảnh Canvas.

Một số layout: `auth_unified.xml` (đăng nhập), `catalog_v2_home.xml`, `catalog_v2_catalog.xml`, `catalog_v2_product.xml`, `order_v2_cart.xml`, `order_v2_checkout.xml`, `staff_pos_page.xml`, `staff_admin_dashboard.xml`, `staff_admin_catalog.xml`, `staff_media_preview.xml`.

Hướng dẫn build: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`. Chọn cấu hình `app` trong Android Studio; không mở một module Java/Kotlin song song.
