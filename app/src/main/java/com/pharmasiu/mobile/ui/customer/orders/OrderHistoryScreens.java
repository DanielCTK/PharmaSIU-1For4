package com.pharmasiu.mobile.ui.customer.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import static com.pharmasiu.mobile.ui.customer.CustomerOrderViews.*;

/** History, detail, read-only receipt and local cancel confirmation. */
abstract class OrderHistoryScreens extends OrderCartScreens {
    OrderHistoryScreens(Activity activity,UiHost host,int layout){super(activity,host,layout);}
    protected void orders() {
        headerActions("Yêu cầu của tôi", "Tiếp tục mua sắm", "catalog");
        List<DemoState.Order> mine = state.customerOrders();
        if (mine.isEmpty()) {
            LinearLayout empty = card();
            illustration(empty, R.drawable.order_empty_requests);
            line(empty, "Chưa có yêu cầu", 21, R.color.forest, true).setGravity(android.view.Gravity.CENTER);
            line(empty, "Yêu cầu bạn lưu sẽ xuất hiện ở đây.", 14, R.color.muted, false)
                .setGravity(android.view.Gravity.CENTER);
            iconAction(empty, "Khám phá sản phẩm", R.drawable.order_icon_arrow_right, true,
                () -> host.navigate("catalog"));
            disclosure();
            return;
        }
        chip(body, mine.size() + " yêu cầu", R.drawable.order_icon_receipt, false);
        disclosure();
        for (DemoState.Order order : mine) {
            LinearLayout item = card();
            chip(item, status(order), statusIcon(order), isPending(order));
            line(item, order.reference, 18, R.color.forest, true);
            iconLine(item, R.drawable.order_icon_clock, order.displayTime, R.color.muted);
            productStrip(item, order);
            LinearLayout total = new LinearLayout(activity);
            boolean stacked = activity.getResources().getConfiguration().fontScale >= 1.3f
                || activity.getResources().getConfiguration().screenWidthDp < 360;
            total.setOrientation(stacked ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            total.setGravity(android.view.Gravity.CENTER_VERTICAL);
            TextView count = new TextView(activity);
            count.setText(units(order.lines) + " sản phẩm");
            count.setTextSize(14);
            count.setTextColor(activity.getColor(R.color.muted));
            total.addView(count, stacked ? new LinearLayout.LayoutParams(-1, -2)
                : new LinearLayout.LayoutParams(0, -2, 1));
            TextView amount = new TextView(activity);
            amount.setText(money(order.pricedSubtotalVnd));
            amount.setTextSize(17);
            amount.setTypeface(null, Typeface.BOLD);
            amount.setTextColor(activity.getColor(R.color.emerald));
            LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(-2, -2);
            if (stacked) amountParams.topMargin = dp(6);
            total.addView(amount, amountParams);
            item.addView(total, new LinearLayout.LayoutParams(-1, -2));
            if (order.containsUnpriced)
                iconLine(item, R.drawable.order_icon_info, "Còn mặt hàng chờ báo giá", R.color.warning);
            linkAction(item, "Xem chi tiết", R.drawable.order_icon_arrow_right, () -> {
                state.selectedOrderId = order.reference;
                host.navigate("order-detail-v2");
            });
        }
    }

    protected void detail() {
        headerActions("Chi tiết yêu cầu", "Danh sách yêu cầu", "orders-v2");
        DemoState.Order order = selectedCustomerOrder(state);
        if (order == null) { missingOrder(); return; }
        LinearLayout summary = card(true);
        chip(summary, status(order), statusIcon(order), isPending(order));
        line(summary, order.reference, 23, R.color.forest, true);
        iconLine(summary, R.drawable.order_icon_clock, order.displayTime, R.color.muted);
        if (order.cancelReason != null && !isPending(order))
            iconLine(summary, R.drawable.order_icon_close, order.cancelReason, R.color.muted);
        disclosure();
        LinearLayout goods = card();
        heading(goods, "Sản phẩm");
        showLines(goods, order.lines, order);
        priceSummary(goods, order.pricedSubtotalVnd, order.containsUnpriced);
        if (order.containsRx)
            iconLine(goods, R.drawable.order_icon_receipt, "Toa chưa được dược sĩ thẩm định.", R.color.warning);

        LinearLayout progress = card();
        heading(progress, "Tiến trình");
        if (cancelled(order)) {
            timeline(progress, R.drawable.order_icon_check, "Đã lưu trên thiết bị", order.displayTime, true);
            timeline(progress, R.drawable.order_icon_close, "Đã hủy tại máy", "Chưa gửi lệnh hủy đến nhà thuốc", false);
        } else {
            boolean prepared = "Sẵn sàng nhận".equals(order.status) || "Hoàn tất".equals(order.status);
            boolean done = "Hoàn tất".equals(order.status);
            timeline(progress, R.drawable.order_icon_check, "Đã lưu yêu cầu", order.displayTime, true);
            timeline(progress, prepared ? R.drawable.order_icon_check : R.drawable.order_icon_clock,
                prepared ? "Đã đánh dấu chuẩn bị (demo)" : "Chờ nhà thuốc xác nhận", null, prepared);
            timeline(progress, done ? R.drawable.order_icon_check : R.drawable.order_icon_pin,
                done ? "Hoàn tất (demo)" : "Chưa xác nhận nhận hàng", null, done);
        }
        linkAction(progress, "Làm mới", R.drawable.order_icon_refresh, () -> {
            state.customerOrders();
            host.navigate("order-detail-v2");
            host.message("Đã làm mới thông tin đơn hàng");
        });

        LinearLayout contact = card();
        heading(contact, "Điểm nhận & liên hệ");
        iconLine(contact, R.drawable.order_icon_person, order.customerName, R.color.ink);
        iconLine(contact, R.drawable.order_icon_phone, order.contactPhone, R.color.ink);
        iconLine(contact, R.drawable.order_icon_pin, order.pickup, R.color.ink);
        LinearLayout actions = card();
        iconAction(actions, "Thêm lại vào giỏ", R.drawable.order_icon_cart, true, () -> {
            if (state.reorderRequest(order.reference)) {
                host.message("Đã thêm sản phẩm vào giỏ hàng thành công");
                host.navigate("cart-v2");
            } else host.message("Không thể đặt lại đơn hàng này.");
        });
        if (isPending(order))
            iconAction(actions, "Hủy yêu cầu tại máy", R.drawable.order_icon_close, false, () -> confirmCancellation(order));
    }

    protected void receipt() {
        headerActions("Đã lưu yêu cầu", "Trang chủ", "home");
        DemoState.Order order = selectedCustomerOrder(state);
        if (order == null) { missingOrder(); return; }
        LinearLayout receipt = card(true);
        chip(receipt, status(order), statusIcon(order), isPending(order));
        line(receipt, isPending(order) ? "Yêu cầu đã được lưu" : "Trạng thái yêu cầu", 21, R.color.forest, true);
        line(receipt, order.reference, 24, R.color.emerald, true);
        iconLine(receipt, R.drawable.order_icon_clock, order.displayTime, R.color.muted);
        productStrip(receipt, order);
        priceSummary(receipt, order.pricedSubtotalVnd, order.containsUnpriced);
        if (order.containsRx)
            iconLine(receipt, R.drawable.order_icon_receipt, "Toa chưa gửi hoặc được thẩm định.", R.color.warning);
        disclosure();
        LinearLayout contact = card();
        iconLine(contact, R.drawable.order_icon_person, order.customerName + " · " + order.contactPhone, R.color.ink);
        iconLine(contact, R.drawable.order_icon_pin, order.pickup, R.color.ink);
        iconAction(contact, "Xem chi tiết", R.drawable.order_icon_receipt, true, () -> host.navigate("order-detail-v2"));
        iconAction(contact, "Tiếp tục khám phá", R.drawable.order_icon_arrow_right, false, () -> host.navigate("home"));
    }

    private void productStrip(LinearLayout parent, DemoState.Order order) {
        LinearLayout strip = new LinearLayout(activity);
        strip.setOrientation(LinearLayout.HORIZONTAL);
        strip.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int shown = 0;
        for (String id : order.lines.keySet()) {
            if (shown == 3) break;
            android.widget.FrameLayout image = photo(state.product(id), 52);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(52), dp(52));
            imageParams.setMarginEnd(dp(8));
            strip.addView(image, imageParams);
            shown++;
        }
        if (order.lines.size() > shown) {
            TextView more = new TextView(activity);
            more.setText("+" + (order.lines.size() - shown));
            more.setTextSize(15);
            more.setTypeface(null, Typeface.BOLD);
            more.setTextColor(activity.getColor(R.color.emerald));
            more.setGravity(android.view.Gravity.CENTER);
            more.setBackgroundResource(R.drawable.order_photo_panel);
            strip.addView(more, new LinearLayout.LayoutParams(dp(52), dp(52)));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(8);
        params.bottomMargin = dp(14);
        parent.addView(strip, params);
    }

    private void timeline(LinearLayout parent, int icon, String title, String detail, boolean active) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        android.widget.ImageView marker = new android.widget.ImageView(activity);
        marker.setImageResource(icon);
        marker.setColorFilter(activity.getColor(active ? R.color.emerald : R.color.muted));
        marker.setPadding(dp(7), dp(7), dp(7), dp(7));
        marker.setBackgroundResource(R.drawable.order_mint_panel);
        marker.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        iconParams.setMarginEnd(dp(12));
        row.addView(marker, iconParams);
        LinearLayout copy = new LinearLayout(activity);
        copy.setOrientation(LinearLayout.VERTICAL);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        line(copy, title, 14, active ? R.color.emerald : R.color.muted, active);
        if (detail != null) line(copy, detail, 13, R.color.muted, false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(8);
        parent.addView(row, params);
    }

    private static boolean cancelled(DemoState.Order order) {
        return order.status != null && order.status.toLowerCase(Locale.ROOT).contains("hủy");
    }

    private static int statusIcon(DemoState.Order order) {
        return cancelled(order) ? R.drawable.order_icon_close
            : isPending(order) ? R.drawable.order_icon_clock : R.drawable.order_icon_check;
    }
    protected void confirmCancellation(DemoState.Order order) {
        if (!isPending(order) || selectedCustomerOrder(state) == null
                || !selectedCustomerOrder(state).reference.equals(order.reference)) {
            host.message("Yêu cầu không còn đủ điều kiện hủy tại máy");
            return;
        }
        LinearLayout wrapper = new LinearLayout(activity);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(20), dp(8), dp(20), 0);
        line(wrapper, "Hủy bản lưu tại máy; chưa gửi lệnh hủy đến nhà thuốc.",
            13, R.color.muted, false);
        EditText reason = input(wrapper, "Lý do hủy (3–160 ký tự)", InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, "");
        reason.setSingleLine(false);
        reason.setMinLines(2);
        reason.setFilters(new InputFilter[]{new InputFilter.LengthFilter(160)});
        AlertDialog dialog = new AlertDialog.Builder(activity)
            .setTitle("Hủy yêu cầu này?")
            .setView(wrapper)
            .setNegativeButton("Giữ lại", (d, which) -> {})
            .setPositiveButton("Xác nhận hủy", null)
            .create();
        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String note = reason.getText().toString();
            if (!canCancel(state, order, note)) {
                reason.setError("Nhập lý do từ 3 đến 160 ký tự");
                return;
            }
            if (state.cancelRequest(order.reference, note.trim())) {
                dialog.dismiss();
                host.message("Hủy yêu cầu trong dữ liệu thiết bị");
                host.navigate("order-detail-v2");
            } else {
                host.message("Không thể hủy; kiểm tra phiên và trạng thái yêu cầu.");
            }
        }));
        dialog.show();
    }
    protected void missingOrder() {
        LinearLayout card = card();
        illustration(card, R.drawable.order_empty_requests);
        heading(card, "Không tìm thấy yêu cầu");
        line(card, "Yêu cầu không thuộc tài khoản này hoặc dữ liệu đã thay đổi.",
            13, R.color.muted, false);
        iconAction(card, "Yêu cầu của tôi", R.drawable.order_icon_receipt, true, () -> host.navigate("orders-v2"));
    }
}
