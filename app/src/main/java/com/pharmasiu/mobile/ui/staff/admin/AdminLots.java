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

/** Single feature screen and its closely related business UI. */
public final class AdminLots {
    private AdminLots() {}

    public static View lots(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_lots, host);
        LinearLayout receiving = card(screen);
        title(receiving, "Nhập lô hàng mới");
        List<DemoState.Product> choices = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (DemoState.Product p : s.products) {
            if (!p.requiresPrescription) {
                choices.add(p);
                labels.add(p.name);
            }
        }
        Spinner product = picker(receiving, labels.toArray(new String[0]), a);
        EditText code = input(receiving, "Mã số lô hàng", InputType.TYPE_CLASS_TEXT, "");
        EditText qty = input(receiving, "Số lượng (1 đến 999)", InputType.TYPE_CLASS_NUMBER, "");
        EditText expiry = input(receiving, "Hạn dùng YYYY-MM-DD", InputType.TYPE_CLASS_DATETIME, "");
        android.widget.CheckBox quarantine = new android.widget.CheckBox(a);
        quarantine.setText("Đưa lô mới vào diện cách ly kiểm định");
        receiving.addView(quarantine, new LinearLayout.LayoutParams(-1, dp(a, 54)));
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
        warning(receiving, "Quy định: Tổng tồn tối đa 999 đơn vị/mặt hàng; các lô cách ly hoặc quá hạn sẽ không được phép xuất bán.");

        LinearLayout existing = card(screen);
        title(existing, "Danh sách lô và hạn");
        EditText lotSearch = input(existing, "Tìm mã lô hoặc tên sản phẩm...", InputType.TYPE_CLASS_TEXT, "");
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
                        String status = lot.quarantined ? "CÁCH LY" :
                            lot.expiresOn == null ? "CHƯA XÁC MINH HẠN" :
                            !lot.expiresOn.isAfter(LocalDate.now()) ? "HẾT HẠN" :
                            lot.quantity < 1 ? "HẾT SỐ LƯỢNG" : "ĐỦ ĐIỀU KIỆN XUẤT BÁN (HSD HỢP LỆ)";
                        line(lotItemsContainer, lot.code + " · " + (p == null ? lot.productId : p.name), 15, R.color.ink, true);
                        line(lotItemsContainer, "Hạn " + lot.expiresOn + " · " + lot.quantity + " đơn vị · " + status,
                            12, lot.quarantined || lot.expiresOn == null ||
                                !lot.expiresOn.isAfter(LocalDate.now())
                                ? R.color.warning : R.color.muted, false);
                        if (!lot.quarantined && lot.quantity > 0) {
                            button(lotItemsContainer, "Cách ly lô " + lot.code, false, () -> {
                                if (!s.canAdmin() || !s.quarantineLot(lot.code)) {
                                    host.message("Không đủ quyền hoặc lô không thể cách ly");
                                } else {
                                    host.message("Đã cách ly lô hàng; hệ thống đã cập nhật tồn khả dụng");
                                    host.navigate("lots-v2");
                                }
                            });
                        } else if (lot.quarantined && lot.quantity > 0) {
                            button(lotItemsContainer, "Bỏ cách ly lô " + lot.code, false, () -> {
                                if (restoreDemoLot(s, lot)) {
                                    host.message("Đã mở lại lô hàng hợp lệ");
                                    host.navigate("lots-v2");
                                } else host.message("Lô không đủ điều kiện mở lại: vui lòng kiểm tra quyền, hạn dùng hoặc số lượng tồn");
                            });
                        }
                        divider(lotItemsContainer);
                    }
                }

                if (totalPages > 1) {
                    PharmaUi.pagination(lotItemsContainer, lotPage[0], totalPages, total, "lô", newPage -> {
                        lotPage[0] = newPage;
                        this.run();
                        if (screen.scroll != null) {
                            screen.scroll.smoothScrollTo(0, existing.getTop());
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
        warning(existing, "Quản lý tồn kho theo tiêu chuẩn Thực hành tốt phân phối thuốc (GDP) và ưu tiên xuất lô cận hạn trước.");
        return screen.root;
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
