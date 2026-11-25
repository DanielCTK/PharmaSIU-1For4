package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.view.Gravity;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.pharmasiu.mobile.ui.staff.StaffMediaViews;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;

/** Single feature screen and its closely related business UI. */
public final class AdminLots {
    private AdminLots() {}

    public static View lots(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_lots, host);
        screen.context(s.lots.size() + " lô hàng · Số lượng và hạn dùng");
        LinearLayout existing = productSection(screen);
        title(existing, "Danh sách lô");
        LinearLayout receiving = productSection(screen);
        View receivingPanel = (View) receiving.getParent();
        receivingPanel.setVisibility(View.GONE);
        Button receivingToggle = screen.root.findViewById(R.id.admin_lot_form_toggle);
        com.pharmasiu.mobile.ui.design.UiMotion.press(receivingToggle);
        Runnable collapseReceiving = () -> {
            receivingPanel.setVisibility(View.GONE);
            receivingToggle.setText("Nhập lô mới");
            receivingToggle.requestRectangleOnScreen(new android.graphics.Rect(
                0, 0, receivingToggle.getWidth(), receivingToggle.getHeight()), false);
        };
        receivingToggle.setOnClickListener(v -> {
            if (!s.canAdmin()) { host.message("Không đủ quyền Quản lý"); return; }
            boolean expanded = receivingPanel.getVisibility() == View.VISIBLE;
            if (expanded) collapseReceiving.run();
            else {
                receivingPanel.setVisibility(View.VISIBLE);
                receivingToggle.setText("Thu gọn nhập lô");
                receiving.post(() -> receiving.requestRectangleOnScreen(new android.graphics.Rect(
                    0, 0, receiving.getWidth(), receiving.getHeight()), false));
            }
        });
        title(receiving, "Nhập lô hàng mới");
        button(receiving, "Thu gọn nhập lô", false, collapseReceiving);
        List<DemoState.Product> choices = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (DemoState.Product p : s.products) {
            if (!p.requiresPrescription) {
                choices.add(p);
                labels.add(p.name);
            }
        }
        LinearLayout selectedProduct = new LinearLayout(a);
        selectedProduct.setOrientation(LinearLayout.HORIZONTAL);
        selectedProduct.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-1, -2);
        previewParams.setMargins(0, dp(a, 12), 0, dp(a, 10));
        receiving.addView(selectedProduct, previewParams);
        Spinner product = picker(receiving, labels.toArray(new String[0]), a);
        product.setContentDescription("Chọn sản phẩm nhập lô");
        product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderSelectedProduct(a, host, s, selectedProduct,
                    position >= 0 && position < choices.size() ? choices.get(position) : null);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                renderSelectedProduct(a, host, s, selectedProduct, null);
            }
        });
        renderSelectedProduct(a, host, s, selectedProduct, choices.isEmpty() ? null : choices.get(0));
        EditText code = input(receiving, "Mã số lô hàng", InputType.TYPE_CLASS_TEXT, "");
        EditText qty = input(receiving, "Số lượng (1 đến 999)", InputType.TYPE_CLASS_NUMBER, "");
        EditText expiry = input(receiving, "Hạn dùng YYYY-MM-DD", InputType.TYPE_CLASS_DATETIME, "");
        android.widget.CheckBox quarantine = new android.widget.CheckBox(a);
        quarantine.setText("Cách ly lô mới");
        quarantine.setMinHeight(dp(a, 48));
        receiving.addView(quarantine, new LinearLayout.LayoutParams(-1, -2));
        button(receiving, "Ghi nhận nhập lô hàng", true, () -> {
            if (!s.canAdmin()) { host.message("Không đủ quyền Quản lý"); return; }
            int index = product.getSelectedItemPosition();
            Integer count = boundedInt(qty.getText().toString(), 1, MAX_RECEIVE);
            LocalDate date = parseDate(expiry.getText().toString());
            String lotCode = code.getText().toString().trim();
            if (index < 0 || index >= choices.size() || count == null ||
                date == null || !date.isAfter(LocalDate.now()) ||
                lotCode.length() < 2 || lotCode.length() > 40 || duplicateLotCode(s, lotCode) ||
                totalLotQty(s, choices.get(index).id) > MAX_RECEIVE - count ||
                s.stock.getOrDefault(choices.get(index).id, 0) > MAX_RECEIVE - count ||
                !s.receiveLot(choices.get(index).id, lotCode, count, date)) {
                host.message("Lô trùng, quá hạn, sản phẩm hoặc số lượng không hợp lệ");
            } else {
                if (quarantine.isChecked()) s.quarantineLot(lotCode);
                host.message("Đã ghi nhận nhập lô hàng thành công");
                host.navigate("lots-v2");
            }
        });
        warning(receiving, "Tối đa 999 đơn vị/mặt hàng. Lô mới cần hạn dùng trong tương lai.");

        View searchControl = a.getLayoutInflater().inflate(R.layout.admin_lot_list_controls, existing, false);
        existing.addView(searchControl);
        EditText lotSearch = searchControl.findViewById(R.id.admin_lot_search);
        TextView lotSummary = line(existing, "", 12, R.color.muted, false);
        LinearLayout lotItemsContainer = new LinearLayout(a);
        lotItemsContainer.setOrientation(LinearLayout.VERTICAL);
        existing.addView(lotItemsContainer, new LinearLayout.LayoutParams(-1, -2));

        final int LOT_PAGE_SIZE = 8;
        final int[] lotPage = {0};

        Runnable renderLotList = new Runnable() {
            @Override
            public void run() {
                lotItemsContainer.removeAllViews();
                String query = lotSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
                List<DemoState.Lot> matched = new ArrayList<>();
                for (DemoState.Lot lot : new ArrayList<>(s.lots)) {
                    DemoState.Product p = s.product(lot.productId);
                    String pName = p == null ? "" : p.name;
                    if (query.isEmpty() ||
                        lot.code.toLowerCase(Locale.ROOT).contains(query) ||
                        lot.productId.toLowerCase(Locale.ROOT).contains(query) ||
                        pName.toLowerCase(Locale.ROOT).contains(query)) {
                        matched.add(lot);
                    }
                }
                int total = matched.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) total / LOT_PAGE_SIZE));
                if (lotPage[0] >= totalPages) lotPage[0] = totalPages - 1;
                if (lotPage[0] < 0) lotPage[0] = 0;

                int start = lotPage[0] * LOT_PAGE_SIZE;
                int end = Math.min(start + LOT_PAGE_SIZE, total);
                lotSummary.setText("Hiển thị " + (total == 0 ? 0 : (start + 1)) + "–" + end + " trên tổng số " + total + " lô hàng");

                if (total == 0) {
                    line(lotItemsContainer, "Chưa có lô hàng phù hợp", 13, R.color.muted, false);
                } else {
                    for (int i = start; i < end; i++) {
                        DemoState.Lot lot = matched.get(i);
                        DemoState.Product p = s.product(lot.productId);
                        LinearLayout lotRow = (LinearLayout) a.getLayoutInflater().inflate(
                            R.layout.admin_lot_item, lotItemsContainer, false);
                        ((TextView) lotRow.findViewById(R.id.admin_lot_product_name))
                            .setText(p == null ? "Sản phẩm không còn trong danh mục" : p.name);
                        ((TextView) lotRow.findViewById(R.id.admin_lot_product_sku)).setText("SKU " + lot.productId);
                        ((TextView) lotRow.findViewById(R.id.admin_lot_product_price)).setText(p == null ? ""
                            : p.priceVnd > 0 ? vnd(p.priceVnd) + " / " + p.unit : "Chưa có giá bán");
                        ((TextView) lotRow.findViewById(R.id.admin_lot_code)).setText("Lô " + lot.code);
                        bindThumbnail(a, host, s, lotRow.findViewById(R.id.admin_lot_photo), p);
                        TextView expiryView = lotRow.findViewById(R.id.admin_lot_expiry);
                        expiryView.setText(lot.expiresOn == null ? "Chưa có HSD" : "HSD " + lot.expiresOn.format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        ((TextView) lotRow.findViewById(R.id.admin_lot_quantity)).setText(
                            lot.quantity + " " + (p == null ? "đơn vị" : p.unit));
                        boolean blocked = lot.quarantined || lot.expiresOn == null
                            || !lot.expiresOn.isAfter(LocalDate.now());
                        boolean expiring = !blocked && lot.quantity > 0
                            && !lot.expiresOn.isAfter(LocalDate.now().plusDays(30));
                        String status = lot.quarantined ? "Cách ly" : lot.expiresOn == null ? "Thiếu hạn dùng"
                            : !lot.expiresOn.isAfter(LocalDate.now()) ? "Hết hạn"
                            : lot.quantity < 1 ? "Hết số lượng" : expiring ? "Sắp hết hạn" : "Hạn hợp lệ";
                        TextView statusChip = lotRow.findViewById(R.id.admin_lot_status);
                        statusChip.setText(status);
                        statusChip.setTextColor(a.getColor(blocked || expiring ? R.color.warning : R.color.forest));
                        android.graphics.drawable.GradientDrawable chipBackground = new android.graphics.drawable.GradientDrawable();
                        chipBackground.setColor(a.getColor(blocked || expiring ? R.color.warning_surface : R.color.mint));
                        chipBackground.setCornerRadius(dp(a, 8));
                        statusChip.setBackground(chipBackground);
                        expiryView.setTextColor(a.getColor(blocked || expiring ? R.color.warning : R.color.forest));
                        LinearLayout lotActions = lotRow.findViewById(R.id.admin_lot_actions);
                        if (!lot.quarantined && lot.quantity > 0) {
                            lotAction(lotActions, "Cách ly lô", () -> {
                                if (!s.canAdmin() || !s.quarantineLot(lot.code)) {
                                    host.message("Không đủ quyền hoặc lô không thể cách ly");
                                } else {
                                    host.message("Đã cách ly lô hàng; hệ thống đã cập nhật tồn khả dụng");
                                    host.navigate("lots-v2");
                                }
                            }).setContentDescription("Cách ly lô " + lot.code);
                        } else if (lot.quarantined && lot.quantity > 0) {
                            lotAction(lotActions, "Bỏ cách ly", () -> {
                                if (restoreDemoLot(s, lot)) {
                                    host.message("Đã mở lại lô hàng hợp lệ");
                                    host.navigate("lots-v2");
                                } else host.message("Lô không đủ điều kiện mở lại: vui lòng kiểm tra quyền, hạn dùng hoặc số lượng tồn");
                            }).setContentDescription("Bỏ cách ly lô " + lot.code);
                        }
                        lotItemsContainer.addView(lotRow);
                    }
                }

                if (totalPages > 1) {
                    PharmaUi.pagination(lotItemsContainer, lotPage[0], totalPages, total, "lô", newPage -> {
                        lotPage[0] = newPage;
                        this.run();
                        if (screen.scroll != null) {
                            existing.requestRectangleOnScreen(new android.graphics.Rect(
                                0, 0, existing.getWidth(), dp(a, 120)), false);
                        }
                    });
                }
            }
        };

        lotSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                lotPage[0] = 0;
                renderLotList.run();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        renderLotList.run();
        line(existing, "Lô cách ly hoặc hết hạn không dùng để bán.", 13, R.color.muted, false);
        return screen.root;
    }

    private static void bindThumbnail(Activity activity, UiHost host, DemoState state,
                                      FrameLayout holder, DemoState.Product product) {
        holder.removeAllViews();
        if (product != null) {
            View thumbnail = StaffMediaViews.createThumbnail(activity, host, product,
                StaffMediaViews.demoPhoto(state, product.id),
                () -> AdminMedia.showProductPhoto(activity, host, product, "lots-v2"));
            holder.addView(thumbnail, new FrameLayout.LayoutParams(-1, -1));
        } else {
            ImageView unavailable = new ImageView(activity);
            unavailable.setImageResource(R.drawable.admin_lot_box);
            unavailable.setPadding(dp(activity, 20), dp(activity, 20), dp(activity, 20), dp(activity, 20));
            unavailable.setBackgroundResource(R.drawable.admin_lot_panel);
            unavailable.setContentDescription("Không có ảnh cho sản phẩm không còn trong danh mục");
            holder.addView(unavailable, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    private static Button lotAction(LinearLayout parent, String label, Runnable action) {
        Button control = button(parent, label, false, action);
        control.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        if (control instanceof com.google.android.material.button.MaterialButton) {
            ((com.google.android.material.button.MaterialButton) control).setStrokeWidth(0);
            ((com.google.android.material.button.MaterialButton) control).setCornerRadius(dp(parent.getContext(), 8));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.END;
        control.setLayoutParams(params);
        return control;
    }

    private static void renderSelectedProduct(Activity activity, UiHost host, DemoState state,
                                              LinearLayout preview, DemoState.Product product) {
        preview.removeAllViews();
        if (product == null) {
            line(preview, "Chưa có sản phẩm phù hợp để nhập lô.", 14, R.color.muted, false);
            return;
        }
        FrameLayout picture = new FrameLayout(activity);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(activity, 72), dp(activity, 72));
        imageParams.setMarginEnd(dp(activity, 12));
        preview.addView(picture, imageParams);
        bindThumbnail(activity, host, state, picture, product);
        LinearLayout text = new LinearLayout(activity);
        text.setOrientation(LinearLayout.VERTICAL);
        preview.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        line(text, product.name, 16, R.color.ink, true);
        line(text, product.id + " · " + product.unit, 13, R.color.muted, false);
    }

    public static boolean duplicateLotCode(DemoState s, String code) {
        if (s == null || code == null) return false;
        for (DemoState.Lot lot : s.lots) if (lot.code.equalsIgnoreCase(code.trim())) return true;
        return false;
    }

    private static int totalLotQty(DemoState s, String productId) {
        int count = 0;
        for (DemoState.Lot lot : s.lots) if (lot.productId.equals(productId)) count += lot.quantity;
        return count;
    }

    public static boolean restoreDemoLot(DemoState s, DemoState.Lot lot) {
        if (s == null || !s.canAdmin() || lot == null || !lot.quarantined ||
            lot.quantity <= 0 || lot.expiresOn == null || !lot.expiresOn.isAfter(LocalDate.now())) return false;
        DemoState.Product product = s.product(lot.productId);
        if (product == null || product.requiresPrescription) return false;
        int stock = s.stock.getOrDefault(lot.productId, 0);
        if (stock < 0 || stock > MAX_RECEIVE - lot.quantity) return false;
        lot.quarantined = false;
        s.stock.put(lot.productId, stock + lot.quantity);
        return true;
    }
}
