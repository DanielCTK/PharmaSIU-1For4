# L4 — Interaction / navigation / state

- Mọi CTA có đích; Back vật lý và Back top bar phải không bỏ dữ liệu người dùng bất ngờ.
- Form: validation gắn trường, hạn chế gửi lặp, handle loading/retry/timeouts.
- Rx: upload ảnh != approve; gửi request != hold; HELD != READY; thời hạn giữ phải do server cung cấp.
- Network: server là nguồn sự thật cho giá, tồn, permission, Rx; optimistic UI chỉ dùng khi có rollback và không ảnh hưởng lâm sàng.
- Khi chuyển sang Navigation Compose, viết graph route và deep link contract, kiểm thử process death.
