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
public final class CustomerBrowseScreen extends CatalogBase {
    public CustomerBrowseScreen(Activity a, UiHost host, NavFilter nav){super(a,host,nav);}
    public View catalog() {
        View root = a.getLayoutInflater().inflate(R.layout.catalog_v2_catalog, null, false);
        EditText input = root.findViewById(R.id.catalog_v2_catalog_search);
        LinearLayout chips = root.findViewById(R.id.catalog_v2_catalog_filters);
        LinearLayout grid = root.findViewById(R.id.catalog_v2_catalog_grid);
        Spinner sort = root.findViewById(R.id.catalog_v2_catalog_sort);
        TextView results = root.findViewById(R.id.catalog_v2_catalog_results_count);
        TextView heroCount = root.findViewById(R.id.catalog_v2_catalog_hero_count);
        Button reset = root.findViewById(R.id.catalog_v2_catalog_reset);
        root.findViewById(R.id.catalog_v2_catalog_back).setOnClickListener(v -> host.navigate("home"));
        View scrollTop = root.findViewById(R.id.catalog_v2_scroll_top);
        if (scrollTop != null) {
            scrollTop.setOnClickListener(v -> {
                android.widget.ScrollView scroller = root.findViewById(R.id.catalog_v2_catalog_scroll);
                if (scroller != null) scroller.smoothScrollTo(0, 0);
            });
        }
        Set<String> categories = new LinkedHashSet<>();
        for (DemoState.Product p : state.products) categories.add(p.category);
        List<String> filters = new ArrayList<>(Arrays.asList(ALL,"Mỹ phẩm","Thực phẩm","Không kê đơn","Kê đơn"));
        for (String group : categories) if (!filters.contains(group)) filters.add(group);
        Set<String> groups = new HashSet<>(categories);
        if (state.products.stream().anyMatch(p -> p.requiresPrescription)) groups.add("Kê đơn");
        filters.removeIf(group -> !ALL.equals(group) && !"Thực phẩm".equals(group) &&
            !groups.contains(group));
        if (!filters.contains(nav.category)) nav.category = ALL;
        heroCount.setText(state.products.size() + " sản phẩm chính hãng   •   " + categories.size() + " danh mục");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(a, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(adapter);
        sort.setSelection(Math.max(0,SORT_OPTIONS.indexOf(nav.sort)));
        input.setText(nav.query);
        final Runnable[] render = new Runnable[1];
        render[0] = () -> {
            List<DemoState.Product> matches = filterProducts(state.products, nav.query, nav.category, nav.sort);
            results.setText(matches.size() + " kết quả tìm thấy");
            buildGrid(grid, matches, false);
            reset.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
            chips.removeAllViews();
            for (String group : filters) {
                TextView chip = chip(group, group.equals(nav.category));
                chips.addView(chip, spacedChip());
                chip.setOnClickListener(v -> { nav.category = group; render[0].run(); });
            }
        };
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nav.sort = SORT_OPTIONS.get(position); render[0].run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                nav.query = s.toString(); render[0].run();
            }
            @Override public void afterTextChanged(Editable editable) { }
        });
        reset.setOnClickListener(v -> {
            nav.query="";nav.category=ALL;nav.sort=RECOMMENDED;
            input.setText("");sort.setSelection(0);render[0].run();
        });
        render[0].run();
        return root;
    }
}
