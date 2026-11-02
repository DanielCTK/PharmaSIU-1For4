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
import com.pharmasiu.mobile.ui.design.PharmaUi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Shared customer account Java Views and layout helpers. */
public abstract class AccountUiBase  {

    protected final Activity activity;
    protected final UiHost host;
    protected final DemoState state;
    protected final DemoState.CustomerProfile profile;
    protected final AccountOnboardingProgress onboardingProgress;
    protected final String route;
    protected LinearLayout header, body;
    protected ScrollView scroll;

    AccountUiBase(Activity activity, UiHost host, DemoState.CustomerProfile profile,
           AccountOnboardingProgress onboardingProgress, String route) {
        this.activity = activity;
        this.host = host;
        this.state = host.state();
        this.profile = profile;
        this.onboardingProgress = onboardingProgress;
        this.route = route;
    }
    protected abstract void draw();
    protected int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    protected int color(int id) { return activity.getColor(id); }

    protected LinearLayout.LayoutParams params(int width, int height, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.topMargin = dp(top);
        return p;
    }

    protected TextView label(String text, int sp, int color, boolean bold) {
        TextView v = new TextView(activity);
        v.setText(text);
        v.setTextSize(sp);
        v.setTextColor(color(color));
        v.setLineSpacing(dp(3), 1f);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    protected void text(LinearLayout parent, String content, int sp, int tint, boolean bold, int top) {
        parent.addView(label(content, sp, tint, bold), params(-1, -2, top));
    }

    protected LinearLayout stack() {
        LinearLayout l = new LinearLayout(activity);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    protected LinearLayout card() {
        LinearLayout card = (LinearLayout) LayoutInflater.from(activity)
            .inflate(R.layout.customer_account_card, body, false);
        body.addView(card, params(-1, -2, 12));
        return card;
    }

    protected void hero(String overline, String heading, String subheading, boolean back) {
        if (back) {
            Button previous = new Button(activity);
            previous.setAllCaps(false);
            previous.setText("‹  Quay lại tài khoản");
            previous.setTextColor(color(R.color.lime));
            previous.setBackgroundTintList(ColorStateList.valueOf(color(R.color.forest)));
            previous.setOnClickListener(v -> host.navigate("account"));
            header.addView(previous, params(-1, dp(48), 0));
        }
        text(header, overline, 11, R.color.lime, true, back ? 14 : 0);
        text(header, heading, 28, R.color.white, true, 10);
        text(header, subheading, 14, R.color.mint, false, 8);
    }

    protected void title(String title, String caption) {
        text(body, title, 20, R.color.forest, true, 22);
        if (caption != null && !caption.isEmpty()) {
            text(body, caption, 13, R.color.muted, false, 5);
        }
    }

    protected Button action(LinearLayout target, String title, boolean primary, Runnable click) {
        return PharmaUi.button(target,title,primary,click);
    }

    protected void tile(String title, String detail, String destination) {
        LinearLayout c = card();
        text(c, title + "   ›", 16, R.color.ink, true, 0);
        text(c, detail, 12, R.color.muted, false, 5);
        c.setClickable(true);
        c.setFocusable(true);
        c.setContentDescription(title + ". " + detail);
        c.setOnClickListener(v -> host.navigate(destination));
        action(c, "Mở " + title.toLowerCase(new Locale("vi", "VN")), false,
            () -> host.navigate(destination));
    }

    protected void disclaimer() {
        text(body, "PharmaSIU • Hệ thống phân phối Dược phẩm & Chăm sóc sức khỏe chính hãng.",
            12, R.color.muted, false, 20);
    }
}
