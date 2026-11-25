package com.pharmasiu.mobile.ui.customer.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
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

/** Cart, local Rx picker and checkout screens. */
abstract class OrderCartScreens extends OrderUiBase {
    OrderCartScreens(Activity activity,UiHost host,int layout){super(activity,host,layout);}
    protected void cart() {
        headerActions("Giỏ hàng", "Tiếp tục mua sắm", "catalog");
        if (state.cart.isEmpty()) {
            LinearLayout empty = card();
            illustration(empty, R.drawable.order_empty_cart);
            line(empty, "Giỏ hàng đang trống", 21, R.color.forest, true).setGravity(Gravity.CENTER);
            line(empty, "Chọn sản phẩm bạn cần, thêm vào giỏ.", 14, R.color.muted, false).setGravity(Gravity.CENTER);
            iconAction(empty, "Khám phá sản phẩm", R.drawable.order_icon_arrow_right, true,
                () -> host.navigate("catalog"));
            disclosure();
            return;
        }
        chip(body, units(state.cart) + " sản phẩm", R.drawable.order_icon_cart, false);
        if (state.hasRx(state.cart))
            iconLine(body, R.drawable.order_icon_receipt, "Thuốc kê đơn cần đính kèm toa ở bước tiếp theo.", R.color.warning);
        LinearLayout products = card();
        products.setPadding(dp(14), 0, dp(14), 0);
        for (Map.Entry<String, Integer> entry : new ArrayList<>(state.cart.entrySet())) {
            DemoState.Product product = state.product(entry.getKey());
            if (product == null) continue;
            if (products.getChildCount() > 0) {
                View separator = new View(activity);
                separator.setBackgroundColor(activity.getColor(R.color.line));
                products.addView(separator, new LinearLayout.LayoutParams(-1, dp(1)));
            }
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(14), 0, dp(14));
            products.addView(row, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout top = new LinearLayout(activity);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            android.widget.FrameLayout picture = photo(product, 72);
            LinearLayout.LayoutParams pictureParams = new LinearLayout.LayoutParams(dp(72), dp(72));
            pictureParams.setMarginEnd(dp(12));
            top.addView(picture, pictureParams);
            LinearLayout info = new LinearLayout(activity);
            info.setOrientation(LinearLayout.VERTICAL);
            top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
            line(info, product.name, 16, R.color.ink, true);
            line(info, product.priceVnd > 0 ? money(product.priceVnd) + " / " + product.unit
                : "Chờ báo giá · " + product.unit, 15, R.color.emerald, true);
            top.setContentDescription("Xem sản phẩm " + product.name);
            top.setClickable(true);
            top.setFocusable(true);
            top.setOnClickListener(v -> {
                state.selectedProductId = product.id;
                host.navigate("product");
            });
            UiMotion.press(top);
            row.addView(top, new LinearLayout.LayoutParams(-1, -2));
            LinearLayout controls = new LinearLayout(activity);
            controls.setGravity(Gravity.CENTER_VERTICAL);
            controls.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams controlParams = new LinearLayout.LayoutParams(-1, -2);
            controlParams.topMargin = dp(8);
            row.addView(controls, controlParams);
            LinearLayout amount = new LinearLayout(activity);
            amount.setOrientation(LinearLayout.VERTICAL);
            controls.addView(amount, new LinearLayout.LayoutParams(0, -2, 1));
            line(amount, "Thành tiền", 13, R.color.muted, false).setPadding(0, 0, dp(8), dp(3));
            line(amount, product.priceVnd > 0 ? money(product.priceVnd * entry.getValue()) : "Chờ báo giá",
                18, R.color.emerald, true).setPadding(0, 0, dp(8), 0);
            Button decrease = iconAction(controls, "", R.drawable.order_icon_minus, false, () -> {
                state.changeCartQty(product.id, state.cart.getOrDefault(product.id, 0) - 1);
                host.navigate("cart-v2");
            });
            quantityControl(decrease, "Giảm số lượng " + product.name
                + (entry.getValue() == 1 ? "; bỏ khỏi giỏ" : ""));
            TextView quantity = new TextView(activity);
            quantity.setText(String.valueOf(entry.getValue()));
            quantity.setTextSize(16);
            quantity.setTextColor(activity.getColor(R.color.ink));
            quantity.setGravity(Gravity.CENTER);
            quantity.setTypeface(null, Typeface.BOLD);
            quantity.setContentDescription("Số lượng " + product.name + ": " + entry.getValue());
            quantity.setMinHeight(dp(48));
            quantity.setPadding(0, dp(4), 0, dp(4));
            controls.addView(quantity, new LinearLayout.LayoutParams(dp(40), -2));
            Button increase = iconAction(controls, "", R.drawable.order_icon_plus, false, () -> {
                if (state.cart.getOrDefault(product.id, 0) >= MAX_QTY)
                    host.message("Mỗi sản phẩm tối đa 20 đơn vị");
                else {
                    state.changeCartQty(product.id, state.cart.getOrDefault(product.id, 0) + 1);
                    host.navigate("cart-v2");
                }
            });
            quantityControl(increase, "Tăng số lượng " + product.name);
            increase.setEnabled(entry.getValue() < MAX_QTY);
        }
        LinearLayout pricing = root.findViewById(R.id.order_v2_footer);
        if (pricing == null) pricing = card(true);
        pricing.setVisibility(View.VISIBLE);
        boolean horizontal = activity.getResources().getConfiguration().screenWidthDp >= 360
            && activity.getResources().getConfiguration().fontScale < 1.3f;
        LinearLayout footerRow = new LinearLayout(activity);
        footerRow.setOrientation(horizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        footerRow.setGravity(Gravity.CENTER_VERTICAL);
        pricing.addView(footerRow, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout total = new LinearLayout(activity);
        total.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams totalParams = horizontal ? new LinearLayout.LayoutParams(0, -2, 1f)
            : new LinearLayout.LayoutParams(-1, -2);
        if (horizontal) totalParams.setMarginEnd(dp(14));
        footerRow.addView(total, totalParams);
        line(total, "Tạm tính · Tham khảo", 13, R.color.muted, false).setPadding(0, 0, 0, dp(3));
        line(total, money(state.knownSubtotal(state.cart)), 23, R.color.emerald, true).setPadding(0, 0, 0, dp(3));
        Button next = iconAction(footerRow, "Tiếp tục", R.drawable.order_icon_arrow_right, true, () -> {
            if (state.cart.isEmpty()) { host.message("Giỏ hàng trống"); return; }
            host.navigate(canAdvance(state) ? "checkout-v2" : "rx-v2");
        });
        LinearLayout.LayoutParams nextParams = horizontal ? new LinearLayout.LayoutParams(0, -2, 1.1f)
            : new LinearLayout.LayoutParams(-1, -2);
        nextParams.topMargin = horizontal ? 0 : dp(8);
        next.setLayoutParams(nextParams);
        next.setContentDescription("Tiếp tục kiểm tra yêu cầu demo");
        if (state.hasUnpriced(state.cart))
            iconLine(pricing, R.drawable.order_icon_info, "Chưa gồm mặt hàng chờ báo giá", R.color.warning);
        iconLine(pricing, R.drawable.order_icon_info, "Demo · Chưa gửi, giữ hàng hay thanh toán", R.color.muted);
    }

    private void quantityControl(Button button, String description) {
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(dp(48));
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(description);
        if (button instanceof com.google.android.material.button.MaterialButton) {
            com.google.android.material.button.MaterialButton material = (com.google.android.material.button.MaterialButton) button;
            material.setIconSize(dp(20));
            material.setIconPadding(0);
            material.setIconGravity(com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START);
        }
    }
    protected void rx() {
        headerActions("Đính kèm đơn thuốc", "Quay lại giỏ", "cart-v2");
        LinearLayout preview = card();
        heading(preview, "Toa cho thuốc kê đơn");
        if (state.cart.isEmpty() || !state.hasRx(state.cart)) {
            line(preview, "Giỏ hiện không có thuốc Rx cần chọn tệp. Trở lại giỏ để tiếp tục.", 14, R.color.muted, false);
            action(preview, "Quay lại giỏ", true, () -> host.navigate("cart-v2"));
            return;
        }
        line(preview, state.localPrescriptionSelected()
            ? "Đã chọn tệp trên máy: " + safeName(state.chosenPrescriptionName)
            : "Chưa chọn ảnh hoặc PDF toa cho giỏ hiện tại.", 14, R.color.ink, true);
        action(preview, state.localPrescriptionSelected() ? "Chọn tệp khác" : "Chọn ảnh / PDF toa",
            true, host::choosePrescriptionFile);
        if (state.localPrescriptionSelected()) {
            action(preview, "Bỏ tệp đã chọn", false, () -> {
                state.removeLocalPrescription();
                host.navigate("rx-v2");
            });
        }
        warning(preview, "Tệp chỉ được chọn tại máy, chưa gửi đến nhà thuốc hoặc được dược sĩ thẩm định.");

        LinearLayout steps = card();
        heading(steps, "Quy trình xử lý đơn thuốc");
        line(steps, "01 / Chọn ảnh hoặc PDF trên thiết bị", 14, R.color.muted, false);
        divider(steps);
        line(steps, "02 / Kiểm tra thông tin yêu cầu demo", 14, R.color.muted, false);
        divider(steps);
        line(steps, "03 / Lưu yêu cầu cục bộ, chờ tích hợp nhà thuốc", 14, R.color.muted, false);
        Button continueButton = action(steps, "Tiếp tục đặt hàng", true, () -> {
            if (canAdvance(state)) host.navigate("checkout-v2");
            else host.message("Chưa chọn tệp toa phù hợp với giỏ Rx hiện tại");
        });
        continueButton.setEnabled(canAdvance(state));
        disclosure();
    }

    protected void checkout() {
        headerActions("Thông tin nhận hàng", "Quay lại giỏ", "cart-v2");
        if (!canAdvance(state)) {
            LinearLayout guarded = card();
            heading(guarded, "Chưa đủ điều kiện hoàn tất đặt hàng");
            warning(guarded, state.cart.isEmpty() ? "Giỏ trống." :
                "Giỏ có Rx nhưng chưa chọn tệp toa hợp lệ cho các mặt hàng hiện tại.");
            action(guarded, "Quay về bước cần hoàn tất", true,
                () -> host.navigate(state.cart.isEmpty() ? "cart-v2" : "rx-v2"));
            return;
        }
        LinearLayout form = card();
        heading(form, "01 / Người liên hệ");
        DemoState.CustomerProfile profile = state.currentCustomer();
        EditText name = input(form, "Họ và tên người nhận", InputType.TYPE_CLASS_TEXT |
            InputType.TYPE_TEXT_FLAG_CAP_WORDS, profile == null ? "" : profile.displayName);
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        EditText phone = input(form, "Điện thoại bắt đầu bằng 0",
            InputType.TYPE_CLASS_PHONE, state.customerPhone);
        phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        line(form, "Số điện thoại gồm 10–11 chữ số, bắt đầu bằng 0.", 12, R.color.muted, false);

        LinearLayout pickupCard = card();
        heading(pickupCard, "02 / Địa chỉ nhận hàng");
        LinearLayout pickupFrame = new LinearLayout(activity);
        pickupFrame.setOrientation(LinearLayout.VERTICAL);
        pickupFrame.setBackground(activity.getDrawable(R.drawable.component_input_outline));
        pickupFrame.setPadding(dp(4), dp(2), dp(4), dp(2));
        Spinner pickup = new Spinner(activity);
        pickup.setMinimumHeight(dp(52));
        pickup.setAdapter(com.pharmasiu.mobile.ui.design.PharmaUi.createSpinnerAdapter(activity, new String[]{PICKUP_A, PICKUP_B}));
        pickupFrame.addView(pickup, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(-1, -2);
        pLp.topMargin = dp(6);
        pLp.bottomMargin = dp(8);
        pickupCard.addView(pickupFrame, pLp);
        line(pickupCard, "Chọn điểm nhận cho yêu cầu demo; chưa có xác nhận từ nhà thuốc.", 14, R.color.muted, false);

        LinearLayout review = card();
        heading(review, "03 / Kiểm tra mặt hàng");
        showLines(review, state.cart, null);
        priceSummary(review, state.knownSubtotal(state.cart), state.hasUnpriced(state.cart));
        if (state.hasRx(state.cart)) {
            line(review, state.localPrescriptionSelected()
                ? "Tệp toa tại máy: " + safeName(state.chosenPrescriptionName)
                : "Vui lòng đính kèm đơn thuốc cho thuốc kê đơn.", 13, R.color.forest, false);
            iconLine(review, R.drawable.order_icon_receipt, "Toa chưa gửi hoặc được thẩm định.", R.color.warning);
        }
        line(review, "Đơn hàng nhận tại nhà thuốc PharmaSIU.", 13, R.color.muted, false);
        AtomicBoolean inFlight = new AtomicBoolean();
        Button save = action(review, "Lưu yêu cầu demo", true, () -> {
            if (!canAdvance(state)) {
                host.message("Giỏ hoặc toa Rx không còn hợp lệ; hãy kiểm tra lại.");
                host.navigate(state.cart.isEmpty() ? "cart-v2" : "rx-v2");
                return;
            }
            String selected = pickup.getSelectedItem() == null ? null : pickup.getSelectedItem().toString();
            DemoState.Order created = submit(state, name.getText().toString(),
                phone.getText().toString(), selected, inFlight);
            if (created == null) {
                host.message("Kiểm tra họ tên, số điện thoại, điểm nhận A/B và toa Rx.");
            } else {
                host.message(state.isDatabaseBacked() ?
                "Đã lưu yêu cầu demo trên thiết bị." :
                "Yêu cầu kiểm thử chỉ lưu trong phiên");
                host.navigate("receipt-v2");
            }
        });
        save.setContentDescription("Lưu yêu cầu demo trên thiết bị, chưa xác nhận thanh toán hoặc giữ hàng");
        disclosure();
    }
}
