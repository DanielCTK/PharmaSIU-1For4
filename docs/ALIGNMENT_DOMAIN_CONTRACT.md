# PHARMASIU — HỢP ĐỒNG DOMAIN / STATE / ACTIVITY (KHÔNG DATABASE)

## 1. State chính từ bản vẽ
CHỜ XỬ LÝ; CHỜ DUYỆT TOA; TỪ CHỐI; ĐÃ GIỮ HÀNG; SẴN SÀNG NHẬN; HOÀN TẤT; ĐÃ HỦY.

Code có state chi tiết hơn, giữ chi tiết này nhưng phải có ánh xạ nhãn và lý do:
| State Kotlin | Nhóm STATE | Chú ý |
|---|---|---|
| REQUEST_RECEIVED, STOCK_CONFIRMATION_PENDING | CHỜ XỬ LÝ | Chưa giữ |
| RX_REVIEW_PENDING, RX_NEEDS_UPDATE | CHỜ DUYỆT TOA | Chưa duyệt |
| RX_REJECTED | TỪ CHỐI | Không giao |
| HELD, PREPARING | ĐÃ GIỮ HÀNG | PREPARING là bước soạn nội bộ |
| READY_FOR_PICKUP | SẴN SÀNG NHẬN | Chưa giao |
| COMPLETED | HOÀN TẤT | Thanh toán + giao hàng |
| CANCELLED | ĐÃ HỦY | Lý do hủy |
| EXPIRED | ĐÃ HỦY (quá hạn) | Phân biệt nguyên nhân quá hạn |

SRS chi tiết có PENDING -> UNDER_REVIEW -> APPROVED -> RESERVED -> PICKING -> READY -> COMPLETED; các nhánh REJECTED/CANCELLED/EXPIRED. Bước APPROVED của Rx KHÁC bước RESERVED. Không tự suy diễn trạng thái này trong ứng dụng khi chưa có backend.

## 2. Activity Click & Collect
Chọn sản phẩm -> gửi yêu cầu và ảnh toa nếu Rx -> CHỜ XỬ LÝ -> người có quyền thẩm định Rx -> kiểm tra toàn bộ lô, hạn, chất lượng, tồn -> giữ ĐỦ toàn đơn có thời hạn -> soạn theo lô giữ -> SẴN SÀNG NHẬN -> xác minh mã -> xác nhận thu tiền -> xuất lô đúng một lần -> bàn giao -> HOÀN TẤT.
Nhánh ngoại lệ: Rx không hợp lệ = TỪ CHỐI; thiếu hàng = rollback toàn bộ phần giữ, TỪ CHỐI; thanh toán lỗi = KHÔNG xuất, KHÔNG hoàn tất; hủy hoặc hết TTL = nhả tồn đúng một lần nếu có backend có thẩm quyền.

## 3. Activity POS
Chọn/quét -> tra giá/lô/hạn/tồn -> kiểm Rx theo quyền -> ưu tiên FEFO -> đối chiếu lô thực tế -> đối chiếu số tiền và thu tiền -> xác minh giao dịch -> ghi xuất đúng một lần -> giao hàng và chứng từ -> HOÀN TẤT.
Thiếu tồn, hết hạn/cách ly, Rx chưa duyệt, hoặc thanh toán thất bại đều phải chặn trước bước xuất kho.

## 4. Quyền và cách hiển thị dữ liệu
- KHÁCH: tra cứu/đặt trước/theo dõi-hủy; mọi giá và khả dụng khi chưa có API chỉ là tham khảo hoặc chưa xác nhận.
- NHÂN VIÊN: POS và xử lý yêu cầu; không tự duyệt Rx.
- NGƯỜI CÓ QUYỀN DƯỢC SĨ: mới có quyền thẩm định Rx; quyền admin không thay thế thẩm định.
- QUẢN TRỊ VIÊN: sản phẩm/lô/kiểm kê/tài khoản/báo cáo/đối soát. Mô phỏng phải ghi demo và không ngầm cập nhật kho thật.
- DEMO request tồn tại tại máy, không gửi tới quầy thực; không tự hiện HELD/READY/PAID.
- Chọn tệp toa = local file; không ghi 'đã tải lên' nếu chưa có upload API.
- Tồn staff mock không tự đồng bộ với tồn khách; không tuyên bố còn hàng khi không có bằng chứng server.

## 5. Bài test bắt buộc
Rx không có toa không checkout; ảnh local không tính là upload; không nhảy từ REQUEST_RECEIVED sang HELD; chưa xác nhận thanh toán không COMPLETED; không bán Rx từ cashier; không âm tồn; lô cách ly/hết hạn không bán khi có logic lô; quá hạn/hủy không tự cho giao; không đóng ca có giỏ chưa xử lý; login/logout/back không rò phiên; báo cáo demo có chú thích phạm vi dữ liệu.

