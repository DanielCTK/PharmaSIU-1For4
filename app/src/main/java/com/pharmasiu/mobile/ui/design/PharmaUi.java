package com.pharmasiu.mobile.ui.design;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pharmasiu.mobile.R;

/**
 * Shared and team-friendly Material 3 view toolkit for a Java/XML application.
 * Business logic must never live here; screens own callbacks and data.
 */
public final class PharmaUi {
    private PharmaUi() { }

    public static int dp(Context context, float value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }

    public static int color(Context context, int id) { return context.getColor(id); }

    /** Adds a Material card wrapper and returns its vertical inner content container. */
    public static LinearLayout surface(LinearLayout parent, boolean highlight) {
        Context context = parent.getContext();
        MaterialCardView card = new MaterialCardView(context);
        card.setRadius(dp(context, 14));
        card.setCardBackgroundColor(color(context, highlight ? R.color.mint : R.color.white));
        card.setStrokeColor(color(context, highlight ? R.color.lime : R.color.line));
        card.setStrokeWidth(dp(context, 1));
        card.setCardElevation(dp(context, 2));
        card.setUseCompatPadding(false);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.bottomMargin = dp(context, 8);
        parent.addView(card, cardParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(context, 18),dp(context,18),dp(context,18),dp(context,18));
        card.addView(content,new ViewGroup.LayoutParams(-1,-2));
        return content;
    }

