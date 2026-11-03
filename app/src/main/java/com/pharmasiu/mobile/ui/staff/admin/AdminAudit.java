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
public final class AdminAudit {
    private AdminAudit() {}

    public static View audit(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_audit, host);
        LinearLayout inventory = card(screen);
        title(inventory, "Kiểm kê lệch kho");
        Spinner goods = picker(inventory, productLabels(s), a);
        EditText counted = input(inventory, "Số lượng thực tế (0–999)", InputType.TYPE_CLASS_NUMBER, "");
        EditText reason = input(inventory, "Lý do lệch (>= 3 ký tự)", InputType.TYPE_CLASS_TEXT, "");
        button(inventory, "Lưu số lượng kiểm kê", true, () -> {
            if (!s.canAdmin()) { host.message("Không đủ quyền Quản lý"); return; }
            Integer amount = boundedInt(counted.getText().toString(), 0, MAX_RECEIVE);
            int index = goods.getSelectedItemPosition();
            if (index < 0 || index >= s.products.size() || amount == null ||
                !s.countDemoStock(s.products.get(index).id, amount, reason.getText().toString())) {
                host.message("Số đếm hoặc lý do không hợp lệ");
            } else {
                host.message("Đã lưu biên bản kiểm kê kho thành công");
                host.navigate("audit-v2");
            }
        });
        warning(inventory, "Biên bản kiểm kê ghi nhận số đếm thực tế để đối soát số lượng tồn kho định kỳ.");

        LinearLayout shifts = card(screen);
        title(shifts, "Đối soát ca bán hàng");
        long expected = s.openingCashVnd + s.cashCollectedVnd;
        line(shifts, "Ca #" + s.shiftNumber + (s.shiftOpen ? " · đang mở" : " · đã đóng / chưa mở"),
            14, R.color.ink, true);
        line(shifts, "Tiền đầu ca " + vnd(s.openingCashVnd) +
            " + tiền mặt POS " + vnd(s.cashCollectedVnd),
            12, R.color.muted, false);
        line(shifts, "Tiền mặt kỳ vọng: " + vnd(expected), 17, R.color.emerald, true);
        EditText cash = input(shifts, "Tiền mặt thực tế kết ca (VNĐ)", InputType.TYPE_CLASS_NUMBER, String.valueOf(expected));
        EditText note = input(shifts, "Ghi chú đối soát", InputType.TYPE_CLASS_TEXT, "");
        button(shifts, "Lưu đối soát ca bán hàng", true, () -> {
            Long amount = positiveOrZero(cash.getText().toString());
            if (!s.canAdmin() || s.shiftOpen || s.shiftNumber < 1 || amount == null ||
                !s.reconcileDemoShift(amount, note.getText().toString())) {
                host.message("Chỉ đối soát sau khi đóng ca, với số tiền và ghi chú hợp lệ");
            } else {
                host.message("Đã lưu biên bản đối soát ca bán hàng");
                host.navigate("audit-v2");
            }
        });
        warning(shifts, "Đối soát đối chiếu doanh thu bán hàng tại quầy với số tiền mặt thực tế khi kết thúc ca làm việc.");

        LinearLayout history = card(screen);
        title(history, "Lịch sử kiểm kê và đối soát ca làm việc");
        if (s.inventoryChecks.isEmpty() && s.shiftReconciliations.isEmpty()) {
            line(history, "Chưa có biên bản đối soát nào được ghi nhận", 13, R.color.muted, false);
        }
        for (String entry : s.inventoryChecks) { line(history, entry, 12, R.color.ink, false); divider(history); }
        for (String entry : s.shiftReconciliations) { line(history, entry, 12, R.color.ink, false); divider(history); }
        return screen.root;
    }
}
