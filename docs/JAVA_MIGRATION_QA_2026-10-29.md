> **Lưu ý phiên bản:** Kết quả bàn giao bên dưới ghi nhận giai đoạn chuyển từ Kotlin sang Java trước khi tái cấu trúc. Cấu trúc module mới và số lượng file hiện hành nằm tại [JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md](JAVA_UI_REFACTOR_HANDOFF_2026-10-29.md).

# Kiểm chứng Java/XML cuối cùng — 29/10/2026

**Đối tượng nghiệm thu:** original root `C:\PharmaSIU\PharmaSIU-1For4`, một Gradle module `:app`.

- `:app:clean :app:assembleDebug :app:testDebugUnitTest --offline --no-daemon --console=plain` → **BUILD SUCCESSFUL**.
- Android JUnit XML report: **29 suites, 141/141 tests PASS**, failures=0, errors=0, skipped=0.
- **50 Java production files, 29 Java test files, 52 XML files**, **0 Kotlin .kt/.kts** trong source/repository.
- **Namespace và appId** `com.pharmasiu.mobile`; chỉ một launcher `com.pharmasiu.mobile.MainActivity`; không còn module thử nghiệm.
- Android Emulator API28: `adb install -r app/build/outputs/apk/debug/app-debug.apk` thành công; `am start -n com.pharmasiu.mobile/.MainActivity` thành công; UI login mới hiển thị, đăng nhập khách hàng `0900000000`, vào trang chủ; no FATAL trong phần logcat kiểm tra.
- Đã kiểm tra trực tiếp trên APK **sau khi chuyển package và xóa Kotlin**: Khách hàng đăng nhập → Trang chủ; Nhân viên DEMO đăng nhập → POS (ca chưa mở, thông báo RAM); Quản trị viên đăng nhập → dashboard → Hàng hóa. Cả ba phân quyền mở được, PID tồn tại và không thấy FATAL EXCEPTION ở logcat đã truy xuất.
- Gradle được kiểm tra lại sau khi bỏ `org.gradle.java.home` khóa theo máy; local build/test vẫn thành công. GitHub Actions JDK17 trên Ubuntu **chưa chạy** trong lần kiểm chứng này.
- Một số trường hợp trước chuyển package đã smoke riêng theo vai trò: giỏ/yêu cầu khách, ca & POS demo, màn quản trị/hàng hóa/lô/đối soát; không đánh dấu full end-to-end trên APK cuối.

### Phạm vi các hợp đồng nghiệp vụ được kiểm thử

Đăng nhập/role separation, giỏ/số lượng, danh mục 30 seed, lọc tìm kiếm không dấu, trạng thái đơn, fingerprint Rx, lưu yêu cầu/chi tiết lịch sử, tính giá, FEFO/quarantine/lô hết hạn, bảo vệ dự thảo POS, phiếu demo snapshot, số tiền nhận/thối, đối soát sau đóng ca, photo URI cục bộ và validation lỗi.

### Chưa có chứng cứ PASS

Khác biệt UI nhỏ so với Compose (full pixel/motion), màn 320/412dp, fontScale 1.5x, TalkBack, UI full E2E sau package rename, Android 14/15, toàn bộ file-picker permission flow và hiệu năng/giảm tiêu thụ bộ nhớ.

Cần chạy lại các kịch bản trên thiết bị thật trước khi phát hành. Không có backend/database/dược sĩ duyệt toa/thanh toán thực nên không thể xác nhận các nghiệp vụ đó.
