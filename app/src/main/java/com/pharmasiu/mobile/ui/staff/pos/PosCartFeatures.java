package com.pharmasiu.mobile.ui.staff.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.domain.JavaRules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Cart validation, amounts, tender and save demo receipt. */
abstract class PosCartFeatures extends PosProductFeatures {
    PosCartFeatures(Activity activity, UiHost host, View root){super(activity,host,root);}
    protected void renderCart() {
        cartRows.removeAllViews();
        if (state.posCart.isEmpty()) cartRows.addView(simpleText("Giỏ đang trống. Chọn hàng thử phía trên.",
            14, R.color.muted));
        for (Map.Entry<String, Integer> entry : new ArrayList<>(state.posCart.entrySet())) {
            DemoState.Product product = state.product(entry.getKey());
            if (product == null) {
                cartRows.addView(simpleText("Mã sản phẩm không tìm thấy: " + entry.getKey(), 13,
                    R.color.warning));
                continue;
            }
            int qty = entry.getValue();
            View item = activity.getLayoutInflater().inflate(R.layout.staff_pos_cart_item, cartRows, false);
            ((TextView) item.findViewById(R.id.staff_pos_cart_title)).setText(product.name);
            ((TextView) item.findViewById(R.id.staff_pos_cart_subtitle)).setText(
                vnd(product.priceVnd) + " / " + product.unit + " · Tồn hợp lệ demo " + effectiveAvailable(product.id));
            ((TextView) item.findViewById(R.id.staff_pos_cart_quantity)).setText(String.valueOf(qty));
            ((TextView) item.findViewById(R.id.staff_pos_cart_line_total)).setText(vnd(product.priceVnd * qty));
            Button minus = item.findViewById(R.id.staff_pos_cart_decrease);
            Button plus = item.findViewById(R.id.staff_pos_cart_increase);
            minus.setContentDescription("Giảm một " + product.name);
            plus.setContentDescription("Tăng một " + product.name);
            minus.setOnClickListener(v -> {
                if (!authorized()) return;
                state.changePosQty(product.id, state.posCart.getOrDefault(product.id, 0) - 1);
                clearError();
                renderAll();
            });
            boolean canIncrease = authorized() && JavaRules.canSellDemo(product, qty + 1,
                effectiveAvailable(product.id), state.shiftOpen);
            plus.setEnabled(canIncrease);
            plus.setAlpha(canIncrease ? 1 : .45f);
            plus.setOnClickListener(v -> {
                if (!authorized() || !JavaRules.canSellDemo(product,
                    state.posCart.getOrDefault(product.id, 0) + 1,
                    effectiveAvailable(product.id), state.shiftOpen)) {
                    host.message("Không đủ tồn lô hợp lệ để tăng số lượng mẫu."); return;
                }
                state.changePosQty(product.id, state.posCart.getOrDefault(product.id, 0) + 1);
                clearError();
                renderAll();
            });
            cartRows.addView(item);
        }
        totalLabel.setText("Tạm tính mẫu: " + vnd(state.posTotal()));
        clearCart.setEnabled(!state.posCart.isEmpty());
        cartJump.setEnabled(!state.posCart.isEmpty());
        cartJump.setText("Xem giỏ và thanh toán demo (" +
            state.posCart.values().stream().mapToInt(Integer::intValue).sum() + ")");
        renderPayment();
    }

    protected void renderPayment() {
        if (cashGroup == null) return;
        boolean cashSelected = !other.isChecked();
        cashGroup.setVisibility(cashSelected ? View.VISIBLE : View.GONE);
        Long paid = money(tender.getText().toString());
        long subtotal = state.posTotal();
        if (cashSelected && paid == null) changeLabel.setText("Nhập số tiền giả định để tính tiền thối.");
        else if (cashSelected && paid < subtotal)
            changeLabel.setText("Tiền mẫu còn thiếu: " + vnd(subtotal - paid));
        else if (cashSelected) changeLabel.setText("Tiền thối thử: " + vnd(paid - subtotal));
        saveSale.setEnabled(authorized() && state.shiftOpen && !state.posCart.isEmpty() &&
            subtotal > 0 && (!cashSelected || (paid != null && paid >= subtotal)) &&
            cartSaleEligible());
        saveSale.setAlpha(saveSale.isEnabled() ? 1f : .55f);
    }

    protected void saveDemoSale() {
        clearError();
        if (!authorized() || !state.shiftOpen) {
            showError("Bạn không có quyền hoặc ca demo chưa mở."); return;
        }
        String method = other.isChecked() ? "OTHER_DEMO" : "CASH";
        Long tendered = "CASH".equals(method) ? money(tender.getText().toString()) : 0L;
        if (tendered == null || ("CASH".equals(method) && tendered < state.posTotal())) {
            showError("Tiền mặt giả định phải đủ tổng giá trị mẫu."); return;
        }
        if (!cartSaleEligible()) {
            showError("Giỏ có Rx, giá lỗi hoặc không đủ tồn lô hợp lệ. Cần điều chỉnh trước khi lưu.");
            renderAll();
            return;
        }
        DemoState.Sale result = state.savePosSale(method, tendered);
        if (result == null) {
            showError("Không lưu được phiếu. Kho hoặc trạng thái ca vừa thay đổi.");
            renderAll();
            return;
        }
        displayedReceipt = result;
        tender.setText("");
        renderAll();
        host.message("Đã lưu " + result.reference + " trong RAM. Chưa xác minh thu tiền hoặc giao hàng.");
        receiptPanel.post(() -> scroll.smoothScrollTo(0, receiptPanel.getTop()));
    }

    /** Repeat lot and role validation before save, since the existing store is shared. */
    protected boolean cartSaleEligible() {
        if (!authorized() || !state.shiftOpen || state.posCart.isEmpty()) return false;
        for (Map.Entry<String, Integer> entry : state.posCart.entrySet()) {
            DemoState.Product product = state.product(entry.getKey());
            if (!JavaRules.canSellDemo(product, entry.getValue(), effectiveAvailable(entry.getKey()), true))
                return false;
            if (hasLot(entry.getKey()) && JavaRules.allocateFefo(state.lots, entry.getKey(),
                entry.getValue(), LocalDate.now()) == null) return false;
        }
        return state.posTotal() > 0;
    }
}
