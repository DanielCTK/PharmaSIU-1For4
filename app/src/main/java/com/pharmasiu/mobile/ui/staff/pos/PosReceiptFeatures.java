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

/** Read-only historical receipt and paginated sales log. */
abstract class PosReceiptFeatures extends PosCartFeatures {
    PosReceiptFeatures(Activity activity, UiHost host, View root){super(activity,host,root);}
    protected void renderReceipt() {
        receiptPanel.setVisibility(authorized() && displayedReceipt != null ? View.VISIBLE : View.GONE);
        if (!authorized() || displayedReceipt == null) return;
        DemoState.Sale sale = displayedReceipt;
        ((TextView) root.findViewById(R.id.staff_pos_receipt_reference)).setText(sale.reference);
        ((TextView) root.findViewById(R.id.staff_pos_receipt_amount)).setText(
            "Tổng cộng: " + vnd(sale.amountVnd));
        receiptRows.removeAllViews();
        if (sale.immutableDetail != null && sale.immutableDetail.lines != null) {
            for (int i = 0; i < sale.immutableDetail.lines.size(); i++) {
                String name = sale.immutableDetail.lines.get(i).name;
                String id = sale.immutableDetail.lines.get(i).productId;
                String unit = sale.immutableDetail.lines.get(i).unit;
                long price = sale.immutableDetail.lines.get(i).unitPriceVnd;
                int qty = sale.immutableDetail.lines.get(i).quantity;
                appendReceiptLine(name + " × " + qty,
                    id + " · " + vnd(price) + "/" + unit + " · Thành tiền " + vnd(price * qty));
            }
        } else {
            for (Map.Entry<String, Integer> entry : sale.lines.entrySet()) {
                DemoState.Product product = state.product(entry.getKey());
                appendReceiptLine((product != null ? product.name : "Mã " + entry.getKey()) +
                    " × " + entry.getValue(),
                    "Dữ liệu phiếu cũ: chỉ có mã và số lượng · " + entry.getKey());
            }
        }
        String method = "CASH".equals(sale.method) ? "Tiền mặt" : "Chuyển khoản / Thẻ";
        String details = "Thời gian: " + sale.createdAt + "\nThu ngân: " + sale.cashierName +
            "\nCa bán hàng #" + sale.shiftNumber + "\nHình thức thanh toán: " + method;
        if ("CASH".equals(sale.method)) {
            details += "\nTiền giả định khách đưa: " + vnd(sale.receivedVnd) +
                "\nTiền thối tính thử: " + vnd(sale.changeVnd);
        }
        ((TextView) root.findViewById(R.id.staff_pos_receipt_details)).setText(details);
    }

    protected void appendReceiptLine(String title, String meta) {
        View row = activity.getLayoutInflater().inflate(R.layout.staff_pos_receipt_item,
            receiptRows, false);
        ((TextView) row.findViewById(R.id.staff_pos_receipt_line_title)).setText(title);
        ((TextView) row.findViewById(R.id.staff_pos_receipt_line_meta)).setText(meta);
        receiptRows.addView(row);
    }

    protected void renderHistory() {
        historyRows.removeAllViews();
        long revenue = 0L;
        for (DemoState.Sale sale : state.sales) revenue += sale.amountVnd;
        historySummary.setText(state.sales.size() + " hóa đơn đã lập · " +
            vnd(revenue) + " (mọi ca trong phiên)");
        if (state.sales.isEmpty()) {
            historyRows.addView(simpleText("Chưa có giao dịch trong ca.", 14, R.color.muted));
            historyMore.setVisibility(View.GONE);
            return;
        }
        for (int shown = 0; shown < Math.min(visibleHistoryCount, state.sales.size()); shown++) {
            DemoState.Sale sale = state.sales.get(shown);
            View row = activity.getLayoutInflater().inflate(R.layout.staff_pos_receipt_item,
                historyRows, false);
            ((TextView) row.findViewById(R.id.staff_pos_receipt_line_title)).setText(
                sale.reference + "  ·  " + vnd(sale.amountVnd));
            int count = 0;
            for (int qty : sale.lines.values()) count += qty;
            ((TextView) row.findViewById(R.id.staff_pos_receipt_line_meta)).setText(
                count + " sản phẩm · " + ("CASH".equals(sale.method) ? "Tiền mặt" : "Khác") +
                " · " + sale.createdAt + " · Chạm để xem");
            row.setContentDescription("Mở hóa đơn " + sale.reference);
            row.setFocusable(true);
            row.setClickable(true);
            row.setOnClickListener(v -> {
                displayedReceipt = sale;
                renderReceipt();
                scrollTo(receiptPanel);
            });
            historyRows.addView(row);
        }
        historyMore.setVisibility(visibleHistoryCount < state.sales.size() ? View.VISIBLE : View.GONE);
        historyMore.setText("Xem thêm " + Math.min(12, state.sales.size() - visibleHistoryCount) + " hóa đơn");
    }
}
