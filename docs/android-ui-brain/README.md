# Android UI Brain — extension cho UI UX Brain v4.1

**Mục đích:** chuyển quy tắc UI UX Brain thành quyết định triển khai Kotlin/Jetpack Compose có thể kiểm chứng. Đây **không** phải một framework runtime, Gradle plugin, hay công cụ tự cài toàn bộ thư viện.

Đọc [ANDROID_ROUTER.md](ANDROID_ROUTER.md) → nạp layer được chỉ định → đọc mã nguồn thật → triển khai → đánh giá bằng [QUALITY_REPORT](QUALITY_REPORT.template.md) và [QUALITY_GATES.md](../QUALITY_GATES.md).

**Hợp đồng:** sự thật nghiệp vụ và bảo mật quan trọng hơn UI; dữ liệu mẫu không được nhận là dữ liệu thật. Với sản phẩm liên quan sức khỏe, không thể dùng màu sắc hay animation để ám chỉ toa đã duyệt hoặc hàng đã giữ. Không có runtime/emulator thì gate đó phải đánh dấu **NOT RUN**.

| Layer | Tài liệu | Kết quả mong đợi |
|---|---|---|
| L1 | [Tokens](layers/L1_design_tokens.md) | Palette, type, shape, spacing, semantic colors |
| L2 | [Visual](layers/L2_visual_composition.md) | Bố cục editorial, hierarchy, density |
| L3 | [Components](layers/L3_component_system.md) | Compose Material 3, primitive và reusable UI |
| L4 | [Interaction](layers/L4_navigation_states.md) | navigation, back, keyboard, loading/error |
| L5 | [Motion](layers/L5_motion.md) | Animation có mục đích, reduced motion |
| L6 | [Media](layers/L6_media_and_charts.md) | Coil/Lottie/Rive/Vico khi cần |
| L7 | [A11y](layers/L7_accessibility.md) | TalkBack, contrast, touch, dynamic type |
| L8 | [Performance](layers/L8_performance.md) | Recomposition, images, memory, startup |
| L9 | [QA](layers/L9_visual_qa.md) | Emulator, screenshot compare, gates |

Chỉ ứng dụng các layer liên quan tác vụ; không ép dự án thêm Rive, Lottie hay Vico nếu chưa có nhu cầu. Không ghi khóa truy cập/tài khoản vào repository.
