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
import android.widget.FrameLayout;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.domain.JavaRules;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories;

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
            if (fold(product.name + " " + product.id + " " + CatalogCategories.categoryOf(product)).contains(query)) {
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
            com.pharmasiu.mobile.ui.design.UiMotion.press(add);
            FrameLayout artwork = row.findViewById(R.id.staff_pos_product_artwork);
            com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.bindPicture(activity, artwork, product,
                com.pharmasiu.mobile.ui.staff.StaffMediaViews.demoPhoto(state, product.id), "Ảnh sản phẩm");
            int aggregate = Math.max(0, state.stock.getOrDefault(product.id, 0));
            boolean dated = hasLot(product.id);
            int usable = effectiveAvailable(product.id);
            int inCart = state.posCart.getOrDefault(product.id, 0);
            title.setText(product.name);
            meta.setText("SKU " + product.id + " · " + CatalogCategories.categoryOf(product));
            ((TextView) row.findViewById(R.id.staff_pos_product_price)).setText(
                product.priceVnd > 0 ? vnd(product.priceVnd) + " / " + product.unit : "Chưa có giá");
            ((TextView) row.findViewById(R.id.staff_pos_product_stock)).setText(
                "Khả dụng " + usable + " · Tồn " + aggregate + (inCart > 0 ? " · Đã chọn " + inCart : ""));
            String blocked = product.requiresPrescription ? "Rx · Chờ dược sĩ, không bán tại quầy" :
                product.priceVnd <= 0 ? "Chưa cập nhật giá bán" :
                usable < 1 ? "Không có tồn/lô hợp lệ" :
                dated ? nextLotLabel(product.id) : "Chưa có lô theo dõi hạn dùng";
            status.setText(blocked);
            row.setFocusable(true);
            row.setClickable(true);
            row.setContentDescription("Xem thông tin chi tiết " + product.name);
            row.setOnClickListener(v -> showProductDetails(product));
            com.pharmasiu.mobile.ui.design.UiMotion.press(row);
            status.setTextColor(activity.getColor(product.requiresPrescription || usable == 0 || product.priceVnd <= 0
                ? R.color.warning : R.color.emerald));
            boolean canAdd = JavaRules.canSellDemo(product, inCart + 1, usable, state.shiftOpen) &&
                inCart < 30 && authorized();
            add.setEnabled(canAdd);
            add.setContentDescription("Thêm " + product.name + " vào giỏ bán hàng");
            add.setAlpha(canAdd ? 1f : .5f);
            add.setText(canAdd ? "+ Thêm" : product.requiresPrescription ? "Rx · Khóa" :
                product.priceVnd <= 0 ? "Chưa có giá" : inCart >= 30 ? "Đủ 30" : "Hết khả dụng");
            add.setOnClickListener(v -> {
                if (!authorized() || !JavaRules.canSellDemo(product,
                    state.posCart.getOrDefault(product.id, 0) + 1,
                    effectiveAvailable(product.id), state.shiftOpen) || !state.addPos(product.id)) {
                    host.message("Không thể thêm vào giỏ. Vui lòng kiểm tra quyền, thuốc Rx, giá và số lượng tồn.");
                    renderAll();
                    return;
                }
                clearError();
                renderAll();
            });
            productRows.addView(row);
        }

        searchSummary.setText(totalMatches + " mặt hàng · " + (totalMatches == 0 ? 0 : (start + 1))
            + "–" + end + " · Trang " + (productPage + 1) + "/" + totalPages);
        if (totalMatches == 0) productRows.addView(simpleText("Không tìm thấy sản phẩm phù hợp.", 14, R.color.muted));

        if (totalPages > 1) {
            com.pharmasiu.mobile.ui.design.PharmaUi.pagination(productRows, productPage, totalPages, totalMatches, "mặt hàng", newPage -> {
                productPage = newPage;
                renderProducts();
                scrollTo(search);
            });
        }
    }
    protected void showProductDetails(DemoState.Product product) {
        if (!authorized()) return;
        String specification = CatalogCategories.detailOf(product);
        String details = (specification.isEmpty() ? "" : specification + "\n\n") + product.description +
            "\n\nMã: " + product.id + "\nNhóm: " + CatalogCategories.categoryOf(product) +
            "\nĐơn vị: " + product.unit +
            "\nGiá niêm yết: " + (product.priceVnd > 0 ? vnd(product.priceVnd) : "Chưa cập nhật giá") +
            "\nTồn kho khả dụng: " + effectiveAvailable(product.id) +
            "\n" + (product.requiresPrescription ? "Rx: Chờ dược sĩ; không được bán tại quầy."
                : "Thông tin từ danh mục demo trên thiết bị.");
        if (hasLot(product.id)) details += "\n" + lotDescription(product.id);
        ScrollView content = new ScrollView(activity);
        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(16 * activity.getResources().getDisplayMetrics().density);
        body.setPadding(padding, padding, padding, padding);
        content.addView(body, new ScrollView.LayoutParams(-1, -2));
        FrameLayout photo = new FrameLayout(activity);
        body.addView(photo, new LinearLayout.LayoutParams(-1, Math.round(
            180 * activity.getResources().getDisplayMetrics().density)));
        com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.bindPicture(activity, photo, product,
            com.pharmasiu.mobile.ui.staff.StaffMediaViews.demoPhoto(state, product.id), "Ảnh sản phẩm");
        body.addView(simpleText(details, 15, R.color.ink));
        new AlertDialog.Builder(activity)
            .setTitle(product.name)
            .setView(content)
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

    private String nextLotLabel(String productId) {
        LocalDate today = LocalDate.now();
        DemoState.Lot first = null;
        for (DemoState.Lot lot : state.lots) {
            if (!productId.equals(lot.productId) || lot.quarantined || lot.quantity < 1
                || lot.expiresOn == null || !lot.expiresOn.isAfter(today)) continue;
            if (first == null || lot.expiresOn.isBefore(first.expiresOn)
                || (lot.expiresOn.equals(first.expiresOn) && lot.code.compareTo(first.code) < 0)) first = lot;
        }
        return first == null ? "Không có lô đủ điều kiện" : "Lô " + first.code + " · HSD "
            + first.expiresOn.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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
            "Hạn sử dụng: " + available.get(0).code + " · HSD " + available.get(0).expiresOn;
        if (expired > 0 || quarantined > 0)
            detail += " · " + expired + " lô hết hạn/thiếu hạn · " + quarantined + " lô cách ly";
        return detail;
    }
}
