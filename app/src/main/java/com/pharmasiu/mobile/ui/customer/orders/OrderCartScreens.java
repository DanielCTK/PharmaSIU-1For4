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

/** Cart, local Rx picker and checkout screens. */
abstract class OrderCartScreens extends OrderUiBase {
    OrderCartScreens(Activity activity,UiHost host,int layout){super(activity,host,layout);}
    protected void cart() {
        headerActions("Chọn hàng · kiểm tra lại", "Danh mục", "catalog");
        if (state.cart.isEmpty()) {
            LinearLayout empty = card();
            heading(empty, "Giỏ của bạn đang trống");
            line(empty, "Chọn sản phẩm để trải nghiệm lưu yêu cầu Click & Collect tại máy.", 14, R.color.muted, false);
            action(empty, "Khám phá sản phẩm", true, () -> host.navigate("catalog"));
            return;
        }
        int units = 0;
        for (int n : state.cart.values()) units += n;
        LinearLayout summary = card();
        heading(summary, "Giỏ hàng của bạn");
        line(summary, units + " sản phẩm · số lượng mỗi mặt hàng tối đa " + MAX_QTY, 12, R.color.muted, false);
        for (Map.Entry<String, Integer> line : new ArrayList<>(state.cart.entrySet())) {
            DemoState.Product product = state.product(line.getKey());
            if (product == null) continue;
            LinearLayout row = card();
            line(row, product.category + " · " + (product.requiresPrescription ? "THUỐC KÊ ĐƠN (CẦN TOA)" : "CHÍNH HÃNG"),
                11, product.requiresPrescription ? R.color.warning : R.color.emerald, true);
            heading(row, product.name);
            line(row, product.subtitle + " · " + product.unit, 12, R.color.muted, false);
            line(row, product.priceVnd > 0 ? money(product.priceVnd) + " / đơn vị · tham khảo"
                : "Giá khi nhà thuốc báo giá", 14, R.color.emerald, true);
            line(row, "Số lượng: " + line.getValue(), 14, R.color.ink, true);
            action(row, "− Giảm / bỏ sản phẩm", false, () -> {
                state.changeCartQty(product.id, state.cart.getOrDefault(product.id, 0) - 1);
                host.navigate("cart-v2");
            });
            Button increase = action(row, "+ Tăng số lượng", false, () -> {
                if (state.cart.getOrDefault(product.id, 0) >= MAX_QTY)
                    host.message("Mỗi sản phẩm tối đa 20 đơn vị");
                else {
                    state.changeCartQty(product.id, state.cart.getOrDefault(product.id, 0) + 1);
                    host.navigate("cart-v2");
                }
            });
            increase.setEnabled(line.getValue() < MAX_QTY);
            line(row, "Tồn kho tại quầy chưa được xác minh.", 12, R.color.muted, false);
        }
        LinearLayout pricing = card();
        heading(pricing, "CHI PHÍ THAM KHẢO");
        line(pricing, money(state.knownSubtotal(state.cart)), 24, R.color.emerald, true);
        if (state.hasUnpriced(state.cart))
            warning(pricing, "Có sản phẩm chờ báo giá. Tạm tính chưa phải tổng tiền phải trả.");
        else line(pricing, "Giá niêm yết đã bao gồm VAT. Hỗ trợ giao tận nơi hoặc nhận tại quầy.",
            13, R.color.muted, false);
        if (state.hasRx(state.cart)) {
            warning(pricing, "Giỏ có thuốc kê đơn. Chỉ chọn ảnh/PDF toa tại máy để thử bước tiếp theo; chưa tải lên hoặc được duyệt.");
        }
        action(pricing, "Tiếp tục đặt hàng", true, () -> {
            if (state.cart.isEmpty()) { host.message("Giỏ hàng trống"); return; }
            host.navigate(canAdvance(state) ? "checkout-v2" : "rx-v2");
        });
        disclosure();
    }

    protected void rx() {
        headerActions("Chọn tệp toa · tại thiết bị", "Quay lại giỏ", "cart-v2");
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
        warning(preview, "Chọn tệp chỉ lưu dấu trong phiên trên thiết bị. KHÔNG upload, xác thực, duyệt toa hoặc giữ thuốc. Khi thay đổi hàng Rx/số lượng, phải chọn lại tệp.");

        LinearLayout steps = card();
        heading(steps, "Quy trình khi tích hợp thật");
        line(steps, "01 / Gửi toa đến nhà thuốc  · CHỜ TÍCH HỢP", 13, R.color.muted, false);
        divider(steps);
        line(steps, "02 / Dược sĩ có thẩm quyền thẩm định  · CHỜ TÍCH HỢP", 13, R.color.muted, false);
        divider(steps);
        line(steps, "03 / Xác minh lô, tồn và lịch nhận  · CHỜ TÍCH HỢP", 13, R.color.muted, false);
        Button continueButton = action(steps, "Tiếp tục đặt hàng", true, () -> {
            if (canAdvance(state)) host.navigate("checkout-v2");
            else host.message("Chưa chọn tệp toa phù hợp với giỏ Rx hiện tại");
        });
        continueButton.setEnabled(canAdvance(state));
        disclosure();
    }

    protected void checkout() {
        headerActions("Thông tin liên hệ · điểm nhận", "Quay lại giỏ", "cart-v2");
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
        Spinner pickup = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
            android.R.layout.simple_spinner_item, new String[]{PICKUP_A, PICKUP_B});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pickup.setAdapter(adapter);
        pickupCard.addView(pickup, new LinearLayout.LayoutParams(-1, dp(54)));
        line(pickupCard, "Thời gian nhận sẽ được hẹn sau khi nhà thuốc có hệ thống xác nhận; chưa có địa điểm/địa chỉ thật.", 13, R.color.muted, false);

        LinearLayout review = card();
        heading(review, "03 / Kiểm tra mặt hàng");
        showLines(review, state.cart, null);
        priceSummary(review, state.knownSubtotal(state.cart), state.hasUnpriced(state.cart));
        if (state.hasRx(state.cart)) {
            warning(review, state.localPrescriptionSelected()
                ? "Đã chọn tệp toa trong máy. Chưa tải lên hoặc được dược sĩ duyệt."
                : "Chưa chọn toa phù hợp. Vui lòng đính kèm đơn thuốc.");
        }
        warning(review, "Lưu yêu cầu chỉ ghi vào RAM trên máy; KHÔNG gửi đến nhà thuốc, không giữ hàng, không thanh toán.");
        AtomicBoolean inFlight = new AtomicBoolean();
        Button save = action(review, "Xác nhận đặt hàng", true, () -> {
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
                "Đã lưu vào SQLite trên thiết bị; chưa gửi nhà thuốc" :
                "Yêu cầu kiểm thử chỉ lưu trong phiên");
                host.navigate("receipt-v2");
            }
        });
        save.setContentDescription("Xác nhận đặt hàng tại nhà thuốc PharmaSIU");
        disclosure();
    }
}
