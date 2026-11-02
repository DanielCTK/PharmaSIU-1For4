package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import java.util.List;
import java.util.Map;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;

/** Single feature screen and its closely related business UI. */
public final class AdminRequests {
    private AdminRequests() {}

    public static View requests(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_requests);
        LinearLayout status = card(screen);
        title(status, "Quản lý đơn đặt hàng");
        line(status, "HỆ THỐNG DUYỆT ĐƠN PHARMASIU", 16, R.color.emerald, true);
        warning(status, "Dược sĩ kiểm tra đơn thuốc, thẩm định và chuẩn bị đơn trước khi giao cho khách hàng.");

        List<DemoState.Order> orders = s.allOrdersForStaff();
        if (orders.isEmpty()) {
            LinearLayout empty = card(screen);
            title(empty, "Danh sách đơn đặt hàng");
            line(empty, "Hiện chưa có đơn đặt hàng nào từ khách hàng.", 13, R.color.muted, false);
            return screen.root;
        }

        LinearLayout countCard = card(screen);
        title(countCard, "Tổng số đơn hàng: " + orders.size());
        line(countCard, "Dược sĩ bấm Duyệt đơn để chuẩn bị thuốc cho khách nhận tại quầy.", 13, R.color.forest, false);

        for (DemoState.Order order : orders) {
            LinearLayout itemCard = card(screen);
            line(itemCard, "ĐƠN HÀNG #" + order.reference, 12, R.color.emerald, true);
            
            boolean isPending = "Đang xử lý".equals(order.status) || "Chờ xử lý".equals(order.status) 
                             || "Chờ xác minh toa".equals(order.status) || "Chờ gửi và xác minh toa".equals(order.status);
            boolean isReady = "Sẵn sàng nhận".equals(order.status);
            boolean isDone = "Hoàn tất".equals(order.status);
            boolean isCancelled = "Đã hủy".equals(order.status);

            int statusColor = isReady ? R.color.emerald : isDone ? R.color.forest : isCancelled ? R.color.muted : R.color.ink;
            line(itemCard, "Trạng thái: " + order.status, 15, statusColor, true);

            line(itemCard, "Khách hàng: " + (order.customerName.isEmpty() ? "Khách hàng" : order.customerName) + 
                (order.contactPhone.isEmpty() ? "" : " · " + order.contactPhone), 14, R.color.ink, false);
            line(itemCard, "Địa chỉ nhận: " + order.pickup, 13, R.color.muted, false);
            line(itemCard, "Thời gian đặt: " + order.displayTime, 12, R.color.muted, false);
            if (order.containsRx) {
                line(itemCard, "⚠️ Thuốc kê đơn (Rx) · Cần Dược sĩ kiểm tra đơn thuốc trước khi giao", 12, R.color.forest, true);
            }
            divider(itemCard);

            line(itemCard, "Sản phẩm trong đơn:", 13, R.color.ink, true);
            for (Map.Entry<String, Integer> line : order.lines.entrySet()) {
                String pName = order.productNamesSnapshot != null && order.productNamesSnapshot.containsKey(line.getKey())
                        ? order.productNamesSnapshot.get(line.getKey())
                        : line.getKey();
                long price = order.unitPricesSnapshot != null && order.unitPricesSnapshot.containsKey(line.getKey())
                        ? order.unitPricesSnapshot.get(line.getKey())
                        : 0L;
                line(itemCard, "• " + line.getValue() + " x " + pName + (price > 0 ? " (" + vnd(price) + ")" : ""), 13, R.color.ink, false);
            }
            divider(itemCard);
            line(itemCard, "Tổng tiền: " + vnd(order.pricedSubtotalVnd), 16, R.color.emerald, true);

            if (isPending) {
                button(itemCard, "✓  Duyệt đơn & Chuẩn bị thuốc", true, () -> {
                    if (s.approveOrder(order.reference)) {
                        host.message("Đã duyệt đơn " + order.reference + " thành công!");
                        host.navigate("requests-admin-v2");
                    } else {
                        host.message("Không thể duyệt đơn này.");
                    }
                });
                button(itemCard, "✕  Từ chối / Hủy đơn", false, () -> {
                    new AlertDialog.Builder(a)
                        .setTitle("Hủy đơn hàng")
                        .setMessage("Bạn có chắc chắn muốn từ chối/hủy đơn hàng " + order.reference + "?")
                        .setNegativeButton("Quay lại", null)
                        .setPositiveButton("Hủy đơn", (d, w) -> {
                            if (s.rejectOrder(order.reference, "Dược sĩ từ chối hoặc hết hàng")) {
                                host.message("Đã hủy đơn " + order.reference);
                                host.navigate("requests-admin-v2");
                            } else {
                                host.message("Không thể hủy đơn.");
                            }
                        }).show();
                });
            } else if (isReady) {
                line(itemCard, "● Thuốc đã soạn xong, đang chờ khách đến nhận.", 13, R.color.forest, true);
                button(itemCard, "📦  Giao thuốc & Hoàn tất đơn hàng", true, () -> {
                    if (s.completeOrder(order.reference)) {
                        host.message("Đã hoàn tất đơn hàng " + order.reference + "!");
                        host.navigate("requests-admin-v2");
                    } else {
                        host.message("Không thể hoàn tất đơn.");
                    }
                });
                button(itemCard, "✕  Hủy đơn hàng này", false, () -> {
                    new AlertDialog.Builder(a)
                        .setTitle("Hủy đơn hàng")
                        .setMessage("Khách không đến nhận hoặc muốn hủy đơn " + order.reference + "?")
                        .setNegativeButton("Quay lại", null)
                        .setPositiveButton("Xác nhận hủy", (d, w) -> {
                            if (s.rejectOrder(order.reference, "Khách không đến nhận")) {
                                host.message("Đã hủy đơn " + order.reference);
                                host.navigate("requests-admin-v2");
                            }
                        }).show();
                });
            } else if (isDone) {
                line(itemCard, "✓  Đơn hàng đã hoàn tất giao dịch.", 13, R.color.emerald, true);
            } else {
                line(itemCard, "✕  Đơn hàng đã hủy" + (order.cancelReason != null ? ": " + order.cancelReason : "."), 13, R.color.muted, false);
            }
        }
        return screen.root;
    }
}
