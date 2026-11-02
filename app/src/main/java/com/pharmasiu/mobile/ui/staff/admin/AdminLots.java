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
        Screen screen = new Screen(a, R.layout.staff_admin_lots);
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
                host.message("Đã ghi nhận lô trong RAM, chưa có phiếu nhập thật");
                host.navigate("lots-v2");
            }
        });
        warning(receiving, "Quyền Quản lý: Tổng tồn tối đa 999 đơn vị/mặt hàng; lô cách ly và quá hạn sẽ không xuất bán theo nguyên tắc FEFO.");

        LinearLayout existing = card(screen);
        title(existing, "Danh sách lô và hạn");
        if (s.lots.isEmpty()) line(existing, "Chưa có lô theo hạn", 13, R.color.muted, false);
        for (DemoState.Lot lot : new ArrayList<>(s.lots)) {
            DemoState.Product p = s.product(lot.productId);
            String status = lot.quarantined ? "CÁCH LY" :
                lot.expiresOn == null ? "CHƯA XÁC MINH HẠN" :
                !lot.expiresOn.isAfter(LocalDate.now()) ? "HẾT HẠN" :
                lot.quantity < 1 ? "HẾT SỐ LƯỢNG" : "ĐỦ ĐIỀU KIỆN XUẤT FEFO";
            line(existing, lot.code + " · " + (p == null ? lot.productId : p.name), 15, R.color.ink, true);
            line(existing, "Hạn " + lot.expiresOn + " · " + lot.quantity + " đơn vị · " + status,
                12, lot.quarantined || lot.expiresOn == null ||
                    !lot.expiresOn.isAfter(LocalDate.now())
                    ? R.color.warning : R.color.muted, false);
            if (!lot.quarantined && lot.quantity > 0) {
                button(existing, "Cách ly lô " + lot.code, false, () -> {
                    if (!s.canAdmin() || !s.quarantineLot(lot.code)) {
                        host.message("Không đủ quyền hoặc lô không thể cách ly");
                    } else {
                        host.message("Đã cách ly lô hàng; hệ thống đã cập nhật tồn khả dụng");
                        host.navigate("lots-v2");
                    }
                });
            } else if (lot.quarantined && lot.quantity > 0) {
                button(existing, "Bỏ cách ly lô " + lot.code, false, () -> {
                    if (restoreDemoLot(s, lot)) {
                        host.message("Đã mở lại lô hàng hợp lệ");
                        host.navigate("lots-v2");
                    } else host.message("Lô không đủ điều kiện bỏ cách ly: quyền, hạn hoặc tổng tồn");
                });
            }
            divider(existing);
        }
        warning(existing, "Quản lý tồn kho theo tiêu chuẩn Thực hành tốt phân phối thuốc (GDP) và nguyên tắc FEFO.");
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
