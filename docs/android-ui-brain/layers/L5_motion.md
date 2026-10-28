# L5 — Motion

- Ưu tiên Compose Animation cho feedback nhỏ: thêm giỏ, expanded panels, skeleton khi tải.
- Lottie với JSON gốc được phép tái sử dụng, Rive với state machine tương tác; cả hai cần asset, footprint và lifecycle QA.
- Thiết kế motion có mục đích: trạng thái/toạ độ chuyển động giúp người dùng hiểu thay đổi, không che thông tin Rx hoặc lỗi.
- Hỗ trợ hệ thống giảm chuyển động: nếu không có cơ chế automatic, cung cấp setting tắt; không dùng loading animation vô hạn làm bằng chứng thành công.
