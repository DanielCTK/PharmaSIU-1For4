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

/** Binds screen listeners and orchestrates each section. */
public final class PosPresenter extends PosReceiptFeatures {
    public PosPresenter(Activity activity, UiHost host, View root){super(activity,host,root);}
    public void bind() {
        openShift.setOnClickListener(v -> {
            Long amount = money(openingCash.getText().toString());
            if (amount == null) {
                host.message("Tiền đầu ca mẫu phải là số không âm.");
                openingCash.setError("Nhập giá trị hợp lệ");
                return;
            }
            if (!state.openShift(amount)) {
                host.message("Không thể mở ca demo. Kiểm tra quyền và trạng thái ca.");
                return;
            }
            openingCash.setError(null);
            renderAll();
            host.message("Đã mở ca DEMO #" + state.shiftNumber);
        });
        closeShift.setOnClickListener(v -> {
            if (!state.canSell() || !state.shiftOpen) return;
            if (!state.posCart.isEmpty()) {
                host.message("Hãy lưu phiếu hoặc hủy giỏ demo trước khi đóng ca.");
                return;
            }
            new AlertDialog.Builder(activity)
                .setTitle("Đóng ca DEMO #" + state.shiftNumber + "?")
                .setMessage("Ca chỉ được đóng trong RAM. Không xác nhận đã kiểm tiền hoặc đối soát thật.")
                .setNegativeButton("Quay lại", (dialog, which) -> { })
                .setPositiveButton("Đóng ca mẫu", (dialog, which) -> {
                    if (state.closeShift()) {
                        renderAll();
                        host.message("Đã đóng ca DEMO. Chưa có đối soát thực tế.");
                    } else host.message("Không thể đóng ca khi còn giỏ hoặc không đủ quyền.");
                }).show();
        });
        search.addTextChangedListener(watch(() -> renderProducts()));
        cartJump.setOnClickListener(v -> scrollTo(cartRows));
        historyMore.setOnClickListener(v -> {
            visibleHistoryCount = Math.min(state.sales.size(), visibleHistoryCount + 12);
            renderHistory();
        });
        clearCart.setOnClickListener(v -> {
            if (!state.canSell() || state.posCart.isEmpty()) return;
            new AlertDialog.Builder(activity)
                .setTitle("Hủy giỏ demo đang lập?")
                .setMessage("Chỉ bỏ các dòng trong giỏ mô phỏng, chưa tạo phiếu bán.")
                .setNegativeButton("Giữ giỏ", (dialog, which) -> { })
                .setPositiveButton("Hủy giỏ", (dialog, which) -> {
                    for (String id : new ArrayList<>(state.posCart.keySet())) state.changePosQty(id, 0);
                    clearError();
                    renderAll();
                }).show();
        });
        cash.setChecked(true);
        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            cashGroup.setVisibility(checkedId == R.id.staff_pos_other ? View.GONE : View.VISIBLE);
            clearError();
            renderPayment();
        });
        tender.addTextChangedListener(watch(this::renderPayment));
        saveSale.setOnClickListener(v -> saveDemoSale());
        receiptDismiss.setOnClickListener(v -> {
            displayedReceipt = null;
            receiptPanel.setVisibility(View.GONE);
            scroll.smoothScrollTo(0, 0);
        });
        receiptHistory.setOnClickListener(v -> {
            displayedReceipt = null;
            renderReceipt();
            scrollTo(historyRows);
        });
        renderAll();
    }

    protected boolean authorized() {
        return state.canSell() && (DemoState.CASHIER.equals(state.role) || DemoState.ADMIN.equals(state.role));
    }

    protected void renderAll() {
        boolean allowed = authorized();
        denied.setVisibility(allowed ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.staff_pos_shift_panel).setVisibility(allowed ? View.VISIBLE : View.GONE);
        salesPanel.setVisibility(allowed && state.shiftOpen ? View.VISIBLE : View.GONE);
        shiftStatus.setText(!allowed ? "Quyền truy cập chưa hợp lệ"
            : state.shiftOpen ? "●  CA DEMO #" + state.shiftNumber + " ĐANG MỞ"
                : "●  CHƯA MỞ CA BÁN HÀNG");
        openingGroup.setVisibility(allowed && !state.shiftOpen ? View.VISIBLE : View.GONE);
        activeGroup.setVisibility(allowed && state.shiftOpen ? View.VISIBLE : View.GONE);
        int soldInShift = 0;
        long shiftRevenueVnd = 0L;
        for (DemoState.Sale sale : state.sales) {
            if (sale.shiftNumber == state.shiftNumber) {
                soldInShift++;
                shiftRevenueVnd += sale.amountVnd;
            }
        }
        shiftBalance.setText("Tiền đầu ca mẫu: " + vnd(state.openingCashVnd) +
            "\nTiền mặt ghi trên phiếu demo: " + vnd(state.cashCollectedVnd) +
            "\nTrong ca: " + soldInShift + " phiếu · " + vnd(shiftRevenueVnd) +
            "\nGiỏ đang lập: " + state.posCart.values().stream().mapToInt(Integer::intValue).sum() + " sản phẩm");
        closeShift.setEnabled(allowed && state.shiftOpen && state.posCart.isEmpty());
        if (allowed) {
            renderProducts();
            renderCart();
            renderHistory();
        }
        renderReceipt();
    }
}
