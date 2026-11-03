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

/** Shared screen scaffolding and consistent Material UI. */
public final class AdminUi {
    static final String[] NON_RX_CATEGORIES = {
        "Vật tư y tế", "Thiết bị", "Chăm sóc", "Mỹ phẩm", "Thực phẩm",
        "Thực phẩm bổ sung"
    };
    static final long MAX_PRICE = 100_000_000L;
    static final int MAX_RECEIVE = 999;
    static final String PHOTO_PICKER_TAG = "pharmasiu-demo-staff-photo-picker";
    private AdminUi(){}

    static final class Screen {
        final Activity activity;
        final View root;
        final LinearLayout body;
        final android.widget.ScrollView scroll;
        final View scrollTop;
        Screen(Activity a, int layout) {
            this(a, layout, null);
        }

        Screen(Activity a, int layout, UiHost host) {
            activity = a;
            root = a.getLayoutInflater().inflate(layout, null, false);
            body = root.findViewById(R.id.staff_admin_content);
            if (body == null) throw new IllegalStateException("Missing staff_admin_content");
            scroll = root.findViewById(R.id.staff_admin_scroll);
            scrollTop = root.findViewById(R.id.staff_admin_scroll_top);
            if (scroll != null && scrollTop != null) {
                scrollTop.setOnClickListener(v -> scroll.smoothScrollTo(0, 0));
                scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    scrollTop.setVisibility(scrollY > 280 ? View.VISIBLE : View.GONE);
                });
            }
            if (host != null) {
                View logoutBtn = root.findViewById(R.id.staff_admin_btn_logout);
                if (logoutBtn != null) {
                    logoutBtn.setOnClickListener(v -> host.logout());
                }
                View backBtn = root.findViewById(R.id.staff_admin_btn_back);
                if (backBtn != null) {
                    backBtn.setOnClickListener(v -> host.navigate("admin"));
                }
            }
        }
    }

    public static View access(Activity activity, String title, String detail) {
        View view = activity.getLayoutInflater().inflate(R.layout.staff_admin_access, null, false);
        ((TextView) view.findViewById(R.id.staff_admin_access_title)).setText(title);
        ((TextView) view.findViewById(R.id.staff_admin_access_detail)).setText(detail);
        return view;
    }

    static LinearLayout card(Screen screen) {
        return PharmaUi.surface(screen.body, false);
    }

    static void title(LinearLayout parent, String value) {
        PharmaUi.sectionTitle(parent, value);
    }

    static TextView line(LinearLayout parent, String value, int sp, int color, boolean bold) {
        TextView t = new TextView(parent.getContext());
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(parent.getContext().getColor(color));
        t.setPadding(0, dp(parent.getContext(), 4), 0, dp(parent.getContext(), 4));
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setLineSpacing(dp(parent.getContext(), 2), 1f);
        parent.addView(t, new LinearLayout.LayoutParams(-1, -2));
        return t;
    }

    static void warning(LinearLayout parent, String value) {
        PharmaUi.warning(parent, value);
    }

    static void divider(LinearLayout parent) {
        View border = new View(parent.getContext());
        border.setBackgroundColor(parent.getContext().getColor(R.color.line));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(parent.getContext(), 1));
        p.setMargins(0, dp(parent.getContext(), 9), 0, dp(parent.getContext(), 9));
        parent.addView(border, p);
    }

    static Button button(LinearLayout parent, String title, boolean primary, Runnable action) {
        return PharmaUi.button(parent, title, primary, action);
    }

    static EditText input(LinearLayout parent, String hint, int type, String initial) {
        return PharmaUi.input(parent, hint, type, initial);
    }

    static Spinner picker(LinearLayout parent, String[] values, Activity activity) {
        LinearLayout frame = new LinearLayout(activity);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setBackground(activity.getDrawable(R.drawable.component_input_outline));
        frame.setPadding(dp(activity, 4), dp(activity, 2), dp(activity, 4), dp(activity, 2));

        Spinner spinner = new Spinner(activity);
        spinner.setAdapter(PharmaUi.createSpinnerAdapter(activity, values));
        frame.addView(spinner, new LinearLayout.LayoutParams(-1, dp(activity, 48)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(activity, 6);
        lp.bottomMargin = dp(activity, 8);
        parent.addView(frame, lp);
        return spinner;
    }

    static String[] productLabels(DemoState s) {
        List<String> labels = new ArrayList<>();
        for (DemoState.Product p : s.products) {
            labels.add(p.name + (p.requiresPrescription ? " · Rx KHÓA" : "") +
                " · tồn " + s.stock.getOrDefault(p.id, 0));
        }
        return labels.toArray(new String[0]);
    }

    static int dp(android.content.Context context, int value) {
        return Math.round(context.getResources().getDisplayMetrics().density * value);
    }

    static String vnd(long amount) {
        return String.format(new Locale("vi", "VN"), "%,d ₫", amount);
    }

    static Long positiveLong(String input, long max) {
        Long result = positiveOrZero(input);
        return result != null && result > 0 && result <= max ? result : null;
    }

    static Long positiveOrZero(String input) {
        try {
            long value = Long.parseLong(input.trim());
            return value >= 0 ? value : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static Integer boundedInt(String input, int low, int high) {
        try {
            int value = Integer.parseInt(input.trim());
            return value >= low && value <= high ? value : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException | NullPointerException ignored) {
            return null;
        }
    }
}
