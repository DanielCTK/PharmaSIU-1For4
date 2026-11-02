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

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.NavFilter;
public final class CustomerHomeScreen extends CatalogBase {
    public CustomerHomeScreen(Activity a, UiHost host, NavFilter nav){super(a,host,nav);}
    public View home() {
        View root = a.getLayoutInflater().inflate(R.layout.catalog_v2_home, null, false);
        LinearLayout grid = root.findViewById(R.id.catalog_v2_home_grid);
        LinearLayout chips = root.findViewById(R.id.catalog_v2_home_shelves);
        LinearLayout shortcuts = root.findViewById(R.id.catalog_v2_home_shortcuts);
        Runnable toCatalog = () -> host.navigate("catalog");
        root.findViewById(R.id.catalog_v2_home_search).setOnClickListener(v -> {
            nav.category = ALL; nav.query = ""; toCatalog.run();
        });
        root.findViewById(R.id.catalog_v2_home_explore).setOnClickListener(v -> {
            nav.category = ALL; toCatalog.run();
        });
        root.findViewById(R.id.catalog_v2_home_all).setOnClickListener(v -> {
            nav.category = ALL; toCatalog.run();
        });
        root.findViewById(R.id.catalog_v2_home_open_catalog).setOnClickListener(v -> {
            nav.category = ALL; toCatalog.run();
        });
        TextView cart = root.findViewById(R.id.catalog_v2_home_cart);
        int totalCart = state.cart.isEmpty() ? 0 : state.cart.values().stream().mapToInt(Integer::intValue).sum();
        android.graphics.drawable.Drawable cartIcon = a.getDrawable(R.drawable.ic_cart);
        if (cartIcon != null) {
            cartIcon.setBounds(0, 0, dp(a, 22), dp(a, 22));
            cartIcon.setTint(a.getColor(R.color.emerald));
            cart.setCompoundDrawables(cartIcon, null, null, null);
        }
        cart.setText(totalCart > 0 ? " " + totalCart : "");
        cart.setOnClickListener(v -> host.navigate("cart"));
        String[] groups = {"Mỹ phẩm", "Thực phẩm", "Không kê đơn"};
        for (String group : groups) {
            TextView shortcut = chip(group, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(a, 58), 1f);
            lp.setMargins(0,0,dp(a,8),0); shortcuts.addView(shortcut,lp);
            shortcut.setBackgroundTintList(ColorStateList.valueOf(MINT));
            shortcut.setOnClickListener(v -> { nav.category = group; nav.query = ""; toCatalog.run(); });
        }
        final Runnable[] renderShelves = new Runnable[1];
        renderShelves[0] = () -> {
            chips.removeAllViews();
            for (String shelf : SHELVES) {
                TextView chip = chip(shelf, shelf.equals(nav.shelf));
                chips.addView(chip, spacedChip());
                chip.setOnClickListener(v -> {
                    nav.shelf = shelf;
                    renderShelves[0].run();
                    renderHomeFeatured(grid);
                });
            }
        };
        renderShelves[0].run();
        renderHomeFeatured(grid);
        return root;
    }

    protected void renderHomeFeatured(LinearLayout grid) {
        List<DemoState.Product> list = new ArrayList<>();
        if (SHELVES.get(0).equals(nav.shelf)) {
            for (String id : FEATURED_IDS) {
                DemoState.Product p = state.product(id);
                if (p != null) list.add(p);
            }
            // When catalog is SQLite-backed, choose the first real products,
            // never show an empty homepage because old fixture IDs differ.
            if (list.isEmpty()) {
                for (DemoState.Product product : state.products) {
                    if (!product.requiresPrescription) list.add(product);
                    if (list.size() == 6) break;
                }
            }
        } else {
            for (DemoState.Product p : state.products) {
                if (nav.shelf.equals(p.category) || ("Thực phẩm".equals(nav.shelf) &&
                    "Thực phẩm bổ sung".equals(p.category))) list.add(p);
            }
        }
        buildGrid(grid, list, true);
    }
}
