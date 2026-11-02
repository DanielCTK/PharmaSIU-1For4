package com.pharmasiu.mobile.ui.customer.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Dedicated account feature controller. */
public abstract class AccountHome extends AccountUiBase {
    protected AccountHome(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}
    protected void account() {
        hero("PHARMASIU  /  KHÔNG GIAN KHÁCH HÀNG", profile.displayName,
            profile.phone + "  •  Thành viên thân thiết", false);
        title("Tổng quan tài khoản", "Đơn hàng và sản phẩm quan tâm.");
        LinearLayout stats = stack();
        stats.setOrientation(LinearLayout.HORIZONTAL);
        body.addView(stats, params(-1, -2, 13));
        stat(stats, String.format(Locale.ROOT, "%02d", state.favorites.size()), "Đã yêu thích");
        stat(stats, String.format(Locale.ROOT, "%02d", state.customerOrders().size()), "Đơn hàng");
        title("Hồ sơ của bạn", "Quản lý thông tin cá nhân và liên hệ.");
        tile("Chỉnh sửa hồ sơ", "Tên, điện thoại, email, ghi chú ngày sinh", "profile");
        tile("Sổ địa chỉ", "Địa chỉ giao nhận thuốc và mua sắm", "addresses");
        tile("Đổi mật khẩu", "Cập nhật mật khẩu bảo vệ tài khoản", "change-password");
        title("Hoạt động", "Xem lại các thao tác tại máy.");
        tile("Sản phẩm yêu thích", "Danh sách sản phẩm đã lưu", "favorites");
        tile("Yêu cầu đã tạo", "Xem và hủy yêu cầu cục bộ", "orders");
        title("Quyền và khu vực sử dụng", "Mọi quyền nhân viên cần đăng nhập tài khoản tương ứng.");
        LinearLayout gate = card();
        text(gate, "Khu vận hành nhà thuốc", 17, R.color.forest, true, 0);
        text(gate, "Để vào POS hoặc quản trị, bạn phải quay về đăng nhập chung và xác thực bằng tài khoản được cấp quyền.",
            13, R.color.muted, false, 6);
        action(gate, "Đăng nhập bằng tài khoản nhân viên", false, host::logout);
        action(body, "Đăng xuất", true, host::logout);
        disclaimer();
    }

    protected void stat(LinearLayout row, String figure, String caption) {
        LinearLayout tile = stack();
        tile.setBackground(activity.getDrawable(R.drawable.bg_panel));
        tile.setPadding(dp(16), dp(16), dp(12), dp(16));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1f);
        p.setMargins(dp(3), 0, dp(3), 0);
        row.addView(tile, p);
        text(tile, figure, 26, R.color.emerald, true, 0);
        text(tile, caption, 12, R.color.muted, false, 7);
    }
}
