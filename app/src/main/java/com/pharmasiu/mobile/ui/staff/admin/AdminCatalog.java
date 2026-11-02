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
import static com.pharmasiu.mobile.ui.staff.admin.AdminMedia.*;

/** Single feature screen and its closely related business UI. */
public final class AdminCatalog {
    private AdminCatalog() {}

    public static View catalog(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_catalog);
        LinearLayout editor = card(screen);
        title(editor, "Sửa mặt hàng đã có");
        Spinner select = picker(editor, productLabels(s), a);
        EditText name = input(editor, "Tên mặt hàng", InputType.TYPE_CLASS_TEXT, "");
        EditText price = input(editor, "Giá niêm yết (VND)", InputType.TYPE_CLASS_NUMBER, "");
        Runnable load = () -> {
            int index = select.getSelectedItemPosition();
            if (index < 0 || index >= s.products.size()) return;
            DemoState.Product p = s.products.get(index);
            name.setText(p.name);
            price.setText(p.priceVnd > 0 ? String.valueOf(p.priceVnd) : "");
            boolean allowed = !p.requiresPrescription;
            name.setEnabled(allowed);
            price.setEnabled(allowed);
        };
        select.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { load.run(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        load.run();
        button(editor, "Lưu thông tin sản phẩm", true, () -> {
            if (!s.canAdmin()) { host.message("Không đủ quyền Quản lý"); return; }
            int index = select.getSelectedItemPosition();
            if (index < 0 || index >= s.products.size()) return;
            Long amount = positiveLong(price.getText().toString(), MAX_PRICE);
            if (amount == null || s.posCart.containsKey(s.products.get(index).id) ||
                !s.renameProduct(s.products.get(index).id,
                name.getText().toString(), amount)) {
                host.message("Tên/giá không hợp lệ, Rx bị khóa hoặc mặt hàng đang ở giỏ POS");
            } else {
                host.message("Đã cập nhật thông tin sản phẩm thành công");
                host.navigate("catalog-admin-v2");
            }
        });
        warning(editor, "Thuốc kê đơn (Rx) cần có sự phê duyệt của Dược sĩ trước khi xuất bán hoặc điều chỉnh giá.");

        LinearLayout create = card(screen);
        title(create, "Thêm mặt hàng mới");
        EditText newName = input(create, "Tên hàng mới", InputType.TYPE_CLASS_TEXT, "");
        Spinner category = picker(create, NON_RX_CATEGORIES, a);
        EditText unit = input(create, "Đơn vị (hộp, chai...)", InputType.TYPE_CLASS_TEXT, "");
        EditText newPrice = input(create, "Giá bán (VND)", InputType.TYPE_CLASS_NUMBER, "");
        EditText initial = input(create, "Tồn mở đầu từ 0 đến 999", InputType.TYPE_CLASS_NUMBER, "0");
        EditText lotCode = input(create, "Mã lô mở đầu (nếu có tồn)", InputType.TYPE_CLASS_TEXT, "");
        EditText expires = input(create, "Hạn YYYY-MM-DD (nếu có tồn)", InputType.TYPE_CLASS_DATETIME, "");
        final Uri[] newPhoto = {null};
        button(create, "Chọn ảnh từ thiết bị (không bắt buộc)", false, () ->
            pickPhoto(a, host, uri -> {
                newPhoto[0] = uri;
                host.message("Đã chọn ảnh; hãy lưu mặt hàng để hoàn tất");
            }));
        button(create, "Lưu thông tin sản phẩm", true, () -> {
            Long amount = positiveLong(newPrice.getText().toString(), MAX_PRICE);
            Integer qty = boundedInt(initial.getText().toString(), 0, MAX_RECEIVE);
            String categoryValue = category.getSelectedItem() == null ? "" : category.getSelectedItem().toString();
            LocalDate expiry = parseDate(expires.getText().toString());
            if (amount == null || qty == null || !createDemoProduct(s,
                newName.getText().toString(), categoryValue, unit.getText().toString(),
                amount, qty, lotCode.getText().toString(), expiry)) {
                host.message("Kiểm tra tên, giá, tồn, mã lô và hạn dùng; không tạo thuốc Rx");
            } else {
                if (!s.isDatabaseBacked() && newPhoto[0] != null) {
                    DemoState.Product created = s.products.get(s.products.size() - 1);
                    StaffMediaViews.changeDemoPhoto(s, created.id, newPhoto[0],
                        a.getContentResolver().getType(newPhoto[0]));
                }
                host.message("Đã lưu sản phẩm vào danh mục nhà thuốc");
                host.navigate("catalog-admin-v2");
            }
        });
        warning(create, "Tồn mở đầu cần mã lô và hạn dùng hợp lệ. Ảnh sản phẩm được lưu trên thiết bị. Hệ thống tự động ưu tiên xuất lô cận hạn trước.");

        LinearLayout listing = card(screen);
        title(listing, "Danh mục sản phẩm nhà thuốc");
        EditText catalogSearch = input(listing, "Tìm sản phẩm (tên, mã, nhóm hàng)...", InputType.TYPE_CLASS_TEXT, "");
        TextView catalogSummary = line(listing, "", 12, R.color.muted, false);
        LinearLayout itemsContainer = new LinearLayout(a);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        listing.addView(itemsContainer, new LinearLayout.LayoutParams(-1, -2));

        final int PAGE_SIZE = 8;
        final int[] catalogPage = {0};

        Runnable renderList = new Runnable() {
            @Override
            public void run() {
                itemsContainer.removeAllViews();
                String query = catalogSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
                List<DemoState.Product> matched = new ArrayList<>();
                for (DemoState.Product p : s.products) {
                    if (query.isEmpty() ||
                        p.name.toLowerCase(Locale.ROOT).contains(query) ||
                        p.id.toLowerCase(Locale.ROOT).contains(query) ||
                        p.category.toLowerCase(Locale.ROOT).contains(query)) {
                        matched.add(p);
                    }
                }
                int total = matched.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
                if (catalogPage[0] >= totalPages) catalogPage[0] = totalPages - 1;
                if (catalogPage[0] < 0) catalogPage[0] = 0;

                int start = catalogPage[0] * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, total);
                catalogSummary.setText("Hiển thị " + (total == 0 ? 0 : (start + 1)) + "–" + end + " trên tổng số " + total + " sản phẩm");

                if (total == 0) {
                    line(itemsContainer, "Không có sản phẩm nào khớp tìm kiếm", 13, R.color.muted, false);
                } else {
                    for (int i = start; i < end; i++) {
                        DemoState.Product p = matched.get(i);
                        Uri photo = StaffMediaViews.demoPhoto(s, p.id);
                        itemsContainer.addView(StaffMediaViews.createThumbnail(a, host, p, photo,
                            () -> showProductPhoto(a, host, p)),
                            new LinearLayout.LayoutParams(dp(a, 76), dp(a, 76)));
                        line(itemsContainer, p.name, 15, R.color.ink, true);
                        line(itemsContainer, p.category + " · " + p.id + " · " +
                            (p.priceVnd > 0 ? vnd(p.priceVnd) : "Chưa báo giá") +
                            " · tồn khả dụng " + s.stock.getOrDefault(p.id, 0), 12, R.color.muted, false);
                        if (p.requiresPrescription) line(itemsContainer, "Rx · CHỜ DUYỆT CHUYÊN MÔN", 12, R.color.warning, true);
                        button(itemsContainer, "Xem hình / đổi ảnh", false, () -> showProductPhoto(a, host, p));
                        divider(itemsContainer);
                    }
                }

                if (totalPages > 1) {
                    PharmaUi.pagination(itemsContainer, catalogPage[0], totalPages, total, "sản phẩm", newPage -> {
                        catalogPage[0] = newPage;
                        this.run();
                        if (screen.scroll != null) {
                            screen.scroll.smoothScrollTo(0, listing.getTop());
                        }
                    });
                }
            }
        };

        catalogSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                catalogPage[0] = 0;
                renderList.run();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        renderList.run();
        return screen.root;
    }

