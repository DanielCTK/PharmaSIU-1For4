package com.pharmasiu.mobile.navigation;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;

import java.util.function.Consumer;

/**
 * Only one place decides the role-aware bottom navigation presentation.
 * Screen-specific routing and permission enforcement remain in MainActivity/DemoState.
 */
public final class PharmaBottomBar {
    private PharmaBottomBar() { }

    private static final String[] CUSTOMER = {"home", "catalog", "cart", "orders", "account"};
    private static final String[] CUSTOMER_LABELS = {"Trang chủ", "Khám phá", "Giỏ hàng", "Yêu cầu", "Cá nhân"};
    private static final int[] CUSTOMER_ICONS = {
        R.drawable.nav_home, R.drawable.nav_catalog, R.drawable.nav_cart,
        R.drawable.nav_orders, R.drawable.nav_account
    };

    private static final String[] ADMIN = {"admin", "pos", "catalog-admin", "lots", "audit"};
    private static final String[] ADMIN_LABELS = {"Tổng quan", "Quầy POS", "Sản phẩm", "Lô hàng", "Đối soát"};
    private static final int[] ADMIN_ICONS = {
        R.drawable.nav_admin, R.drawable.nav_pos, R.drawable.nav_catalog,
        R.drawable.nav_lots, R.drawable.nav_audit
    };

    private static final String[] CASHIER = {"pos", "logout"};
    private static final String[] CASHIER_LABELS = {"Quầy bán", "Đăng xuất"};
    private static final int[] CASHIER_ICONS = {R.drawable.nav_pos, R.drawable.nav_logout};

    public static String activeTab(String role, String route) {
        if (DemoState.CUSTOMER.equals(role)) {
            if ("catalog".equals(route) || "catalog-v2".equals(route)
                || route.startsWith("product")) return "catalog";
            if (route.startsWith("cart") || route.startsWith("rx")
                || route.startsWith("checkout") || "prescription".equals(route)) return "cart";
            if (route.startsWith("orders") || route.startsWith("order-detail")
                || route.startsWith("receipt")) return "orders";
            if ("account".equals(route) || "favorites".equals(route)
                || "profile".equals(route) || "edit-profile".equals(route)
                || "addresses".equals(route) || "address-book".equals(route)
                || "change-password".equals(route)) return "account";
            return "home";
        }
        if (DemoState.CASHIER.equals(role)) return "pos";
        if (DemoState.ADMIN.equals(role)) {
            if ("pos".equals(route)) return "pos";
            if (route.startsWith("catalog-admin")) return "catalog-admin";
            if (route.startsWith("lots")) return "lots";
            if (route.startsWith("audit")) return "audit";
            return "admin";
        }
        return "";
    }

    public static void bind(Activity activity, LinearLayout container, String role,
                            String route, Consumer<String> onNavigate) {
        container.removeAllViews();
        if (DemoState.NONE.equals(role)) {
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);

        String[] routes = CUSTOMER;
        String[] labels = CUSTOMER_LABELS;
        int[] icons = CUSTOMER_ICONS;
        if (DemoState.ADMIN.equals(role)) {
            routes = ADMIN;
            labels = ADMIN_LABELS;
            icons = ADMIN_ICONS;
        } else if (DemoState.CASHIER.equals(role)) {
            routes = CASHIER;
            labels = CASHIER_LABELS;
            icons = CASHIER_ICONS;
        }
        BottomNavigationView bar = new BottomNavigationView(activity);
        bar.setBackgroundColor(activity.getColor(R.color.white));
        bar.setElevation(dp(activity, 10));
        bar.setItemHorizontalTranslationEnabled(false);
        bar.setLabelVisibilityMode(1); // All tabs labeled, including five-item nav.
        bar.setItemIconSize(dp(activity, 22));
        ColorStateList itemColors = new ColorStateList(
            new int[][]{{android.R.attr.state_checked}, {}},
            new int[]{activity.getColor(R.color.emerald), activity.getColor(R.color.muted)});
        bar.setItemIconTintList(itemColors);
        bar.setItemTextColor(itemColors);
        bar.setItemActiveIndicatorEnabled(true);
        bar.setItemActiveIndicatorColor(ColorStateList.valueOf(activity.getColor(R.color.mint)));
        Menu menu = bar.getMenu();
        String selected = activeTab(role, route);
        int selectedId = 0;
        for (int i = 0; i < routes.length; i++) {
            int itemId = 100 + i;
            menu.add(Menu.NONE, itemId, i, labels[i]).setIcon(icons[i]);
            if (routes[i].equals(selected)) selectedId = itemId;
        }
        if (selectedId != 0) bar.setSelectedItemId(selectedId);
        final String[] routeTargets = routes;
        bar.setOnItemSelectedListener(item -> {
            int index = item.getItemId() - 100;
            if (index < 0 || index >= routeTargets.length) return false;
            onNavigate.accept(routeTargets[index]);
            return true;
        });
        container.addView(bar, new LinearLayout.LayoutParams(-1, -1));
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
