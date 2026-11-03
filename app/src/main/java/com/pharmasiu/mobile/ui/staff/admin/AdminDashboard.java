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
public final class AdminDashboard {
    private AdminDashboard() {}

    public static View dashboard(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_dashboard, host);
        LinearLayout metrics = card(screen);
        title(metrics, "Tổng quan hoạt động");
        int low = 0, rx = 0, blockedLots = 0;
        for (DemoState.Product product : s.products) {
            if (product.requiresPrescription) rx++;
            if (s.stock.getOrDefault(product.id, 0) <= 4) low++;
        }
        for (DemoState.Lot lot : s.lots) {
            if (lot.quarantined || lot.expiresOn == null ||
                !lot.expiresOn.isAfter(LocalDate.now())) blockedLots++;
        }
        line(metrics, s.products.size() + " sản phẩm quản lý   •   " + low + " mặt hàng tồn thấp", 15, R.color.emerald, true);
        line(metrics, s.lots.size() + " lô hàng quản lý   •   " + blockedLots + " lô cách ly/quá hạn", 14, R.color.ink, false);
        line(metrics, rx + " mặt hàng Rx chưa được phép bán hoặc duyệt", 13, R.color.warning, true);
        line(metrics, s.sales.size() + " hóa đơn bán lẻ   •   " + s.orders.size() + " đơn đặt hàng", 13, R.color.muted, false);
        line(metrics, s.shiftOpen ? "● Ca bán hàng #" + s.shiftNumber + " đang mở"
            : "● Chưa mở ca bán hàng", 14, R.color.emerald, true);
        warning(metrics, "Hệ thống quản lý PharmaSIU: Kiểm soát tồn kho, lô hạn sử dụng và doanh thu ca trực theo tiêu chuẩn GPP.");

        LinearLayout actions = card(screen);
        title(actions, "Quản lý nghiệp vụ");
        button(actions, "Quản lý sản phẩm & giá niêm yết", true,
            () -> host.navigate("catalog-admin-v2"));
        button(actions, "Quản lý lô & hạn sử dụng", false,
            () -> host.navigate("lots-v2"));
        button(actions, "Kiểm kê kho & đối soát ca", false,
            () -> host.navigate("audit-v2"));
        button(actions, "Đơn đặt hàng của khách", false,
            () -> host.navigate("requests-admin-v2"));
        button(actions, "Báo cáo KPI trong phiên", false,
            () -> host.navigate("reports-v2"));
        button(actions, "Mở quầy POS bán lẻ", false, () -> host.navigate("pos"));

        LinearLayout pending = card(screen);
        title(pending, "Tiếp nhận đơn hàng trực tuyến");
        line(pending, "SẴN SÀNG TIẾP NHẬN", 14, R.color.emerald, true);
        line(pending, "Hệ thống tiếp nhận và xử lý đơn đặt thuốc trực tuyến nhận tại nhà thuốc từ khách hàng.", 13, R.color.muted, false);

        LinearLayout accountCard = card(screen);
        title(accountCard, "Tài khoản Quản lý & Phiên làm việc");
        line(accountCard, "Tài khoản: Quản trị viên hệ thống (0902222222)", 15, R.color.ink, true);
        line(accountCard, "Phiên làm việc quản trị đang hoạt động an toàn theo tiêu chuẩn GPP.", 13, R.color.muted, false);
        button(accountCard, "🚪 Đăng xuất tài khoản Quản lý", false, host::logout);

        return screen.root;
    }
}
