# L9 — Native visual verification

1. Build `assembleDebug` / `testDebugUnitTest`; screenshot chỉ sau khi app chạy emulator.
2. Duyệt flows Home → Catalog → PDP → Cart → Rx → Checkout → Preview; tất cả trạng thái quan trọng.
3. Kích thước 320/360/390/412 dp, fontScale 1.0/1.5/2.0, portrait/landscape nếu hỗ trợ.
4. TalkBack, on-screen keyboard, system Back, no network, stale stock, Rx reject, expiry, duplicate submit.
5. So sánh screenshot đã mở xem với thiết kế; ghi PASS/FAIL/NOT RUN cho từng gate, P0/P1 không được che bằng điểm thẩm mỹ.
6. Chưa có backend/emulator: nêu rõ giới hạn, không gọi bản chạy thử là bản production.
