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
        headerActions("Lịch sử đơn hàng", "Tiếp tục mua sắm", "catalog");
        List<DemoState.Order> mine = state.customerOrders();
        LinearLayout banner = card();
        heading(banner, mine.isEmpty() ? "Chưa có yêu cầu" : mine.size() + " đơn hàng");
        line(banner, "Lịch sử các đơn đặt hàng nhận tại quầy thuốc của bạn.", 13, R.color.muted, false);
        if (mine.isEmpty()) {
            action(banner, "Khám phá sản phẩm", true, () -> host.navigate("catalog"));
            return;
        }
        for (DemoState.Order order : mine) {
            LinearLayout card = card();
            line(card, "MÃ YÊU CẦU / " + order.reference, 16, R.color.emerald, true);
            line(card, status(order), 13, isPending(order) ? R.color.ink : R.color.muted, true);
            line(card, "Đặt lúc " + order.displayTime + " · " + units(order.lines) + " đơn vị",
                12, R.color.muted, false);
            line(card, "Địa chỉ nhận: " + order.pickup, 12, R.color.muted, false);
            priceSummary(card, order.pricedSubtotalVnd, order.containsUnpriced);
            action(card, "Xem chi tiết yêu cầu", false, () -> {
                state.selectedOrderId = order.reference;
                host.navigate("order-detail-v2");
            });
        }
        disclosure();
    }

    protected void detail() {
        headerActions("Chi tiết yêu cầu", "Lịch sử", "orders-v2");
        DemoState.Order order = selectedCustomerOrder(state);
        if (order == null) {
            missingOrder();
            return;
        }
        LinearLayout status = card();
        line(status, "ĐƠN HÀNG ĐÃ ĐƯỢC TIẾP NHẬN", 12, R.color.emerald, true);
        heading(status, order.reference);
        line(status, status(order), 15, isPending(order) ? R.color.emerald : R.color.forest, true);
        line(status, "Thời gian đặt: " + order.displayTime, 12, R.color.muted, false);
        if (!isPending(order) && order.cancelReason != null) {
            line(status, "Lý do hủy: " + order.cancelReason, 13, R.color.muted, false);
        } else {
        }
        LinearLayout goods = card();
        heading(goods, "Sản phẩm trong đơn hàng");
        showLines(goods, order.lines, order);
        priceSummary(goods, order.pricedSubtotalVnd, order.containsUnpriced);

        LinearLayout contact = card();
        heading(contact, "Thông tin nhận hàng");
        line(contact, "Người nhận: " + order.customerName, 14, R.color.ink, true);
        line(contact, "Điện thoại liên hệ: " + order.contactPhone, 14, R.color.ink, false);
        divider(contact);
        line(contact, "Địa chỉ nhận: " + order.pickup, 14, R.color.ink, true);
        line(contact, "Dược sĩ PharmaSIU sẽ chuẩn bị đơn hàng và liên hệ theo địa chỉ trên.",
            12, R.color.muted, false);

        boolean isApproved = "Sẵn sàng nhận".equals(order.status) || "Hoàn tất".equals(order.status);
        boolean isDone = "Hoàn tất".equals(order.status);

        LinearLayout progress = card();
        heading(progress, "Tiến trình đơn hàng");
        line(progress, "01 / Đã tiếp nhận yêu cầu", 14, R.color.emerald, true);
        divider(progress);
        line(progress, isApproved ? "02 / Dược sĩ đã thẩm định & chuẩn bị thuốc sẵn sàng" : "02 / Dược sĩ đang thẩm định và chuẩn bị thuốc",
            13, isApproved ? R.color.emerald : R.color.forest, isApproved);
        divider(progress);
        line(progress, isDone ? "03 / Đã nhận thuốc tại quầy thành công" : "03 / Sẵn sàng nhận tại " + order.pickup,
            13, isDone ? R.color.emerald : isApproved ? R.color.forest : R.color.muted, isApproved);
        action(progress, "🔄 Cập nhật trạng thái", false, () -> {
            state.customerOrders();
            host.navigate("order-detail-v2");
            host.message("Đã làm mới thông tin đơn hàng");
        });

        LinearLayout actions = card();
        heading(actions, "Thao tác đơn hàng");
        action(actions, "Mua lại sản phẩm này", true, () -> {
            if (state.reorderRequest(order.reference)) {
                host.message("Đã thêm sản phẩm vào giỏ hàng thành công");
                host.navigate("cart-v2");
            } else host.message("Không thể đặt lại đơn hàng này.");
        });
        if (isPending(order)) {
            action(actions, "Hủy đơn hàng này", false, () -> confirmCancellation(order));
        }
        action(actions, "← Quay lại danh sách đơn", false, () -> host.navigate("orders-v2"));
        disclosure();
    }

    protected void receipt() {
        headerActions("Xác nhận yêu cầu", "Trang chủ", "home");
        DemoState.Order order = selectedCustomerOrder(state);
        if (order == null) {
            missingOrder();
            return;
        }
        LinearLayout receipt = card();
        line(receipt, "ĐÃ GHI NHẬN ĐƠN HÀNG", 12, R.color.emerald, true);
        heading(receipt, isPending(order) ? "Đơn hàng đã được ghi nhận thành công" : "Trạng thái yêu cầu");
        line(receipt, order.reference, 24, R.color.emerald, true);
        line(receipt, "Đặt lúc " + order.displayTime, 12, R.color.muted, false);
        line(receipt, "Người nhận: " + order.customerName + " · " + order.contactPhone,
            13, R.color.ink, false);
        line(receipt, order.pickup + " · " + units(order.lines) + " đơn vị",
            13, R.color.ink, false);
        priceSummary(receipt, order.pricedSubtotalVnd, order.containsUnpriced);
                LinearLayout progress = card();
        heading(progress, "Tiến trình xử lý");
        line(progress, "01 / Đã tiếp nhận yêu cầu", 14, R.color.emerald, true);
        divider(progress);
        line(progress, "02 / Dược sĩ thẩm định và chuẩn bị thuốc", 13, R.color.forest, false);
        divider(progress);
        line(progress, "03 / Sẵn sàng nhận tại nhà thuốc hoặc giao tận nơi", 13, R.color.muted, false);
        action(progress, "Xem chi tiết yêu cầu", true, () -> host.navigate("order-detail-v2"));
        action(progress, "Tiếp tục khám phá", false, () -> host.navigate("home"));
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
        line(wrapper, "Chỉ hủy bản lưu trong ứng dụng, không gửi lệnh hủy đến nhà thuốc hoặc thay đổi tồn thật.",
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
        heading(card, "Không tìm thấy yêu cầu trong phiên");
        line(card, "Không tìm thấy yêu cầu trong tài khoản này hoặc dữ liệu thiết bị đã thay đổi.",
            13, R.color.muted, false);
        action(card, "Xem yêu cầu của tôi", true, () -> host.navigate("orders-v2"));
    }
}
