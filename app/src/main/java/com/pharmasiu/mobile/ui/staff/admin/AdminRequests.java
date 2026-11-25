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
        Screen screen = new Screen(a, R.layout.staff_admin_requests, host);
        List<DemoState.Order> orders = s.allOrdersForStaff();
        screen.context(orders.size() + " yêu cầu đã lưu");
        LinearLayout status = card(screen);
        title(status, "Theo dõi yêu cầu");
        int pendingCount = 0, readyCount = 0;
        for (DemoState.Order order : orders) {
            if (pending(order)) pendingCount++;
            if ("Sẵn sàng nhận".equals(order.status)) readyCount++;
        }
        chip(status, pendingCount + " chờ xử lý · " + readyCount + " sẵn sàng", pendingCount > 0);
        warning(status, "Chỉ mô phỏng trạng thái trên thiết bị. Chưa duyệt toa hoặc xác minh thanh toán thật.");
        if (orders.isEmpty()) {
            LinearLayout emptyState = card(screen);
            empty(emptyState, R.drawable.nav_orders, "Chưa có yêu cầu", "Yêu cầu đã lưu của khách sẽ xuất hiện tại đây.");
            return screen.root;
        }

        for (DemoState.Order order : orders) {
            LinearLayout itemCard = card(screen);
            line(itemCard, "Mã " + order.reference, 16, R.color.emerald, true);
            
            boolean isPending = pending(order);
            boolean isReady = "Sẵn sàng nhận".equals(order.status);
            boolean isDone = "Hoàn tất".equals(order.status);
            boolean isCancelled = "Đã hủy".equals(order.status);

            chip(itemCard, order.status, isPending);

            line(itemCard, "Khách hàng: " + (order.customerName.isEmpty() ? "Khách hàng" : order.customerName) + 
                (order.contactPhone.isEmpty() ? "" : " · " + order.contactPhone), 14, R.color.ink, false);
            line(itemCard, "Điểm nhận: " + order.pickup, 13, R.color.muted, false);
            line(itemCard, order.displayTime, 12, R.color.muted, false);
            if (order.containsRx) {
                warning(itemCard, "Rx · Chờ dược sĩ xác minh. Thao tác tại đây chỉ là mô phỏng.");
            }
            divider(itemCard);

            line(itemCard, "Sản phẩm", 14, R.color.ink, true);
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
                button(itemCard, "Chuẩn bị đơn (demo)", true, () -> {
                    if (s.approveOrder(order.reference)) {
                        host.message("Đã cập nhật trạng thái demo cho " + order.reference);
                        host.navigate("requests-admin-v2");
                    } else {
                        host.message("Không thể duyệt đơn này.");
                    }
                });
                button(itemCard, "Từ chối / hủy yêu cầu", false, () -> {
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
                line(itemCard, "Đang chờ nhận (demo)", 13, R.color.forest, true);
                button(itemCard, "Hoàn tất đơn (demo)", true, () -> {
                    if (s.completeOrder(order.reference)) {
                        host.message("Đã hoàn tất demo cho " + order.reference);
                        host.navigate("requests-admin-v2");
                    } else {
                        host.message("Không thể hoàn tất đơn.");
                    }
                });
                button(itemCard, "Hủy yêu cầu", false, () -> {
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
                line(itemCard, "Đã hoàn tất demo", 13, R.color.emerald, true);
            } else if (isCancelled && order.cancelReason != null && !order.cancelReason.isEmpty()) {
                line(itemCard, "Lý do hủy: " + order.cancelReason, 13, R.color.muted, false);
            }
        }
        return screen.root;
    }

    private static boolean pending(DemoState.Order order) {
        return "Đang xử lý".equals(order.status) || "Chờ xử lý".equals(order.status)
            || "Chờ xác minh toa".equals(order.status) || "Chờ gửi và xác minh toa".equals(order.status);
    }
}
