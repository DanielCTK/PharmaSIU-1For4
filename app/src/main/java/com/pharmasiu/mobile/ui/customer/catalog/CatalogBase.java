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

/** Shared product grids and cards; never mutates catalog fixtures. */
abstract class CatalogBase {

    protected final Activity a;
    protected final UiHost host;
    protected final DemoState state;
    protected final NavFilter nav;

    CatalogBase(Activity activity, UiHost host, NavFilter selection) {
        this.a = activity; this.host = host; this.state = host.state(); this.nav = selection;
    }
    protected void buildGrid(LinearLayout target, List<DemoState.Product> products, boolean featured) {
        target.removeAllViews();
        if (products.isEmpty()) {
            TextView empty = new TextView(a);
            empty.setText(featured ? "Nhóm này chưa có sản phẩm." :
                "Không tìm thấy sản phẩm. Thử đổi từ khóa hoặc chọn nhóm Tất cả.");
            empty.setTextSize(14); empty.setGravity(Gravity.CENTER); empty.setTextColor(MUTED);
            empty.setPadding(dp(a,15),dp(a,28),dp(a,15),dp(a,28));
            target.addView(empty);return;
        }
        for (int i=0;i<products.size();i+=2) {
            LinearLayout row = new LinearLayout(a);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            target.addView(row, new LinearLayout.LayoutParams(-1,-2));
            DemoState.Product left = products.get(i);
            View tile = card(left, 168);
            LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0,-2,1f);
            leftParams.setMargins(0,0,dp(a,5),0);row.addView(tile,leftParams);
            if (i + 1 < products.size()) {
                View right = card(products.get(i+1), 168);
                LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0,-2,1f);
                rightParams.setMargins(dp(a,5),0,0,0);row.addView(right,rightParams);
            } else {
                View blank = new View(a);
                row.addView(blank,new LinearLayout.LayoutParams(0,1,1f));
            }
        }
    }

    protected View card(DemoState.Product p,int pictureDp) {
        View card = a.getLayoutInflater().inflate(R.layout.catalog_v2_card,null,false);
        FrameLayout media = card.findViewById(R.id.catalog_v2_card_media);
        media.getLayoutParams().height = dp(a,pictureDp);
        media.addView(ProductImageLoader.createArtwork(a,p),0,new FrameLayout.LayoutParams(-1,-1));
        ((TextView) card.findViewById(R.id.catalog_v2_card_category)).setText(
            p.requiresPrescription ? "RX • CẦN TOA" : p.category.toUpperCase(new Locale("vi","VN")));
        ((TextView) card.findViewById(R.id.catalog_v2_card_name)).setText(p.name);
        ((TextView) card.findViewById(R.id.catalog_v2_card_subtitle)).setText(subtitle(p));
        ((TextView) card.findViewById(R.id.catalog_v2_card_price)).setText(p.priceVnd <= 0 ? "Chờ báo giá" : money(p.priceVnd));
        ((TextView) card.findViewById(R.id.catalog_v2_card_caveat)).setText(
            p.requiresPrescription ? "Cần dược sĩ kiểm tra toa" : "Giá đã bao gồm VAT");
        TextView favorite = card.findViewById(R.id.catalog_v2_card_favorite);
        Runnable sync = () -> {
            boolean liked = state.favorites.contains(p.id);
            favorite.setText("");
            favorite.setCompoundDrawablesWithIntrinsicBounds(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline, 0, 0, 0);
            favorite.setContentDescription((liked ? "Bỏ yêu thích " : "Lưu yêu thích ") + p.name);
        };
        sync.run();
        favorite.setOnClickListener(v -> {
            state.toggleFavorite(p.id);sync.run();
            host.message(state.favorites.contains(p.id) ? "Đã lưu yêu thích DEMO" : "Đã bỏ yêu thích DEMO");
        });
        card.setContentDescription("Xem chi tiết " + p.name);
        card.setOnClickListener(v -> { state.selectedProductId = p.id; host.navigate("product"); });
        return card;
    }

    protected TextView chip(String label,boolean selected) {
        TextView chip=new TextView(a);
        chip.setGravity(Gravity.CENTER);
        chip.setText(label);chip.setTextSize(12);
        chip.setPadding(dp(a,13),0,dp(a,13),0);
        chip.setTextColor(selected ? Color.WHITE : EMERALD);
        chip.setSelected(selected);
        chip.setBackgroundResource(R.drawable.catalog_v2_chip);
        chip.setContentDescription(label + (selected ? " đang chọn" : ""));
        return chip;
    }

    protected LinearLayout.LayoutParams spacedChip() {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(a,49));
        lp.setMargins(0,0,dp(a,8),0);return lp;
    }
}
