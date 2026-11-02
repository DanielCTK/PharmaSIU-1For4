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

/** Catalog rows, stock hints and item detail for POS. */
abstract class PosProductFeatures extends PosBase {
    PosProductFeatures(Activity activity, UiHost host, View root){super(activity,host,root);}
    protected void renderProducts() {
        if (!authorized() || !state.shiftOpen) return;
        productRows.removeAllViews();
        String query = fold(search.getText().toString().trim());
        List<DemoState.Product> matched = new ArrayList<>();
        for (DemoState.Product product : state.products) {
            if (fold(product.name + " " + product.id + " " + product.category).contains(query)) {
                matched.add(product);
            }
        }
        int totalMatches = matched.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalMatches / PRODUCT_PAGE_SIZE));
        if (productPage >= totalPages) productPage = totalPages - 1;
        if (productPage < 0) productPage = 0;

        int start = productPage * PRODUCT_PAGE_SIZE;
        int end = Math.min(start + PRODUCT_PAGE_SIZE, totalMatches);

        for (int i = start; i < end; i++) {
            DemoState.Product product = matched.get(i);
            View row = activity.getLayoutInflater().inflate(R.layout.staff_pos_product_item, productRows, false);
            TextView title = row.findViewById(R.id.staff_pos_product_title);
            TextView meta = row.findViewById(R.id.staff_pos_product_meta);
            TextView status = row.findViewById(R.id.staff_pos_product_status);
            Button add = row.findViewById(R.id.staff_pos_product_add);
            int aggregate = Math.max(0, state.stock.getOrDefault(product.id, 0));
            boolean dated = hasLot(product.id);
            int usable = effectiveAvailable(product.id);
            int inCart = state.posCart.getOrDefault(product.id, 0);
            title.setText(product.name);
            meta.setText(product.category + " · " + product.unit + " · " + product.id +
                "\nGiá bán: " + (product.priceVnd > 0 ? vnd(product.priceVnd) : "Chưa có giá"));
            String blocked = product.requiresPrescription ? "Rx: CHỜ DƯỢC SĨ / KHÔNG ĐƯỢC BÁN" :
                product.priceVnd <= 0 ? "Chưa cập nhật giá bán" :
                dated ? "Tồn tổng " + aggregate + " · Lô hợp lệ " + usable +
                    " · Đang chọn " + inCart + "\n" + lotDescription(product.id) :
                    "Tồn kho " + aggregate + " · Đang chọn " + inCart +
                        "\nLô hàng khả dụng theo tiêu chuẩn FEFO";
            status.setText(blocked);
            row.setFocusable(true);
            row.setClickable(true);
            row.setContentDescription("Xem thông tin chi tiết " + product.name);
            row.setOnClickListener(v -> showProductDetails(product));
            status.setTextColor(activity.getColor(product.requiresPrescription || usable == 0 || product.priceVnd <= 0
                ? R.color.warning : R.color.emerald));
            boolean canAdd = JavaRules.canSellDemo(product, inCart + 1, usable, state.shiftOpen) &&
                inCart < 30 && authorized();
            add.setEnabled(canAdd);
            add.setAlpha(canAdd ? 1f : .5f);
            add.setText(canAdd ? "+ Thêm vào giỏ" :
                product.requiresPrescription ? "Rx · Chờ duyệt đơn" : "Hết tồn khả dụng");
            add.setOnClickListener(v -> {
                if (!authorized() || !JavaRules.canSellDemo(product,
                    state.posCart.getOrDefault(product.id, 0) + 1,
                    effectiveAvailable(product.id), state.shiftOpen) || !state.addPos(product.id)) {
                    host.message("Không thể thêm. Kiểm tra vai trò, Rx, giá, lô/hạn và tồn kho.");
                    renderAll();
                    return;
                }
                clearError();
                renderAll();
            });
            productRows.addView(row);
        }

        searchSummary.setText(totalMatches + " mặt hàng phù hợp · hiển thị " + (totalMatches == 0 ? 0 : (start + 1)) + "–" + end + " (trang " + (productPage + 1) + "/" + totalPages + ")");
        if (totalMatches == 0) productRows.addView(simpleText("Không tìm thấy sản phẩm phù hợp.", 14, R.color.muted));

        if (totalPages > 1) {
            com.pharmasiu.mobile.ui.design.PharmaUi.pagination(productRows, productPage, totalPages, totalMatches, "mặt hàng", newPage -> {
                productPage = newPage;
                renderProducts();
                scroll.smoothScrollTo(0, search.getTop());
            });
        }
    }
    protected void showProductDetails(DemoState.Product product) {
        if (!authorized()) return;
        String details = product.subtitle + "\n\n" + product.description +
            "\n\nMã: " + product.id + "\nNhóm: " + product.category +
            "\nĐơn vị: " + product.unit +
            "\nGiá niêm yết: " + (product.priceVnd > 0 ? vnd(product.priceVnd) : "Chưa cập nhật giá") +
            "\nTồn kho khả dụng: " + effectiveAvailable(product.id) +
            "\n" + (product.requiresPrescription ? "Rx: Cần đơn thuốc hợp lệ từ bác sĩ."
                : "Thông tin chi tiết sản phẩm chính hãng.");
        if (hasLot(product.id)) details += "\n" + lotDescription(product.id);
        new AlertDialog.Builder(activity)
            .setTitle(product.name)
            .setMessage(details)
            .setPositiveButton("Đóng", (dialog, which) -> { })
            .show();
    }

    protected void scrollTo(View target) {
        target.post(() -> {
            Rect rect = new Rect();
            target.getDrawingRect(rect);
            scroll.offsetDescendantRectToMyCoords(target, rect);
            int inset = Math.round(14 * activity.getResources().getDisplayMetrics().density);
            scroll.smoothScrollTo(0, Math.max(0, rect.top - inset));
        });
    }

    protected boolean hasLot(String id) {
        for (DemoState.Lot lot : state.lots) if (id.equals(lot.productId)) return true;
        return false;
    }

    protected int effectiveAvailable(String id) {
        int aggregate = Math.max(0, state.stock.getOrDefault(id, 0));
        if (!hasLot(id)) return aggregate; // Existing undated seed is only a RAM demo fallback.
        return Math.max(0, Math.min(aggregate, state.eligibleLotQty(id, LocalDate.now())));
    }

    protected String lotDescription(String id) {
        List<DemoState.Lot> available = new ArrayList<>();
        int expired = 0, quarantined = 0;
        LocalDate today = LocalDate.now();
        for (DemoState.Lot lot : state.lots) {
            if (!id.equals(lot.productId)) continue;
            if (lot.quarantined) quarantined++;
            else if (lot.expiresOn == null || !lot.expiresOn.isAfter(today)) expired++;
            else if (lot.quantity > 0) available.add(lot);
        }
        available.sort(Comparator.comparing((DemoState.Lot l) -> l.expiresOn)
            .thenComparing(l -> l.code));
        String detail = available.isEmpty() ? "Không có lô đủ điều kiện thử bán" :
            "Ưu tiên FEFO: " + available.get(0).code + " · HSD " + available.get(0).expiresOn;
        if (expired > 0 || quarantined > 0)
            detail += " · " + expired + " lô hết hạn/thiếu hạn · " + quarantined + " lô cách ly";
        return detail;
    }
}
