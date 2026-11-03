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
public final class AdminReports {
    private AdminReports() {}

    public static View reports(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_reports, host);
        LinearLayout kpi = card(screen);
        title(kpi, "Báo cáo hoạt động ca trực");
        long gross = 0;
        int blocked = 0, low = 0, rx = 0;
        for (DemoState.Sale sale : s.sales) gross += sale.amountVnd;
        for (DemoState.Lot lot : s.lots)
            if (lot.quarantined || lot.expiresOn == null ||
                !lot.expiresOn.isAfter(LocalDate.now())) blocked++;
        for (DemoState.Product p : s.products) {
            if (p.requiresPrescription) rx++;
            if (s.stock.getOrDefault(p.id, 0) <= 4) low++;
        }
        line(kpi, vnd(gross), 23, R.color.emerald, true);
        line(kpi, "Tổng doanh thu bán hàng tại quầy thuốc", 12, R.color.muted, false);
        divider(kpi);
        line(kpi, "Số hóa đơn đã thanh toán: " + s.sales.size(), 14, R.color.ink, true);
        line(kpi, "Tiền mặt thu tại quầy: " + vnd(s.cashCollectedVnd), 13, R.color.muted, false);
        line(kpi, "Mặt hàng / tồn thấp: " + s.products.size() + " / " + low, 13, R.color.muted, false);
        line(kpi, "Tổng số lô / Hết hạn hoặc cách ly: " + s.lots.size() + " / " + blocked, 13, R.color.muted, false);
        line(kpi, "Hàng Rx bị khóa: " + rx, 13, R.color.muted, false);
        line(kpi, "Biên bản kiểm kê: " + s.inventoryChecks.size(), 13, R.color.muted, false);
        line(kpi, "Biên bản đối soát: " + s.shiftReconciliations.size(), 13, R.color.muted, false);
        line(kpi, "Đơn đặt hàng trực tuyến: " + s.orders.size(), 13, R.color.ink, true);
        warning(kpi, "Báo cáo phục vụ theo dõi hoạt động kinh doanh và kiểm soát chất lượng tại quầy thuốc.");

        LinearLayout actions = card(screen);
        title(actions, "Đi tới phần liên quan");
        button(actions, "Kiểm kê và đối soát ca trực", false, () -> host.navigate("audit-v2"));
        button(actions, "Quản lý lô & cách ly", false, () -> host.navigate("lots-v2"));
        return screen.root;
    }
}
