package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import java.time.LocalDate;
import java.util.Locale;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;

/** Readable overview of the actual local state and shortcuts to the existing tools. */
public final class AdminDashboard {
    private AdminDashboard() {}

    public static View dashboard(Activity activity, UiHost host) {
        DemoState state = host.state();
        Screen screen = new Screen(activity, R.layout.staff_admin_dashboard, host);
        screen.context(state.shiftOpen ? "Ca #" + state.shiftNumber + " đang mở" : "Chưa mở ca bán hàng");
        int lowStock = 0, blockedLots = 0, nearExpiry = 0;
        LocalDate today = LocalDate.now();
        for (DemoState.Product product : state.products) {
            if (state.stock.getOrDefault(product.id, 0) <= 4) lowStock++;
        }
        for (DemoState.Lot lot : state.lots) {
            if (lot.quarantined || lot.expiresOn == null || !lot.expiresOn.isAfter(today)) {
                blockedLots++;
            } else if (lot.quantity > 0 && !lot.expiresOn.isAfter(today.plusDays(30))) {
                nearExpiry++;
            }
        }
        long revenue = 0;
        for (DemoState.Sale sale : state.sales) revenue += sale.amountVnd;
        final View[] attentionTarget = new View[1];
        LinearLayout first = row(screen.body, 0);
        metric(first, "Sản phẩm", String.valueOf(state.products.size()), R.drawable.nav_catalog,
            false, () -> host.navigate("catalog-admin-v2"), true);
        metric(first, "Hóa đơn demo", String.valueOf(state.sales.size()), R.drawable.nav_orders,
            false, () -> host.navigate("reports-v2"), false);
        LinearLayout second = row(screen.body, 10);
        metric(second, "Doanh số demo", compactMoney(revenue), R.drawable.nav_admin,
            false, () -> host.navigate("reports-v2"), true)
            .setContentDescription("Doanh số demo đã lưu trên thiết bị: " + vnd(revenue) + ". Mở báo cáo");
        metric(second, "Cần kiểm tra", String.valueOf(lowStock + blockedLots + nearExpiry), R.drawable.nav_lots,
            true, () -> {
                View target=attentionTarget[0];
                if(target!=null) target.requestRectangleOnScreen(
                    new android.graphics.Rect(0,0,target.getWidth(),target.getHeight()),false);
            }, false).setContentDescription((lowStock + blockedLots + nearExpiry)
                + " cảnh báo về hàng tồn và lô hàng. Xem mục Cần chú ý");

        heading(screen.body, "Thao tác nhanh");
        LinearLayout actions = row(screen.body, 0);
        shortcut(actions, "Hàng hóa", R.drawable.nav_catalog, () -> host.navigate("catalog-admin-v2"), true);
        shortcut(actions, "Nhập lô", R.drawable.nav_lots, () -> host.navigate("lots-v2"), false);
        LinearLayout actions2 = row(screen.body, 10);
        shortcut(actions2, "Bán tại quầy", R.drawable.nav_pos, () -> host.navigate("pos"), true);
        shortcut(actions2, "Kiểm kê", R.drawable.nav_audit, () -> host.navigate("audit-v2"), false);
        LinearLayout actions3 = row(screen.body, 10);
        shortcut(actions3, "Yêu cầu", R.drawable.nav_orders, () -> host.navigate("requests-admin-v2"), true);
        shortcut(actions3, "Báo cáo", R.drawable.nav_admin, () -> host.navigate("reports-v2"), false);

        attentionTarget[0]=heading(screen.body, "Cần chú ý");
        attention(screen.body, "Hàng tồn thấp", lowStock + " mặt hàng có tồn từ 4 trở xuống",
            () -> host.navigate("catalog-admin-v2"));
        attention(screen.body, "Lô gần hết hạn", nearExpiry + " lô còn hàng, hết hạn trong 30 ngày",
            () -> host.navigate("lots-v2"));
        attention(screen.body, "Lô cần kiểm tra", blockedLots + " lô cách ly, thiếu hạn hoặc đã hết hạn",
            () -> host.navigate("lots-v2"));
        LinearLayout operation = card(screen);
        title(operation, "Phiên làm việc");
        line(operation, state.shiftOpen ? "Ca bán hàng #" + state.shiftNumber + " đang mở"
            : "Chưa mở ca bán hàng", 16, R.color.emerald, true);
        line(operation, state.orders.size() + " yêu cầu khách đã lưu trên thiết bị", 14, R.color.muted, false);
        warning(operation, "Chưa đồng bộ máy chủ, xác minh thanh toán hoặc duyệt toa thật.");
        return screen.root;
    }

