package com.pharmasiu.mobile.ui.customer.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.pharmasiu.mobile.ui.design.UiMotion;
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
        TextView titleView = root.findViewById(R.id.order_v2_title);
        View back = root.findViewById(R.id.order_v2_back);
        if (titleView != null && back != null) {
            titleView.setText(title);
            back.setContentDescription(label);
            back.setOnClickListener(v -> host.navigate(route));
            UiMotion.press(back);
            return;
        }
        action(body, "‹  " + label, false, () -> host.navigate(route));
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
                 "Đã lưu trên thiết bị":order.status);
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
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            FrameLayout image = photo(item, 56);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(56), dp(56));
            imageParams.setMarginEnd(dp(12));
            row.addView(image, imageParams);
            LinearLayout copy = new LinearLayout(activity);
            copy.setOrientation(LinearLayout.VERTICAL);
            row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
            line(copy, entry.getValue() + " × " + name, 15, R.color.ink, true);
            line(copy, amount <= 0 ? "Chờ báo giá" : money(amount) + " / đơn vị", 13, R.color.muted, false);
            if (item != null && item.requiresPrescription) chip(copy, "Cần toa thuốc", R.drawable.order_icon_receipt, true);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.bottomMargin = dp(12);
            target.addView(row, rowParams);
        }
    }

    protected void priceSummary(LinearLayout card, long subtotal, boolean unpriced) {
        line(card, "Tạm tính · Giá tham khảo", 13, R.color.muted, false);
        line(card, money(subtotal), 23, R.color.emerald, true);
        line(card, unpriced ? "Chưa gồm mặt hàng chờ báo giá." :
            "Chưa xác nhận tồn kho hoặc thanh toán.", 13,
            unpriced ? R.color.forest : R.color.muted, false);
    }

    protected void disclosure() {
        iconLine(body, R.drawable.order_icon_info, "Demo cục bộ · Chưa gửi nhà thuốc", R.color.muted);
    }

    protected FrameLayout photo(DemoState.Product product, int size) {
        FrameLayout frame = new FrameLayout(activity);
        frame.setBackgroundResource(R.drawable.order_photo_panel);
        frame.setClipToOutline(true);
        frame.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        if (product != null) {
            frame.addView(com.pharmasiu.mobile.ui.customer.catalog.ProductImageLoader.createThumbnail(activity, product),
                new FrameLayout.LayoutParams(-1, -1));
        } else {
            ImageView fallback = new ImageView(activity);
            fallback.setImageResource(R.drawable.order_icon_package);
            fallback.setPadding(dp(12), dp(12), dp(12), dp(12));
            frame.addView(fallback, new FrameLayout.LayoutParams(-1, -1));
        }
        frame.setMinimumWidth(dp(size));
        frame.setMinimumHeight(dp(size));
        return frame;
    }

    protected void illustration(LinearLayout parent, int drawable) {
        ImageView image = new ImageView(activity);
        image.setImageResource(drawable);
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(180), dp(144));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = dp(8);
        params.bottomMargin = dp(16);
        parent.addView(image, params);
    }

    protected TextView iconLine(LinearLayout parent, int icon, String value, int color) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView image = new ImageView(activity);
        image.setImageResource(icon);
        image.setColorFilter(activity.getColor(color));
        image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.setMarginEnd(dp(8));
        row.addView(image, iconParams);
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(13);
        text.setTextColor(activity.getColor(color));
        text.setLineSpacing(dp(2), 1f);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(4);
        params.bottomMargin = dp(6);
        parent.addView(row, params);
        return text;
    }

    protected TextView chip(LinearLayout parent, String value, int icon, boolean caution) {
        TextView chip = new TextView(activity);
        chip.setText(value);
        chip.setTextSize(13);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setTextColor(activity.getColor(caution ? R.color.warning : R.color.forest));
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(activity.getColor(caution ? R.color.warning_surface : R.color.mint));
        background.setCornerRadius(dp(8));
        chip.setBackground(background);
        chip.setPadding(dp(10), dp(6), dp(10), dp(6));
        android.graphics.drawable.Drawable mark = activity.getDrawable(icon);
        if (mark != null) {
            mark = mark.mutate();
            mark.setTint(activity.getColor(caution ? R.color.warning : R.color.emerald));
            mark.setBounds(0, 0, dp(16), dp(16));
            chip.setCompoundDrawablesRelative(mark, null, null, null);
            chip.setCompoundDrawablePadding(dp(6));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.bottomMargin = dp(8);
        parent.addView(chip, params);
        return chip;
    }

    protected Button iconAction(LinearLayout parent, String text, int icon, boolean primary, Runnable command) {
        Button button = action(parent, text, primary, command);
        if (button instanceof com.google.android.material.button.MaterialButton) {
            com.google.android.material.button.MaterialButton material = (com.google.android.material.button.MaterialButton) button;
            material.setIconResource(icon);
            material.setIconTint(ColorStateList.valueOf(activity.getColor(primary ? R.color.white : R.color.forest)));
            material.setIconPadding(dp(8));
        }
        return button;
    }

    protected Button linkAction(LinearLayout parent, String text, int icon, Runnable command) {
        Button button = iconAction(parent, text, icon, false, command);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.END;
        params.topMargin = dp(6);
        button.setLayoutParams(params);
        button.setMinHeight(dp(48));
        button.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        if (button instanceof com.google.android.material.button.MaterialButton)
            ((com.google.android.material.button.MaterialButton) button).setStrokeWidth(0);
        return button;
    }

    protected int dp(int value) {
        return Math.round(activity.getResources().getDisplayMetrics().density * value);
    }

    protected static String money(long value) {
        return String.format(new Locale("vi", "VN"), "%,d ₫", value);
    }

    protected LinearLayout card() {
        return card(false);
    }

    protected LinearLayout card(boolean highlight) {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        content.setBackgroundResource(highlight ? R.drawable.order_summary_gradient : R.drawable.order_surface_panel);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(12);
        body.addView(content, params);
        return content;
    }

    protected void heading(LinearLayout target,String title) {
        TextView heading = line(target,title,18,R.color.forest,true);
        androidx.core.view.ViewCompat.setAccessibilityHeading(heading,true);
    }

    protected TextView line(LinearLayout parent, String text, int sp, int color, boolean strong) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(Math.max(13,sp));
        view.setTextColor(activity.getColor(color));
        view.setPadding(0, dp(2), 0, dp(3));
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
        Button button = PharmaUi.button(parent,text,primary,command);
        UiMotion.press(button);
        return button;
    }

    protected EditText input(LinearLayout parent,String hint,int type,String value){
        return PharmaUi.input(parent,hint,type,value);
    }
}
