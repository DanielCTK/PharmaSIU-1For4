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
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.google.android.material.button.MaterialButton;

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
        v.setTextSize(Math.max(13,sp));
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
        LinearLayout toolbar = new LinearLayout(activity);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(toolbar,params(-1,-2,0));
        if (back) {
            MaterialButton previous = new MaterialButton(activity);
            previous.setText("");
            previous.setIconResource(R.drawable.customer_account_back);
            previous.setIconTint(ColorStateList.valueOf(color(R.color.forest)));
            previous.setIconSize(dp(24));
            previous.setIconPadding(0);
            previous.setPadding(dp(12),dp(12),dp(12),dp(12));
            previous.setInsetTop(0); previous.setInsetBottom(0);
            previous.setMinWidth(0); previous.setMinimumWidth(0);
            previous.setMinHeight(dp(48));
            previous.setCornerRadius(dp(14));
            previous.setBackgroundTintList(ColorStateList.valueOf(color(R.color.mint)));
            previous.setContentDescription("Quay lại Cá nhân");
            previous.setOnClickListener(v -> host.navigate("account"));
            UiMotion.press(previous);
            toolbar.addView(previous,new LinearLayout.LayoutParams(dp(48),dp(48)));
        }
        TextView title = label(heading,22,R.color.forest,true);
        androidx.core.view.ViewCompat.setAccessibilityHeading(title,true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0,-2,1);
        if (back) titleParams.setMarginStart(dp(12));
        toolbar.addView(title,titleParams);
        if (subheading != null && !subheading.isEmpty())
            text(header,subheading,14,R.color.muted,false,8);
    }

    protected ImageView icon(int drawable,int tint) {
        ImageView view = new ImageView(activity);
        view.setImageResource(drawable);
        view.setImageTintList(ColorStateList.valueOf(color(tint)));
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return view;
    }

    protected void section(String title) {
        TextView label = label(title,15,R.color.forest,true);
        androidx.core.view.ViewCompat.setAccessibilityHeading(label,true);
        body.addView(label,params(-1,-2,18));
    }

    protected LinearLayout menuGroup() {
        LinearLayout group = stack();
        group.setBackground(activity.getDrawable(R.drawable.customer_account_menu_group));
        group.setClipToOutline(true);
        body.addView(group,params(-1,-2,8));
        return group;
    }

    protected void menuRow(LinearLayout group,int drawable,String title,String detail,Runnable click) {
        if (group.getChildCount()>0) {
            View divider = new View(activity);
            divider.setBackgroundColor(color(R.color.line));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1,dp(1));
            dividerParams.setMarginStart(dp(70)); dividerParams.setMarginEnd(dp(14));
            group.addView(divider,dividerParams);
        }
        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(64));
        row.setPadding(dp(14),dp(10),dp(14),dp(10));
        ImageView leading = icon(drawable,R.color.emerald);
        leading.setBackground(activity.getDrawable(R.drawable.customer_account_icon_surface));
        leading.setPadding(dp(9),dp(9),dp(9),dp(9));
        row.addView(leading,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout copy = stack();
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0,-2,1);
        copyParams.setMarginStart(dp(12)); copyParams.setMarginEnd(dp(8));
        row.addView(copy,copyParams);
        TextView caption = label(title,16,R.color.ink,true);
        caption.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(caption,params(-1,-2,0));
        if (detail != null && !detail.isEmpty()) {
            TextView description = label(detail,13,R.color.muted,false);
            description.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            copy.addView(description,params(-1,-2,4));
        }
        row.addView(icon(R.drawable.customer_account_chevron,R.color.muted),
            new LinearLayout.LayoutParams(dp(20),dp(20)));
        PharmaUi.pressable(row);
        UiMotion.press(row);
        row.setContentDescription(title+(detail==null||detail.isEmpty()?"":". "+detail));
        row.setOnClickListener(v -> click.run());
        group.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }

    protected void title(String title, String caption) {
        text(body, title, 20, R.color.forest, true, 22);
        if (caption != null && !caption.isEmpty()) {
            text(body, caption, 13, R.color.muted, false, 5);
        }
    }

    protected Button action(LinearLayout target, String title, boolean primary, Runnable click) {
        Button button = PharmaUi.button(target,title,primary,click);
        UiMotion.press(button);
        return button;
    }

    protected void tile(String title, String detail, String destination) {
        LinearLayout c = card();
        text(c, title + "   ›", 16, R.color.ink, true, 0);
        text(c, detail, 14, R.color.muted, false, 5);
        PharmaUi.pressable(c);
        UiMotion.press(c);
        c.setContentDescription(title + ". " + detail);
        c.setOnClickListener(v -> host.navigate(destination));
    }

    protected void disclaimer() {
        LinearLayout note = new LinearLayout(activity);
        note.setGravity(Gravity.CENTER_VERTICAL);
        note.addView(icon(R.drawable.customer_account_device,R.color.muted),
            new LinearLayout.LayoutParams(dp(16),dp(16)));
        TextView label=label("Demo · Dữ liệu trên thiết bị",12,R.color.muted,false);
        LinearLayout.LayoutParams textParams=new LinearLayout.LayoutParams(0,-2,1);
        textParams.setMarginStart(dp(7));
        note.addView(label,textParams);
        body.addView(note,params(-1,-2,20));
    }
}