    private static TextView heading(LinearLayout parent, String title) {
        TextView text = new TextView(parent.getContext());
        text.setText(title);
        text.setTextSize(18);
        text.setTextColor(parent.getContext().getColor(R.color.forest));
        text.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(parent.getContext(), 20), 0, dp(parent.getContext(), 10));
        parent.addView(text, p);
        androidx.core.view.ViewCompat.setAccessibilityHeading(text,true);
        return text;
    }

    private static LinearLayout row(LinearLayout parent, int marginTop) {
        LinearLayout row = new LinearLayout(parent.getContext());
        boolean narrow = parent.getResources().getConfiguration().screenWidthDp < 340
            || parent.getResources().getConfiguration().fontScale >= 1.5f;
        row.setOrientation(narrow ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        row.setBaselineAligned(false);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(parent.getContext(), marginTop);
        parent.addView(row, p);
        return row;
    }

    private static LinearLayout cell(LinearLayout parent, boolean first, int background) {
        LinearLayout cell = new LinearLayout(parent.getContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(parent.getContext(), 12), dp(parent.getContext(), 12),
            dp(parent.getContext(), 12), dp(parent.getContext(), 12));
        cell.setBackgroundResource(background);
        boolean stacked = parent.getOrientation() == LinearLayout.VERTICAL;
        LinearLayout.LayoutParams p = stacked ? new LinearLayout.LayoutParams(-1, -2)
            : new LinearLayout.LayoutParams(0, -1, 1);
        if (first && stacked) p.bottomMargin = dp(parent.getContext(), 10);
        else if (first) p.setMarginEnd(dp(parent.getContext(), 10));
        parent.addView(cell, p);
        return cell;
    }

    private static View metric(LinearLayout parent, String label, String value, int icon,
                               boolean warning, Runnable action, boolean first) {
        int background = warning ? R.drawable.admin_modern_warning
            : icon == R.drawable.nav_orders ? R.drawable.admin_modern_lime
            : R.drawable.admin_modern_mint;
        LinearLayout cell = cell(parent, first, background);
        cell.setMinimumHeight(dp(parent.getContext(), 126));
        ImageView image = new ImageView(parent.getContext());
        image.setImageResource(icon);
        image.setColorFilter(parent.getContext().getColor(warning ? R.color.warning : R.color.emerald));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        cell.addView(image, new LinearLayout.LayoutParams(dp(parent.getContext(), 24), dp(parent.getContext(), 24)));
        line(cell, value, 26, warning ? R.color.warning : R.color.forest, true).setPadding(0, dp(parent.getContext(), 8), 0, dp(parent.getContext(), 2));
        line(cell, label, 14, R.color.ink, false).setPadding(0, 0, 0, 0);
        cell.setContentDescription(label + ": " + value + ". Xem chi tiết");
        pressable(cell, action);
        return cell;
    }

    private static void shortcut(LinearLayout parent, String label, int icon, Runnable action, boolean first) {
        LinearLayout item = cell(parent, first, R.drawable.admin_modern_surface);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setMinimumHeight(dp(parent.getContext(), 64));
        ImageView image = new ImageView(parent.getContext());
        image.setImageResource(icon);
        image.setColorFilter(parent.getContext().getColor(R.color.emerald));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(parent.getContext(), 24), dp(parent.getContext(), 24));
        iconParams.setMarginEnd(dp(parent.getContext(), 10));
        item.addView(image, iconParams);
        TextView title = new TextView(parent.getContext());
        title.setText(label);
        title.setTextSize(14);
        title.setTextColor(parent.getContext().getColor(R.color.forest));
        title.setTypeface(null, Typeface.BOLD);
        item.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        item.setContentDescription(label);
        pressable(item, action);
    }

    private static void attention(LinearLayout parent, String title, String detail, Runnable action) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(parent.getContext(), 76));
        row.setPadding(dp(parent.getContext(), 14), dp(parent.getContext(), 12), dp(parent.getContext(), 14), dp(parent.getContext(), 12));
        row.setBackgroundResource(R.drawable.admin_modern_surface);
        LinearLayout text = new LinearLayout(parent.getContext());
        text.setOrientation(LinearLayout.VERTICAL);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        line(text, title, 16, R.color.forest, true).setPadding(0, 0, 0, dp(parent.getContext(), 4));
        line(text, detail, 14, R.color.muted, false).setPadding(0, 0, 0, 0);
        ImageView arrow = new ImageView(parent.getContext());
        arrow.setImageResource(R.drawable.ic_arrow_forward);
        arrow.setColorFilter(parent.getContext().getColor(R.color.emerald));
        arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(parent.getContext(), 20), dp(parent.getContext(), 20)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(parent.getContext(), 10);
        parent.addView(row, p);
        row.setContentDescription(title + ". " + detail + ". Xem chi tiết");
        pressable(row, action);
    }

    private static void pressable(View view, Runnable action) {
        view.setClickable(true);
        view.setFocusable(true);
        android.util.TypedValue value = new android.util.TypedValue();
        if (view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true))
            view.setForeground(view.getContext().getDrawable(value.resourceId));
        view.setOnClickListener(v -> action.run());
        com.pharmasiu.mobile.ui.design.UiMotion.press(view);
    }

    private static String compactMoney(long value) {
        if (value >= 999_500_000L) return String.format(new Locale("vi", "VN"), "%.1f tỷ", value / 1_000_000_000d);
        if (value >= 999_500L) return String.format(new Locale("vi", "VN"), "%.1f tr", value / 1_000_000d);
        if (value >= 1_000L) return String.format(new Locale("vi", "VN"), "%.0f nghìn", value / 1_000d);
        return value + " ₫";
    }
}
