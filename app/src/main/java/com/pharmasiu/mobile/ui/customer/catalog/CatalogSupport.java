package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

/** Catalog selection state and deterministic search, safe across screen recreation. */
public final class CatalogSupport {
    private CatalogSupport(){}
    // Compile-time constants keep pure-JVM catalog rule tests Android-independent.
    static final int EMERALD = 0xFF064D3A;
    static final int FOREST = 0xFF073D30;
    static final int MUTED = 0xFF566B63;
    static final int MINT = 0xFFE6F3E6;
    static final int WARNING = 0xFF9A5318;
    static final String ALL = "Tất cả", RECOMMENDED = "Đề xuất";
    static final List<String> SORT_OPTIONS = Collections.unmodifiableList(Arrays.asList(
        RECOMMENDED, "Tên A–Z", "Giá thấp", "Giá cao"));
    static final List<String> SHELVES = Collections.unmodifiableList(Arrays.asList(
        "Dành cho bạn", "Mỹ phẩm", "Thực phẩm", "Thiết bị", "Chăm sóc"));
    static final List<String> FEATURED_IDS = Collections.unmodifiableList(Arrays.asList(
        "beauty-02", "food-01", "beauty-01", "food-03", "device-02", "beauty-06",
        "food-02", "care-01", "beauty-04", "supp-01", "beauty-03", "food-05",
        "device-03", "care-02", "supplies-02", "beauty-05", "food-04", "supp-02",
        "otc-01", "thermometer-01"));
    static final WeakHashMap<DemoState, NavFilter> NAV_FILTERS = new WeakHashMap<>();
    public static synchronized NavFilter filterFor(DemoState state) {
        NavFilter filter = NAV_FILTERS.get(state);
        if (filter == null) { filter = new NavFilter(); NAV_FILTERS.put(state, filter); }
        return filter;
    }

    public static final class NavFilter {
        String category = ALL;
        String query = "";
        String sort = RECOMMENDED;
        String shelf = SHELVES.get(0);
    }

    /** Exact parity with Kotlin CatalogSearchRules, including lack of Vietnamese diacritics. */
    public static String normalize(String raw) {
        if (raw == null) return "";
        return Normalizer.normalize(raw.toLowerCase(new Locale("vi", "VN")), Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").replace('đ','d').trim();
    }

    public static List<DemoState.Product> filterProducts(List<DemoState.Product> products,
                String query, String category, String sort) {
        List<DemoState.Product> result = new ArrayList<>();
        String normalized = normalize(query);
        String[] terms = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
        String selected = category == null ? ALL : category;
        for (DemoState.Product item : products) {
            String searchable = normalize(item.name + " " + item.subtitle + " " + item.category);
            boolean matches = true;
            for (String word : terms) if (!searchable.contains(word)) { matches = false; break; }
            boolean group = ALL.equals(selected) ||
                ("Kê đơn".equals(selected) && item.requiresPrescription) ||
                ("Thực phẩm".equals(selected) &&
                    ("Thực phẩm".equals(item.category) || "Thực phẩm bổ sung".equals(item.category))) ||
                selected.equals(item.category);
            if (matches && group) result.add(item);
        }
        String order = sort == null ? RECOMMENDED : sort;
        if ("Tên A–Z".equals(order))
            result.sort(Comparator.comparing((DemoState.Product p) -> p.name,
                String.CASE_INSENSITIVE_ORDER).thenComparing(p -> p.id));
        else if ("Giá thấp".equals(order))
            result.sort(Comparator.comparing((DemoState.Product p) -> p.priceVnd <= 0L)
                .thenComparingLong(p -> p.priceVnd).thenComparing(p -> p.id));
        else if ("Giá cao".equals(order))
            result.sort(Comparator.comparing((DemoState.Product p) -> p.priceVnd <= 0L)
                .thenComparing((DemoState.Product p) -> p.priceVnd, Comparator.reverseOrder())
                .thenComparing(p -> p.id));
        return result;
    }

    public static int dp(Activity a, float d) {
        return Math.round(d * a.getResources().getDisplayMetrics().density);
    }

    public static String money(long value) {
        return String.format(new Locale("vi", "VN"), "%,d ₫", value);
    }

    public static String description(DemoState.Product p) {
        if (p == null) return "";
        return ProductDetailData.getUsage(p.id, p.description);
    }

    public static String ingredients(DemoState.Product p) {
        if (p == null) return "";
        return ProductDetailData.getIngredients(p.id, p.description);
    }

    public static String safetyNotice(DemoState.Product p) {
        if (p == null) return "";
        return ProductDetailData.getSafetyNotice(p.id, p.category, p.requiresPrescription);
    }

    public static String subtitle(DemoState.Product p) {
        if (p == null) return "";
        return p.subtitle != null ? p.subtitle : "";
    }
}
