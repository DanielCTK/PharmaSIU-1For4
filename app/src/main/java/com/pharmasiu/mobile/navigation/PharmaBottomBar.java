package com.pharmasiu.mobile.navigation;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.ui.design.UiMotion;

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

    private static final String[] ADMIN = {"admin", "pos", "catalog-admin", "lots", "logout"};
    private static final String[] ADMIN_LABELS = {"Tổng quan", "Bán hàng", "Hàng hóa", "Lô hàng", "Đăng xuất"};
    private static final int[] ADMIN_ICONS = {
        R.drawable.nav_admin, R.drawable.nav_pos, R.drawable.nav_catalog,
        R.drawable.nav_lots, R.drawable.nav_logout
    };

    private static final String[] CASHIER = {"pos", "requests", "logout"};
    private static final String[] CASHIER_LABELS = {"Bán hàng", "Yêu cầu", "Đăng xuất"};
    private static final int[] CASHIER_ICONS = {R.drawable.nav_pos, R.drawable.nav_orders, R.drawable.nav_logout};

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
        if (DemoState.CASHIER.equals(role)) {
            if ("requests".equals(route) || route.startsWith("requests-admin")) return "requests";
            return "pos";
        }
        if (DemoState.ADMIN.equals(role)) {
            if ("pos".equals(route)) return "pos";
            if (route.startsWith("catalog-admin")) return "catalog-admin";
            if (route.startsWith("lots")) return "lots";
            return "admin";
        }
        return "";
    }

    public static void bind(Activity activity, LinearLayout container, String role,
                            String route, Consumer<String> onNavigate) {
        if (DemoState.NONE.equals(role)) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
            return;
        }
        container.setVisibility(View.VISIBLE);
        String[] routes = routesForRole(role);
        View current = container.getChildAt(0);
        BottomNavigationView bar;
        if (current instanceof BottomNavigationView && role.equals(current.getTag())) {
            bar = (BottomNavigationView) current;
        } else {
            container.removeAllViews();
            bar = create(activity, role, routes);
            container.addView(bar, new LinearLayout.LayoutParams(-1, -1));
        }

        int selected = tabIndex(routes, activeTab(role, route));
        bar.setOnItemSelectedListener(null);
        if (selected >= 0 && bar.getSelectedItemId() != 100 + selected) {
            bar.setSelectedItemId(100 + selected);
        }
        bar.setOnItemSelectedListener(item -> {
            int index = item.getItemId() - 100;
            if (index < 0 || index >= routes.length) return false;
            onNavigate.accept(routes[index]);
            return !"logout".equals(routes[index]);
        });
    }

    public static int transitionDirection(View container, String role, String previous, String next) {
        String[] routes = routesForRole(role);
        int from = tabIndex(routes, activeTab(role, previous));
        int to = tabIndex(routes, activeTab(role, next));
        return UiMotion.horizontalDirection(container, from, to);
    }

    private static String[] routesForRole(String role) {
        if (DemoState.ADMIN.equals(role)) return ADMIN;
        if (DemoState.CASHIER.equals(role)) return CASHIER;
        return CUSTOMER;
    }

    private static int tabIndex(String[] routes, String route) {
        for (int i = 0; i < routes.length; i++) {
            if (routes[i].equals(route)) return i;
        }
        return -1;
    }

    private static BottomNavigationView create(Activity activity, String role, String[] routes) {
        String[] labels = CUSTOMER_LABELS;
        int[] icons = CUSTOMER_ICONS;
        if (DemoState.ADMIN.equals(role)) {
            labels = ADMIN_LABELS;
            icons = ADMIN_ICONS;
        } else if (DemoState.CASHIER.equals(role)) {
            labels = CASHIER_LABELS;
            icons = CASHIER_ICONS;
        }
        BottomNavigationView bar = new BottomNavigationView(activity);
        bar.setTag(role);
        bar.setBackgroundResource(R.drawable.nav_bar_surface);
        bar.setElevation(0);
        bar.setItemHorizontalTranslationEnabled(false);
        bar.setLabelVisibilityMode(1); // All tabs labeled, including five-item nav.
        bar.setItemIconSize(dp(activity, 23));
        bar.setItemTextAppearanceInactive(R.style.TextAppearance_Pharma_Navigation);
        bar.setItemTextAppearanceActive(R.style.TextAppearance_Pharma_Navigation_Active);
        bar.setItemPaddingTop(dp(activity,7));
        bar.setItemPaddingBottom(dp(activity,7));
        ColorStateList itemColors = new ColorStateList(
            new int[][]{{android.R.attr.state_checked}, {}},
            new int[]{activity.getColor(R.color.emerald), activity.getColor(R.color.muted)});
        bar.setItemIconTintList(itemColors);
        bar.setItemTextColor(itemColors);
        bar.setItemActiveIndicatorEnabled(true);
        bar.setItemActiveIndicatorColor(ColorStateList.valueOf(activity.getColor(R.color.ui_navigation_selection)));
        Menu menu = bar.getMenu();
        for (int i = 0; i < routes.length; i++) {
            int itemId = 100 + i;
            menu.add(Menu.NONE, itemId, i, labels[i]).setIcon(icons[i]).setContentDescription(labels[i]);
        }
        return bar;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
