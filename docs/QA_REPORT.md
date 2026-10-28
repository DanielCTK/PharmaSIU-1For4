# QA log — PharmaSIU Mobile 0.1.0

| Gate | Trạng thái | Bằng chứng/việc còn thiếu |
|---|---|---|
| G0 Truth & scope | REVIEWED | Tất cả màn hình gắn nhãn demo; không giả xác nhận tồn hoặc toa |
| G1 User flow | REVIEWED | Home → Catalog → PDP → Cart → Rx (nếu cần) → Checkout → Preview có callback |
| G2 Accessibility | NOT RUN | Cần TalkBack, 48dp targets và font scale trên emulator |
| G3 Visual | NOT RUN | Cần ảnh chụp 320/390/412dp và review thủ công |
| G4 Error/recovery | PARTIAL | Search empty, form invalid, no Rx file đã được xử lý; network chưa tích hợp |
| G5 Runtime | NOT RUN | Cập nhật khi build và test hoàn tất |
| G6 Consistency | REVIEWED | Màu, typography và shared components dựa PharmaPalette |

Không tuyên bố production ready từ kết quả review mã nguồn. Các gate NOT RUN không phải PASS.
