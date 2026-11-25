package com.pharmasiu.mobile.ui.staff.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
    private boolean shiftDetailsExpanded;
    public PosPresenter(Activity activity, UiHost host, View root){super(activity,host,root);}
    public void bind() {
        for (View action : new View[]{openShift, closeShift, cartJump, clearCart, saveSale,
                receiptDismiss, receiptHistory, historyMore, scrollTop,
                root.findViewById(R.id.staff_pos_shift_details_toggle)}) {
            if (action != null) com.pharmasiu.mobile.ui.design.UiMotion.press(action);
        }
        root.findViewById(R.id.staff_pos_shift_details_toggle).setOnClickListener(v -> {
            shiftDetailsExpanded = !shiftDetailsExpanded;
            renderShiftSummary();
        });
        openShift.setOnClickListener(v -> {
            Long amount = money(openingCash.getText().toString());
            if (amount == null) {
                host.message("Tiền đầu ca phải là số không âm.");
                openingCash.setError("Nhập giá trị hợp lệ");
                return;
            }
            if (!state.openShift(amount)) {
                host.message("Không thể mở ca. Vui lòng kiểm tra quyền thu ngân và trạng thái ca.");
                return;
            }
            openingCash.setError(null);
            renderAll();
            host.message("Đã mở ca bán hàng #" + state.shiftNumber);
        });
        closeShift.setOnClickListener(v -> {
            if (!state.canSell() || !state.shiftOpen) return;
            if (!state.posCart.isEmpty()) {
                host.message("Vui lòng thanh toán hoặc xóa giỏ hàng trước khi đóng ca.");
                return;
            }
            new AlertDialog.Builder(activity)
                .setTitle("Đóng ca bán hàng #" + state.shiftNumber + "?")
                .setMessage("Xác nhận đóng ca bán hàng để thực hiện đối soát doanh thu?")
                .setNegativeButton("Quay lại", (dialog, which) -> { })
                .setPositiveButton("Đóng ca bán hàng", (dialog, which) -> {
                    if (state.closeShift()) {
                        renderAll();
                        host.message("Đã đóng ca bán hàng thành công.");
                    } else host.message("Không thể đóng ca khi còn sản phẩm trong giỏ hàng.");
                }).show();
        });
        search.addTextChangedListener(watch(() -> {
            productPage = 0;
            renderProducts();
        }));
        search.setOnEditorActionListener((view, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) return false;
            InputMethodManager keyboard = activity.getSystemService(InputMethodManager.class);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
            return true;
        });
        if (scrollTop != null && scroll != null) {
            scrollTop.setOnClickListener(v -> scroll.smoothScrollTo(0, 0));
            scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                scrollTop.setVisibility(scrollY > 280 ? View.VISIBLE : View.GONE);
            });
        }
        cartJump.setOnClickListener(v -> scrollTo(root.findViewById(R.id.staff_pos_cart_section)));
        historyMore.setOnClickListener(v -> {
            visibleHistoryCount = Math.min(state.sales.size(), visibleHistoryCount + 12);
            renderHistory();
        });
        clearCart.setOnClickListener(v -> {
            if (!state.canSell() || state.posCart.isEmpty()) return;
            new AlertDialog.Builder(activity)
                .setTitle("Xóa giỏ hàng hàng đang chọn?")
                .setMessage("Xác nhận xóa toàn bộ sản phẩm đang có trong giỏ hàng?")
                .setNegativeButton("Giữ giỏ", (dialog, which) -> { })
                .setPositiveButton("Xóa giỏ hàng", (dialog, which) -> {
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
        cartDock.setVisibility(allowed && state.shiftOpen && !state.posCart.isEmpty() ? View.VISIBLE : View.GONE);
        shiftStatus.setText(!allowed ? "Quyền truy cập chưa hợp lệ"
            : state.shiftOpen ? "Ca #" + state.shiftNumber + " đang mở"
                : "Chưa mở ca bán hàng");
        openingGroup.setVisibility(allowed && !state.shiftOpen ? View.VISIBLE : View.GONE);
        activeGroup.setVisibility(allowed && state.shiftOpen ? View.VISIBLE : View.GONE);
        renderShiftSummary();
        closeShift.setEnabled(allowed && state.shiftOpen && state.posCart.isEmpty());
        if (allowed) {
            renderProducts();
            renderCart();
            renderHistory();
        }
        renderReceipt();
    }

    /** Financial details stay available without pushing the product search off-screen. */
    private void renderShiftSummary() {
        boolean open = authorized() && state.shiftOpen;
        if (!open) shiftDetailsExpanded = false;
        root.findViewById(R.id.staff_pos_shift_title).setVisibility(open ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.staff_pos_opening_hint).setVisibility(open ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.staff_pos_intro).setVisibility(open ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.staff_pos_shift_details).setVisibility(open && shiftDetailsExpanded ? View.VISIBLE : View.GONE);
        Button toggle = root.findViewById(R.id.staff_pos_shift_details_toggle);
        toggle.setText(shiftDetailsExpanded ? "Thu gọn" : "Chi tiết ca");
        toggle.setContentDescription((shiftDetailsExpanded ? "Thu gọn" : "Mở")
            + " chi tiết tiền đầu ca, tiền mặt và đóng ca");
        int soldInShift = 0;
        long shiftRevenueVnd = 0L;
        for (DemoState.Sale sale : state.sales) {
            if (sale.shiftNumber == state.shiftNumber) {
                soldInShift++;
                shiftRevenueVnd += sale.amountVnd;
            }
        }
        String details = "Tiền đầu ca: " + vnd(state.openingCashVnd) +
            "\nTiền mặt đã thu: " + vnd(state.cashCollectedVnd) +
            "\nTrong ca: " + soldInShift + " phiếu · " + vnd(shiftRevenueVnd) +
            "\nGiỏ đang lập: " + state.posCart.values().stream().mapToInt(Integer::intValue).sum() + " sản phẩm";
        shiftBalance.setText(soldInShift + " phiếu · " + vnd(shiftRevenueVnd));
        shiftBalance.setContentDescription(details);
        ((TextView) root.findViewById(R.id.staff_pos_shift_financial_details)).setText(details);
    }
}