    /**
     * A small UI-owned RAM mutation, because the current DemoState has no add-product
     * method. All validations happen before insertion. A newly counted quantity is
     * only admitted with an eligible dated demo lot.
     */
    public static boolean createDemoProduct(DemoState s, String name, String category,
                                     String unit, long price, int openingQty,
                                     String lotCode, LocalDate expiry) {
        if (s != null && s.isDatabaseBacked()) {
            if (!s.canAdmin()) return false;
            boolean saved=s.repository().createProduct(s.authenticatedAccountId(),
                name,unit,price,openingQty,lotCode,expiry);
            if(saved)s.refreshDatabaseViews();
            return saved;
        }
        if (s == null || !s.canAdmin() || name == null || unit == null ||
            name.trim().length() < 2 || name.trim().length() > 90 ||
            unit.trim().isEmpty() || unit.trim().length() > 24 ||
            price < 1 || price > MAX_PRICE || openingQty < 0 || openingQty > MAX_RECEIVE) return false;
        boolean recognized = false;
        for (String value : NON_RX_CATEGORIES) if (value.equals(category)) recognized = true;
        if (!recognized) return false;
        for (DemoState.Product p : s.products)
            if (p.name.trim().equalsIgnoreCase(name.trim())) return false;
        if (openingQty > 0) {
            if (lotCode == null || lotCode.trim().length() < 2 || lotCode.trim().length() > 40 ||
                expiry == null || !expiry.isAfter(LocalDate.now())) return false;
            for (DemoState.Lot lot : s.lots)
                if (lot.code.equalsIgnoreCase(lotCode.trim())) return false;
        }
        int serial = s.products.size() + 1;
        String id;
        do { id = String.format(Locale.ROOT, "admin-demo-%04d", serial++); }
        while (s.product(id) != null);
        DemoState.Product p = new DemoState.Product(id, name.trim(),
            "Sản phẩm lưu hành nội bộ", price, category, false, unit.trim(),
            "Mặt hàng lưu hành nội bộ theo tiêu chuẩn nhà thuốc PharmaSIU.");
        s.products.add(p);
        s.stock.put(id, 0);
        if (openingQty > 0 && !s.receiveLot(id, lotCode.trim(), openingQty, expiry)) {
            s.products.remove(p);
            s.stock.remove(id);
            return false;
        }
        return true;
    }
}
