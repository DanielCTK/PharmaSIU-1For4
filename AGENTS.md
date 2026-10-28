# Agent & contributor working agreement

1. Đọc README → docs/ARCHITECTURE.md → docs/android-ui-brain/ANDROID_ROUTER.md trước khi sửa.
2. Không tự khẳng định đã thanh toán, đã duyệt Rx, đã giữ hàng hoặc đã xác nhận giá/tồn; luôn phân biệt demo và dữ liệu máy chủ.
3. Giữ packages trong `com.pharmasiu.mobile`, chia code theo `data/`, `domain/`, `ui/components/`, `ui/features/`.
4. Với feature mới: UI composable chỉ nhận state và callback, business rules thuần Kotlin trong domain, data fetching tách repository.
5. Không thêm thư viện vì hiệu ứng hình thức; tham chiếu registry, lock dependency version và build/test trước khi merge.
6. Nếu sửa nhiều feature, chia ownership tránh trùng files và giao rõ API chữ ký.
7. Không commit ảnh toa/thông tin khách thật, token, signing key, local.properties hoặc file build.
8. Xác minh: `./gradlew :app:assembleDebug :app:testDebugUnitTest`; native visual QA chỉ PASS khi đã thực sự chạy emulator/device.
9. Một thay đổi = một PR mô tả mục đích, ảnh trước/sau nếu UI, test đã chạy, rủi ro/rollback. Không đẩy thẳng main nếu nhóm đang yêu cầu review.
