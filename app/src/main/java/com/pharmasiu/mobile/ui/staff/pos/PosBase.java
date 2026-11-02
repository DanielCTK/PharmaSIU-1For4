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

/** Common screen references, data contracts, and formatting. */
abstract class PosBase {

    protected final Activity activity;
    protected final UiHost host;
    protected final DemoState state;
    protected final View root;
    protected final ScrollView scroll;
    protected final TextView shiftStatus, shiftBalance, searchSummary, totalLabel, changeLabel;
    protected final TextView historySummary;
    protected final TextView paymentError, denied;
    protected final LinearLayout openingGroup, activeGroup, salesPanel, cashGroup;
    protected final LinearLayout productRows, cartRows, receiptRows, historyRows;
    protected final View receiptPanel;
    protected final EditText openingCash, search, tender;
    protected final Button openShift, closeShift, clearCart, saveSale, receiptDismiss;
    protected final Button cartJump, historyMore, receiptHistory;
    protected final RadioGroup paymentGroup;
    protected final RadioButton cash, other;
    protected final View scrollTop;
    protected DemoState.Sale displayedReceipt;
    protected int visibleHistoryCount = 12;
    protected int productPage = 0;
    protected static final int PRODUCT_PAGE_SIZE = 8;

    PosBase(Activity activity, UiHost host, View root) {
        this.activity = activity;
        this.host = host;
        this.state = host.state();
        this.root = root;
        scroll = root.findViewById(R.id.staff_pos_scroll);
        shiftStatus = root.findViewById(R.id.staff_pos_shift_status);
        shiftBalance = root.findViewById(R.id.staff_pos_shift_balance);
        searchSummary = root.findViewById(R.id.staff_pos_search_summary);
        historySummary = root.findViewById(R.id.staff_pos_history_summary);
        totalLabel = root.findViewById(R.id.staff_pos_total);
        changeLabel = root.findViewById(R.id.staff_pos_change);
        paymentError = root.findViewById(R.id.staff_pos_payment_error);
        denied = root.findViewById(R.id.staff_pos_denied);
        openingGroup = root.findViewById(R.id.staff_pos_open_group);
        activeGroup = root.findViewById(R.id.staff_pos_active_group);
        salesPanel = root.findViewById(R.id.staff_pos_sales_panel);
        cashGroup = root.findViewById(R.id.staff_pos_cash_group);
        productRows = root.findViewById(R.id.staff_pos_products);
        cartRows = root.findViewById(R.id.staff_pos_cart_rows);
        receiptPanel = root.findViewById(R.id.staff_pos_receipt_panel);
        receiptRows = root.findViewById(R.id.staff_pos_receipt_lines);
        historyRows = root.findViewById(R.id.staff_pos_history);
        openingCash = root.findViewById(R.id.staff_pos_opening_cash);
        search = root.findViewById(R.id.staff_pos_search);
        tender = root.findViewById(R.id.staff_pos_cash_tender);
        openShift = root.findViewById(R.id.staff_pos_open_shift);
        closeShift = root.findViewById(R.id.staff_pos_close_shift);
        clearCart = root.findViewById(R.id.staff_pos_clear_cart);
        saveSale = root.findViewById(R.id.staff_pos_save_sale);
        receiptDismiss = root.findViewById(R.id.staff_pos_receipt_dismiss);
        receiptHistory = root.findViewById(R.id.staff_pos_receipt_history);
        cartJump = root.findViewById(R.id.staff_pos_cart_jump);
        historyMore = root.findViewById(R.id.staff_pos_history_more);
        paymentGroup = root.findViewById(R.id.staff_pos_payment_group);
        cash = root.findViewById(R.id.staff_pos_cash);
        other = root.findViewById(R.id.staff_pos_other);
        scrollTop = root.findViewById(R.id.staff_pos_scroll_top);
    }

    protected abstract boolean authorized();
    protected abstract void renderAll();
    protected abstract void renderProducts();
    protected abstract void renderCart();
    protected abstract void renderPayment();
    protected abstract void renderReceipt();
    protected abstract void renderHistory();
    protected TextView simpleText(String value, int sp, int color) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(activity.getColor(color));
        int padding = Math.round(8 * activity.getResources().getDisplayMetrics().density);
        text.setPadding(0, padding, 0, padding);
        return text;
    }

    protected void clearError() {
        paymentError.setText("");
        paymentError.setVisibility(View.GONE);
    }

    protected void showError(String message) {
        paymentError.setText(message);
        paymentError.setVisibility(View.VISIBLE);
        host.message(message);
    }

    protected static Long money(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            long result = Long.parseLong(value.trim());
            return result < 0 || result > 999_999_999_999L ? null : result;
        } catch (NumberFormatException ignored) { return null; }
    }

    protected static TextWatcher watch(Runnable callback) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                callback.run();
            }
            @Override public void afterTextChanged(Editable editable) { }
        };
    }

    protected static String vnd(long value) {
        return String.format(new Locale("vi", "VN"), "%,d ₫", value);
    }

    protected static String fold(String value) {
        String normalized = java.text.Normalizer.normalize(value.toLowerCase(Locale.ROOT),
            java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").replace('đ', 'd');
    }
}
