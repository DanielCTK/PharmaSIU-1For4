package com.pharmasiu.mobile.ui.customer;

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
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.customer.account.AccountPresenter;
import com.pharmasiu.mobile.ui.customer.account.AccountOnboardingProgress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Customer-only Java Views implementation of the six legacy Compose account flows.
 * Profile, addresses and password use DemoState's RAM-only customer account API.
 * Only the onboarding step is view-local to this session. No staff role is granted.
 */
public final class CustomerAccountViews {
    private static final Map<DemoState, AccountOnboardingProgress> ONBOARDING =
        Collections.synchronizedMap(new WeakHashMap<>());

    private CustomerAccountViews() { }

    /** Entry point consumed by the owner of MainActivity/route dispatch. */
    public static View create(Activity activity, UiHost host, String route) {
        if (activity == null || host == null || host.state() == null) {
            throw new IllegalArgumentException("Activity, UiHost and DemoState are required");
        }
        if (!DemoState.CUSTOMER.equals(host.state().role)
                || host.state().customerPhone == null
                || host.state().customerPhone.isEmpty()
                || host.state().currentCustomer() == null) {
            return denied(activity, host);
        }
        return new AccountPresenter(activity, host, host.state().currentCustomer(),
            progressFor(host.state()), normalizedRoute(route)).create();
    }

    /** Allows the host to reset onboarding UI progress; account data stays in DemoState. */
    public static void clearRamAccount(DemoState state) {
        if (state != null) ONBOARDING.remove(state);
    }

    private static AccountOnboardingProgress progressFor(DemoState state) {
        synchronized (ONBOARDING) {
            AccountOnboardingProgress existing = ONBOARDING.get(state);
            if (existing != null) return existing;
            AccountOnboardingProgress next = new AccountOnboardingProgress();
            ONBOARDING.put(state, next);
            return next;
        }
    }

    private static String normalizedRoute(String route) {
        if ("profile".equals(route) || "edit-profile".equals(route)) return "profile";
        if ("address-book".equals(route) || "addresses".equals(route)) return "addresses";
        if ("change-password".equals(route)) return "change-password";
        if ("favorites".equals(route)) return "favorites";
        if ("onboarding".equals(route)) return "onboarding";
        return "account";
    }

    /** Guest account landing: square, edge-to-edge panels, safe text inset. */
    private static View denied(Activity activity, UiHost host) {
        LinearLayout page = new LinearLayout(activity);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(activity.getColor(R.color.cream));
        page.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
        int edge = PharmaUi.dp(activity,18);

        TextView bar = new TextView(activity);
        bar.setText("Tài khoản");
        bar.setTextSize(21);
        bar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.setTextColor(activity.getColor(R.color.forest));
        bar.setPadding(edge,PharmaUi.dp(activity,18),edge,PharmaUi.dp(activity,12));
        page.addView(bar,new LinearLayout.LayoutParams(-1,-2));

        // The section fills the full screen width; the text alone is inset.
        LinearLayout hero = new LinearLayout(activity);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackgroundResource(R.drawable.customer_account_hero);
        hero.setPadding(edge,PharmaUi.dp(activity,22),edge,PharmaUi.dp(activity,22));
        page.addView(hero,new LinearLayout.LayoutParams(-1,-2));

        TextView icon = new TextView(activity);
        icon.setText("◎");
        icon.setTextSize(32);
        icon.setTextColor(activity.getColor(R.color.lime));
        hero.addView(icon);

        TextView heading = new TextView(activity);
        heading.setText("Xin chào, khách ghé thăm!");
        heading.setTextSize(23);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setTextColor(activity.getColor(R.color.white));
        hero.addView(heading);

        TextView detail = new TextView(activity);
        detail.setText("Đăng nhập để quản lý hồ sơ, yêu cầu và lịch sử trên thiết bị.");
        detail.setTextSize(14);
        detail.setTextColor(activity.getColor(R.color.mint));
        LinearLayout.LayoutParams description = new LinearLayout.LayoutParams(-1,-2);
        description.topMargin=PharmaUi.dp(activity,8);
        hero.addView(detail,description);
        PharmaUi.button(hero,"Đăng nhập / Đăng ký",false,host::logout);
        return page;
    }

}
