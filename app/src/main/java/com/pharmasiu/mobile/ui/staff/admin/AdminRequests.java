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
public final class AdminRequests {
    private AdminRequests() {}

    public static View requests(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_requests);
        LinearLayout status = card(screen);
        title(status, "Đơn đặt hàng nhận tại quầy thuốc");
        line(status, "HỆ THỐNG ĐANG HOẠT ĐỘNG", 17, R.color.emerald, true);
        warning(status, "Danh sách đơn đặt hàng từ khách hàng PharmaSIU. Dược sĩ kiểm tra đơn thuốc và chuẩn bị trước khi giao cho khách.");

        LinearLayout entries = card(screen);
        title(entries, "Danh sách đơn đặt hàng");
        if (s.orders.isEmpty()) line(entries, "Chưa có đơn đặt hàng từ khách hàng.", 13, R.color.muted, false);
        for (DemoState.Order order : s.orders) {
            line(entries, order.reference + " · " + order.status, 15, R.color.ink, true);
            line(entries, "Tạm tính " + vnd(order.pricedSubtotalVnd) +
                (order.containsUnpriced ? " · có hàng chưa báo giá" : ""), 12, R.color.muted, false);
            if (order.containsRx) line(entries, "Thuốc kê đơn (Rx) · Cần Dược sĩ kiểm tra đơn thuốc khi giao",
                12, R.color.warning, true);
            line(entries, "Đơn hàng sẵn sàng để soạn thuốc", 12, R.color.emerald, false);
            divider(entries);
        }
        return screen.root;
    }
}