    /** Primary and secondary actions use the same height, shape and pressed feedback. */
    public static Button button(LinearLayout parent, String label, boolean primary, Runnable action) {
        Context context = parent.getContext();
        MaterialButton button = new MaterialButton(context);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setCornerRadius(dp(context, 14));
        button.setMinHeight(dp(context, 52));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setBackgroundTintList(ColorStateList.valueOf(
            color(context,primary ? R.color.emerald : R.color.mint)));
        button.setTextColor(color(context,primary ? R.color.white : R.color.forest));
        button.setStrokeColor(ColorStateList.valueOf(
            color(context, primary ? R.color.emerald : R.color.line)));
        button.setStrokeWidth(dp(context, primary ? 0 : 1));
        button.setContentDescription(label);
        button.setOnClickListener(v -> {
            if (action != null && button.isEnabled()) action.run();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(context, 52));
        lp.topMargin = dp(context, 8);
        parent.addView(button,lp);
        return button;
    }

    public static EditText input(LinearLayout parent, String label, int inputType, String value) {
        Context context = parent.getContext();
        TextInputLayout frame = new TextInputLayout(
            context,null,com.google.android.material.R.attr.textInputOutlinedStyle);
        frame.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        frame.setBoxCornerRadii(dp(context, 12), dp(context, 12), dp(context, 12), dp(context, 12));
        frame.setHint(label);
        frame.setBoxStrokeColor(color(context,R.color.emerald));
        TextInputEditText text = new TextInputEditText(frame.getContext());
        text.setTextSize(14);
        text.setSingleLine(true);
        text.setInputType(inputType);
        text.setText(value == null ? "" : value);
        text.setPadding(dp(context,12),dp(context,8),dp(context,12),dp(context,8));
        frame.addView(text,new LinearLayout.LayoutParams(-1,dp(context,54)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(context,10);
        parent.addView(frame,lp);
        return text;
    }

    public static TextView sectionTitle(LinearLayout parent,String value) {
        Context context=parent.getContext();
        TextView t=new TextView(context);
        t.setText(value);
        t.setTextSize(19);
        t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setTextColor(color(context,R.color.forest));
        t.setLetterSpacing(-0.015f);
        t.setPadding(0,0,0,dp(context,10));
        parent.addView(t,new LinearLayout.LayoutParams(-1,-2));
        return t;
    }

    public static TextView label(LinearLayout parent,String text,int sp,int colorId,boolean strong){
        Context context=parent.getContext();
        TextView view=new TextView(context);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color(context,colorId));
        view.setLineSpacing(dp(context,3),1f);
        view.setPadding(0,dp(context,3),0,dp(context,3));
        if(strong)view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        parent.addView(view,new LinearLayout.LayoutParams(-1,-2));
        return view;
    }

    public static View warning(LinearLayout parent,String text){
        Context context=parent.getContext();
        TextView caption = new TextView(context);
        caption.setText(text);
        caption.setTextColor(color(context,R.color.warning));
        caption.setTextSize(12);
        caption.setLineSpacing(dp(context,3),1f);
        caption.setPadding(dp(context,14),dp(context,12),dp(context,14),dp(context,12));
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(color(context,R.color.warning_surface));
        panel.setCornerRadius(dp(context, 10));
        panel.setStroke(dp(context,1),0xFFEECFAF);
        caption.setBackground(panel);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin = dp(context,12);
        parent.addView(caption,lp);
        return caption;
    }

    /** An explicit empty state with meaningful text, not an invisible blank screen. */
    public static View empty(LinearLayout parent,String title,String detail) {
        LinearLayout box=surface(parent,false);
        box.setGravity(Gravity.CENTER);
        TextView headline=sectionTitle(box,title);
        headline.setGravity(Gravity.CENTER);
        TextView explanation=label(box,detail,13,R.color.muted,false);
        explanation.setGravity(Gravity.CENTER);
        return box;
    }

    public interface PageCallback {
        void onPage(int page);
    }

    /** Clean pagination bar with previous/next controls and page position. */
    public static View pagination(LinearLayout parent, int currentPage, int totalPages, int totalItems, String itemNoun, PageCallback callback) {
        if (totalPages <= 1) return null;
        Context context = parent.getContext();
        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(0, dp(context, 12), 0, dp(context, 8));

        MaterialButton prev = new MaterialButton(context);
        prev.setText("◀ Trước");
        prev.setAllCaps(false);
        prev.setTextSize(13);
        prev.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        prev.setCornerRadius(dp(context, 10));
        prev.setMinHeight(dp(context, 42));
        prev.setInsetTop(0);
        prev.setInsetBottom(0);
        prev.setEnabled(currentPage > 0);
        prev.setBackgroundTintList(ColorStateList.valueOf(color(context, currentPage > 0 ? R.color.mint : R.color.cream)));
        prev.setTextColor(color(context, currentPage > 0 ? R.color.forest : R.color.muted));
        prev.setOnClickListener(v -> {
            if (currentPage > 0 && callback != null) callback.onPage(currentPage - 1);
        });

        TextView indicator = new TextView(context);
        String label = (itemNoun != null && !itemNoun.isEmpty())
            ? "Trang " + (currentPage + 1) + " / " + totalPages + "\n(" + totalItems + " " + itemNoun + ")"
            : "Trang " + (currentPage + 1) + " / " + totalPages;
        indicator.setText(label);
        indicator.setTextSize(12);
        indicator.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        indicator.setTextColor(color(context, R.color.forest));
        indicator.setGravity(Gravity.CENTER);
        indicator.setLineSpacing(dp(context, 2), 1f);

        MaterialButton next = new MaterialButton(context);
        next.setText("Sau ▶");
        next.setAllCaps(false);
        next.setTextSize(13);
        next.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        next.setCornerRadius(dp(context, 10));
        next.setMinHeight(dp(context, 42));
        next.setInsetTop(0);
        next.setInsetBottom(0);
        next.setEnabled(currentPage < totalPages - 1);
        next.setBackgroundTintList(ColorStateList.valueOf(color(context, currentPage < totalPages - 1 ? R.color.mint : R.color.cream)));
        next.setTextColor(color(context, currentPage < totalPages - 1 ? R.color.forest : R.color.muted));
        next.setOnClickListener(v -> {
            if (currentPage < totalPages - 1 && callback != null) callback.onPage(currentPage + 1);
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, dp(context, 42), 1f);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, -2, 1.4f);

        bar.addView(prev, btnParams);
        bar.addView(indicator, textParams);
        bar.addView(next, btnParams);

        parent.addView(bar, new LinearLayout.LayoutParams(-1, -2));
        return bar;
    }
}
