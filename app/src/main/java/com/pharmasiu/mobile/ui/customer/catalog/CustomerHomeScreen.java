package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.util.ArrayList;
import java.util.List;

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
        HomeBannerCarousel banner = root.findViewById(R.id.catalog_home_banner);
        banner.setOnExplore(category -> {
            nav.category = category; nav.query = ""; toCatalog.run();
        });
        root.findViewById(R.id.catalog_v2_home_all).setOnClickListener(v -> {
            nav.category = ALL; toCatalog.run();
        });
        root.findViewById(R.id.catalog_v2_home_open_catalog).setOnClickListener(v -> {
            nav.category = ALL; toCatalog.run();
        });
        TextView cart = root.findViewById(R.id.catalog_v2_home_cart);
        onCartChanged = () -> bindCart(cart);
        onCartChanged.run();
        String[] groups = {"Mỹ phẩm", "Thực phẩm", "Thiết bị", "Chăm sóc"};
        int[] photos = {R.drawable.catalog_category_cosmetics, R.drawable.catalog_category_food,
            R.drawable.catalog_category_devices, R.drawable.catalog_category_care};
        shortcuts.setOrientation(LinearLayout.VERTICAL);
        int columns = useSingleColumn() ? 2 : 4;
        int photoSize = dp(a, Math.min(88, (a.getResources().getConfiguration().screenWidthDp
            - 24 - (columns - 1) * 8) / columns));
        for (int start=0;start<groups.length;start+=columns) {
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1,-2);
            rowParams.bottomMargin = dp(a,8);
            shortcuts.addView(row,rowParams);
            for (int column=0;column<columns && start+column<groups.length;column++) {
                String group=groups[start+column];
                LinearLayout shortcut = new LinearLayout(a);
                shortcut.setOrientation(LinearLayout.VERTICAL);
                shortcut.setGravity(Gravity.CENTER_HORIZONTAL);
                shortcut.setBackgroundResource(R.drawable.catalog_market_card_focus);

                ImageView image = new ImageView(a);
                image.setImageResource(photos[start+column]);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setBackgroundResource(R.drawable.catalog_v2_photo_surface);
                image.setClipToOutline(true);
                image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                shortcut.addView(image, new LinearLayout.LayoutParams(photoSize,photoSize));

                TextView title = new TextView(a);
                title.setText(group);
                title.setTextSize(13);
                title.setTextColor(EMERALD);
                title.setGravity(Gravity.CENTER);
                title.setPadding(0,dp(a,6),0,dp(a,3));
                title.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                shortcut.addView(title, new LinearLayout.LayoutParams(-1,-2));
                shortcut.setFocusable(true);
                shortcut.setContentDescription("Khám phá nhóm " + group);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,-2,1f);
                lp.setMargins(column==0?0:dp(a,4),0,column==columns-1?0:dp(a,4),0);
                row.addView(shortcut,lp);
                shortcut.setOnClickListener(v -> { nav.category=group;nav.query="";toCatalog.run(); });
                UiMotion.press(shortcut);
            }
        }
        for (int i = 0; i < SHELVES.size(); i++) {
            int next = i;
            String shelf = SHELVES.get(i);
            TextView chip = chip(shelf, shelf.equals(nav.shelf));
            if (i == 0) chip.setText("Gợi ý");
            chips.addView(chip, spacedChip());
            chip.setOnClickListener(v -> {
                int previous = SHELVES.indexOf(nav.shelf);
                if (next == previous) return;
                nav.shelf = shelf;
                if (previous >= 0) {
                    setChipSelected((TextView) chips.getChildAt(previous), SHELVES.get(previous), false);
                }
                setChipSelected(chip, shelf, true);
                renderHomeFeatured(grid);
                UiMotion.slideIn(grid, UiMotion.horizontalDirection(chips, previous, next));
            });
        }
        UiMotion.press(cart);
        UiMotion.press(root.findViewById(R.id.catalog_v2_home_search));
        UiMotion.press(root.findViewById(R.id.catalog_v2_home_open_catalog));
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
                for (String group : new String[]{"Mỹ phẩm", "Thiết bị", "Chăm sóc"}) {
                    int groupCount = 0;
                    for (DemoState.Product product : state.products) {
                        if (!product.requiresPrescription && CatalogCategories.matches(product, group)) {
                            list.add(product);
                            if (++groupCount == 2) break;
                        }
                    }
                }
                if (list.isEmpty()) {
                    for (DemoState.Product product : state.products) {
                        if (!product.requiresPrescription) list.add(product);
                        if (list.size() == 6) break;
                    }
                }
            }
        } else {
            for (DemoState.Product p : state.products) {
                if (CatalogCategories.matches(p, nav.shelf)) list.add(p);
            }
        }
        buildGrid(grid, list, true);
    }
}
