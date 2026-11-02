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

/** Shared view binding, formatting and branded Material 3 primitives. */
abstract class OrderUiBase {
    protected final Activity activity;
    protected final UiHost host;
    protected final DemoState state;
    protected final LinearLayout body;
    protected final View root;

    protected OrderUiBase(Activity activity, UiHost host, int layout) {
        this.activity = activity;
        this.host = host;
        this.state = host.state();
        root = activity.getLayoutInflater().inflate(layout, null, false);
        body = root.findViewById(R.id.order_v2_content);
        if (body == null) throw new IllegalStateException("Missing order_v2_content");
    }
    protected void headerActions(String title, String label, String route) {
        LinearLayout row = card();
        heading(row, title);
        action(row, "← " + label, false, () -> host.navigate(route));
    }

    protected static String safeName(String text) {
        return text == null || text.trim().isEmpty() ? "Tệp được chọn tại máy" : text;
    }

    protected static int units(Map<String, Integer> lines) {
        int total = 0;
        for (int q : lines.values()) if (q > 0) total += q;
        return total;
    }

    protected static String status(DemoState.Order order) {
        return order==null?"Không có trạng thái":
            ((order.status.equals("Đã lưu yêu cầu DEMO")||order.status.equals("Đã tiếp nhận yêu cầu"))?
                 "Đã tiếp nhận yêu cầu":order.status);
    }

    protected void showLines(LinearLayout target, Map<String, Integer> lines, DemoState.Order historicalOrder) {
        for (Map.Entry<String, Integer> entry : lines.entrySet()) {
            DemoState.Product item = state.product(entry.getKey());
            String name = historicalOrder == null
                ? (item == null ? "Mặt hàng không còn trong danh mục (" + entry.getKey() + ")" : item.name)
                : historicalOrder.productNamesSnapshot.getOrDefault(entry.getKey(),entry.getKey());
            long amount = historicalOrder == null
                ? (item == null ? 0L : item.priceVnd)
                : historicalOrder.unitPricesSnapshot.getOrDefault(entry.getKey(),0L);
            line(target, entry.getValue() + " × " + name, 14, R.color.ink, true);
            line(target, amount <= 0 ? "Chờ nhà thuốc báo giá" :
                money(amount) + " / đơn vị · " + (historicalOrder != null ? "giá chốt khi lưu yêu cầu" : "giá tham khảo"),
                12, R.color.muted, false);
            if (item != null && item.requiresPrescription)
                line(target, "Rx · Thuốc kê đơn", 12, R.color.emerald, true);
            divider(target);
        }
    }

    protected void priceSummary(LinearLayout card, long subtotal, boolean unpriced) {
        line(card, "Tạm tính mặt hàng đã có giá", 12, R.color.muted, false);
        line(card, money(subtotal), 21, R.color.emerald, true);
        line(card, unpriced ? "Chưa gồm các mặt hàng chờ báo giá; tổng tiền chưa xác định." :
            "Đơn hàng được phục vụ bởi dược sĩ chuyên môn PharmaSIU.", 12,
            unpriced ? R.color.forest : R.color.muted, false);
    }

    protected void disclosure() {
        // Warning card removed to keep screens clean and free of red/disclaimer boxes
    }

    protected int dp(int value) {
        return Math.round(activity.getResources().getDisplayMetrics().density * value);
    }

    protected static String money(long value) {
        return String.format(new Locale("vi", "VN"), "%,d ₫", value);
    }

    protected LinearLayout card() {
        return PharmaUi.surface(body, false);
    }

    protected void heading(LinearLayout target,String title) {
        PharmaUi.sectionTitle(target,title);
    }

    protected TextView line(LinearLayout parent, String text, int sp, int color, boolean strong) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(activity.getColor(color));
        view.setPadding(0, dp(4), 0, dp(5));
        view.setLineSpacing(dp(2), 1.0f);
        if (strong) view.setTypeface(null, Typeface.BOLD);
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    protected void warning(LinearLayout parent,String text) {
        PharmaUi.warning(parent,text);
    }

    protected void divider(LinearLayout parent) {
        View divider = new View(activity);
        divider.setBackgroundColor(activity.getColor(R.color.line));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(1));
        lp.setMargins(0, dp(10), 0, dp(10));
        parent.addView(divider, lp);
    }

    protected Button action(LinearLayout parent,String text,boolean primary,Runnable command){
        return PharmaUi.button(parent,text,primary,command);
    }

    protected EditText input(LinearLayout parent,String hint,int type,String value){
        return PharmaUi.input(parent,hint,type,value);
    }
}
