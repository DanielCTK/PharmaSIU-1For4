package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.util.List;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.NavFilter;
public final class CustomerBrowseScreen extends CatalogBase {
    public CustomerBrowseScreen(Activity a, UiHost host, NavFilter nav){super(a,host,nav);}
    public View catalog() {
        View root = a.getLayoutInflater().inflate(R.layout.catalog_v2_catalog, null, false);
        EditText input = root.findViewById(R.id.catalog_v2_catalog_search);
        View clear = root.findViewById(R.id.catalog_v2_catalog_clear);
        LinearLayout chips = root.findViewById(R.id.catalog_v2_catalog_filters);
        LinearLayout grid = root.findViewById(R.id.catalog_v2_catalog_grid);
        ScrollView scroller = root.findViewById(R.id.catalog_v2_catalog_scroll);
        Spinner sort = root.findViewById(R.id.catalog_v2_catalog_sort);
        TextView results = root.findViewById(R.id.catalog_v2_catalog_results_count);
        TextView heroCount = root.findViewById(R.id.catalog_v2_catalog_hero_count);
        Button reset = root.findViewById(R.id.catalog_v2_catalog_reset);
        TextView cart = root.findViewById(R.id.catalog_v2_catalog_cart);
        onCartChanged = () -> bindCart(cart);
        onCartChanged.run();
        UiMotion.press(cart);
        View back = root.findViewById(R.id.catalog_v2_catalog_back);
        back.setOnClickListener(v -> host.navigate("home"));
        UiMotion.press(back);
        View scrollTop = root.findViewById(R.id.catalog_v2_scroll_top);
        if (scrollTop != null) {
            scrollTop.setOnClickListener(v -> scroller.smoothScrollTo(0, 0));
        }
        List<String> filters = CatalogCategories.available(state.products);
        if (!filters.contains(nav.category)) nav.category = ALL;
        if (!SORT_OPTIONS.contains(nav.sort)) nav.sort = RECOMMENDED;
        heroCount.setVisibility(View.GONE);
        sort.setAdapter(com.pharmasiu.mobile.ui.design.PharmaUi.createSpinnerAdapter(a, SORT_OPTIONS));
        sort.setSelection(Math.max(0,SORT_OPTIONS.indexOf(nav.sort)));
        input.setText(nav.query);
        clear.setVisibility(nav.query.isEmpty()?View.GONE:View.VISIBLE);
        clear.setOnClickListener(v -> input.setText(""));
        input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                android.view.inputmethod.InputMethodManager keyboard =
                    (android.view.inputmethod.InputMethodManager)a.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);
                input.clearFocus();
                return true;
            }
            return false;
        });
        // Keep filter views (and their keyboard/TalkBack focus) while results change.
        class BrowseRenderer {
            String selectedCategory;
            boolean updatingControls;
            void render(int direction, boolean resetScroll) {
                UiMotion.slideIn(scroller, 0);
                if (!nav.category.equals(selectedCategory)) {
                    selectedCategory = nav.category;
                    for (int i = 0; i < chips.getChildCount(); i++) {
                        TextView filter = (TextView) chips.getChildAt(i);
                        String group = filters.get(i);
                        boolean selected = group.equals(nav.category);
                        setChipSelected(filter, group, selected);
                        if (selected) filter.post(() -> {
                            if (filter.isSelected() && filter.isAttachedToWindow()) {
                                filter.requestRectangleOnScreen(
                                    new Rect(0, 0, filter.getWidth(), filter.getHeight()),
                                    direction == 0 || !UiMotion.canAnimate(filter));
                            }
                        });
                    }
                }
                List<DemoState.Product> matches = filterProducts(state.products, nav.query, nav.category, nav.sort);
                results.setText(matches.size() + " sản phẩm");
                clear.setVisibility(nav.query.isEmpty() ? View.GONE : View.VISIBLE);
                buildGrid(grid, matches, false);
                if (scrollTop != null) scrollTop.setVisibility(matches.size() > 4 ? View.VISIBLE : View.GONE);
                reset.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
                if (resetScroll) scroller.scrollTo(0, 0);
                if (direction != 0) UiMotion.slideIn(scroller, direction);
            }
        }
        BrowseRenderer renderer = new BrowseRenderer();
        for (String group : filters) {
            TextView filter = chip(group, false);
            chips.addView(filter, spacedChip());
            filter.setOnClickListener(v -> {
                if (group.equals(nav.category)) return;
                int previous = filters.indexOf(nav.category);
                nav.category = group;
                renderer.render(UiMotion.horizontalDirection(chips, previous, filters.indexOf(group)), true);
            });
        }
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = SORT_OPTIONS.get(position);
                if (renderer.updatingControls || selected.equals(nav.sort)) return;
                nav.sort = selected;
                renderer.render(0, false);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                if (renderer.updatingControls || nav.query.equals(s.toString())) return;
                nav.query = s.toString();
                renderer.render(0, false);
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        reset.setOnClickListener(v -> {
            renderer.updatingControls = true;
            nav.query="";nav.category=ALL;nav.sort=RECOMMENDED;
            input.setText("");sort.setSelection(0);
            renderer.updatingControls = false;
            renderer.render(0, true);
        });
        renderer.render(0, false);
        return root;
    }

}
