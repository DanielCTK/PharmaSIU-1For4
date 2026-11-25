package com.pharmasiu.mobile.ui.customer.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.design.UiMotion;

import java.util.Locale;

/** Compact personal dashboard with grouped, icon-led account actions. */
public abstract class AccountHome extends AccountUiBase {
    protected AccountHome(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}

    protected void account() {
        profileCard();
        LinearLayout stats = new LinearLayout(activity);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        body.addView(stats,params(-1,-2,4));
        stat(stats,state.favorites.size(),"Yêu thích",R.drawable.customer_account_heart,"favorites",true);
        stat(stats,state.customerOrders().size(),"Yêu cầu",R.drawable.customer_account_orders,"orders",false);

        section("Tài khoản");
        LinearLayout account = menuGroup();
        menuRow(account,R.drawable.customer_account_profile,"Thông tin cá nhân",null,
            () -> host.navigate("profile"));
        menuRow(account,R.drawable.customer_account_location,"Sổ địa chỉ",null,
            () -> host.navigate("addresses"));
        menuRow(account,R.drawable.customer_account_lock,"Đổi mật khẩu",null,
            () -> host.navigate("change-password"));

        section("Hoạt động");
        LinearLayout activityGroup = menuGroup();
        menuRow(activityGroup,R.drawable.customer_account_heart,"Sản phẩm yêu thích",null,
            () -> host.navigate("favorites"));
        menuRow(activityGroup,R.drawable.customer_account_orders,"Yêu cầu đã tạo",null,
            () -> host.navigate("orders"));

        section("Phiên đăng nhập");
        LinearLayout session = menuGroup();
        menuRow(session,R.drawable.customer_account_staff,"Đăng nhập nhân viên",null,
            () -> confirmSignOut(true));
        menuRow(session,R.drawable.customer_account_logout,"Đăng xuất",null,
            () -> confirmSignOut(false));
        disclaimer();
    }

    private void profileCard() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(row,params(-1,-2,0));

        View avatar = CustomerAvatarStore.createAvatar(activity,state,profile.displayName,64,false,null);
        row.addView(avatar,new LinearLayout.LayoutParams(dp(64),dp(64)));

        LinearLayout identity = stack();
        LinearLayout.LayoutParams identityParams = new LinearLayout.LayoutParams(0,-2,1);
        identityParams.setMarginStart(dp(14));
        row.addView(identity,identityParams);
        TextView name = label(profile.displayName,22,R.color.forest,true);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        androidx.core.view.ViewCompat.setAccessibilityHeading(name,true);
        identity.addView(name,params(-1,-2,0));
        if (profile.phone != null && !profile.phone.isEmpty())
            text(identity,profile.phone,14,R.color.muted,false,4);
        TextView role = label("Khách hàng",12,R.color.forest,true);
        role.setBackground(activity.getDrawable(R.drawable.customer_account_icon_surface));
        role.setPadding(dp(9),dp(4),dp(9),dp(4));
        identity.addView(role,params(-2,-2,8));
    }

    private void stat(LinearLayout parent,int count,String caption,int icon,String destination,boolean first) {
        LinearLayout tile = stack();
        tile.setBackground(activity.getDrawable(first ? R.drawable.customer_account_stat : R.drawable.customer_account_stat_orders));
        tile.setPadding(dp(14),dp(12),dp(14),dp(12));
        tile.setMinimumHeight(dp(84));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,-2,1);
        if (first) p.setMarginEnd(dp(5)); else p.setMarginStart(dp(5));
        parent.addView(tile,p);
        LinearLayout top = new LinearLayout(activity);
        top.setGravity(Gravity.CENTER_VERTICAL);
        tile.addView(top,new LinearLayout.LayoutParams(-1,-2));
        TextView figure = label(String.format(Locale.ROOT,"%02d",count),27,R.color.emerald,true);
        figure.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        top.addView(figure,new LinearLayout.LayoutParams(0,-2,1));
        top.addView(icon(icon,R.color.emerald),new LinearLayout.LayoutParams(dp(25),dp(25)));
        TextView title = label(caption,14,R.color.forest,false);
        title.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(title,params(-1,-2,5));
        PharmaUi.pressable(tile);
        UiMotion.press(tile);
        tile.setContentDescription(count + " " + caption + ", mở danh sách");
        tile.setOnClickListener(v -> host.navigate(destination));
    }

    private void confirmSignOut(boolean staff) {
        new AlertDialog.Builder(activity)
            .setTitle(staff ? "Đăng nhập nhân viên?" : "Đăng xuất tài khoản?")
            .setMessage(staff ? "Quay về màn hình đăng nhập để dùng tài khoản nhân viên được cấp quyền."
                : "Bạn sẽ quay về màn hình đăng nhập.")
            .setNegativeButton("Ở lại",null)
            .setPositiveButton(staff ? "Tiếp tục" : "Đăng xuất",(dialog,which) -> host.logout())
            .show();
    }
}
