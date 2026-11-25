package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.text.Layout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.NavFilter;

/** Shared readable product grids and cards; catalog data stays in the repository. */
abstract class CatalogBase {
    protected final Activity a;
    protected final UiHost host;
    protected final DemoState state;
    protected final NavFilter nav;
    protected Runnable onCartChanged = () -> { };
    private BottomSheetDialog quantitySheet;
    private int lastCartCount=-1;

    CatalogBase(Activity activity, UiHost host, NavFilter selection) {
        this.a = activity;
        this.host = host;
        this.state = host.state();
        this.nav = selection;
    }

    protected boolean useSingleColumn() {
        AccessibilityManager accessibility = (AccessibilityManager)
            a.getSystemService(Activity.ACCESSIBILITY_SERVICE);
        return a.getResources().getConfiguration().screenWidthDp < 360
            || a.getResources().getConfiguration().fontScale >= 1.3f
            || (accessibility != null && accessibility.isTouchExplorationEnabled());
    }

    protected void buildGrid(LinearLayout target, List<DemoState.Product> products, boolean featured) {
        target.removeAllViews();
        if (products.isEmpty()) {
            TextView empty = new TextView(a);
            empty.setText(featured ? "Nhóm này chưa có sản phẩm." :
                "Không có kết quả.\nĐổi từ khóa hoặc bộ lọc.");
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setTextColor(MUTED);
            empty.setPadding(dp(a,16),dp(a,32),dp(a,16),dp(a,32));
            target.addView(empty, new LinearLayout.LayoutParams(-1,-2));
            return;
        }
        CatalogStaggeredGrid grid = new CatalogStaggeredGrid(a);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(-1, -2);
        gridParams.bottomMargin = dp(a, 18);
        target.addView(grid, gridParams);
        for (DemoState.Product product : products) {
            grid.addView(card(product, featured ? 142 : 156));
        }
    }

    protected View card(DemoState.Product p,int pictureDp) {
        View card = a.getLayoutInflater().inflate(R.layout.catalog_v2_card,null,false);
        FrameLayout media = card.findViewById(R.id.catalog_v2_card_media);
        media.getLayoutParams().height = dp(a,pictureDp);
        media.addView(ProductImageLoader.createGridArtwork(a,p),0,new FrameLayout.LayoutParams(-1,-1));
        TextView category = card.findViewById(R.id.catalog_v2_card_category);
        category.setText("Rx · Cần toa");
        category.setVisibility(p.requiresPrescription?View.VISIBLE:View.GONE);
        TextView name = card.findViewById(R.id.catalog_v2_card_name);
        name.setText(p.name);
        name.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
        name.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        String specification = CatalogCategories.detailOf(p);
        ((TextView) card.findViewById(R.id.catalog_v2_card_subtitle)).setText(
            specification.isEmpty() ? p.unit : specification);
        ((TextView) card.findViewById(R.id.catalog_v2_card_price)).setText(
            p.priceVnd <= 0 ? "Chờ báo giá" : money(p.priceVnd));
        TextView favorite = card.findViewById(R.id.catalog_v2_card_favorite);
        Runnable sync = () -> {
            boolean liked = state.favorites.contains(p.id);
            setIcon(favorite,liked?R.drawable.ic_heart_filled:R.drawable.ic_heart_outline);
            favorite.setContentDescription((liked ? "Bỏ yêu thích " : "Lưu yêu thích ") + p.name);
            favorite.setSelected(liked);
        };
        sync.run();
        favorite.setOnClickListener(v -> {
            state.toggleFavorite(p.id);
            sync.run();
            UiMotion.badge(favorite);
            host.message(state.favorites.contains(p.id) ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");
        });
        Runnable openDetail = () -> {
            state.selectedProductId = p.id;
            host.navigate("product");
        };
        Button add = card.findViewById(R.id.catalog_v2_card_add);
        add.setText(p.requiresPrescription ? "→" : "+");
        add.setContentDescription(p.requiresPrescription ? "Xem yêu cầu toa của " + p.name : "Thêm " + p.name + " vào giỏ hàng");
        add.setOnClickListener(v -> {
            if (!DemoState.CUSTOMER.equals(state.role)) {
                host.message("Vui lòng đăng nhập tài khoản khách hàng để thêm vào giỏ.");
                return;
            }
            // Rx cards open full information; the existing detail flow handles the prescription.
            if (p.requiresPrescription) {
                openDetail.run();
                return;
            }
            if(!state.addToCart(p.id)) {
                showCartFeedback("Mỗi mặt hàng tối đa 20 đơn vị. Bạn đã có đủ trong giỏ.");
                return;
            }
            add.setEnabled(false);
            add.postDelayed(() -> add.setEnabled(true),350);
            cartAdded(p,1);
        });
        UiMotion.press(add);
        UiMotion.press(favorite);
        card.setContentDescription(p.name + ". " + (p.priceVnd <= 0 ? "Chờ báo giá" : money(p.priceVnd))
            + ". " + p.unit + (p.requiresPrescription ? ". Thuốc kê đơn, cần toa." : ". Xem chi tiết."));
        card.setOnClickListener(v -> openDetail.run());
        return card;
    }

    protected void bindCart(TextView cart) {
        setIcon(cart,R.drawable.ic_cart);
        int count=state.cart.values().stream().mapToInt(Integer::intValue).sum();
        cart.setText(count>0 ? String.valueOf(count) : "");
        cart.setContentDescription(count>0 ? "Mở giỏ hàng, " + count + " sản phẩm" : "Mở giỏ hàng");
        cart.setOnClickListener(v -> host.navigate("cart"));
        if(lastCartCount>=0 && count>lastCartCount)UiMotion.badge(cart);
        lastCartCount=count;
    }

    protected void showQuantityPicker(DemoState.Product product) {
        if(product==null || product.requiresPrescription || !DemoState.CUSTOMER.equals(state.role))return;
        if(quantitySheet!=null && quantitySheet.isShowing())return;
        if(state.cart.getOrDefault(product.id,0)>=20) {
            showCartFeedback("Mỗi mặt hàng tối đa 20 đơn vị. Bạn đã có đủ trong giỏ.");
            return;
        }
        quantitySheet=CatalogQuantitySheet.show(a,host,product,(added,count) -> {
            cartAdded(added,count);
        });
        if(quantitySheet!=null)quantitySheet.setOnDismissListener(ignored -> quantitySheet=null);
    }

    private void cartAdded(DemoState.Product product,int count) {
        onCartChanged.run();
        showCartFeedback("Đã thêm " + count + " " + product.unit.toLowerCase(new java.util.Locale("vi","VN")) + " " + product.name + ".");
    }

    private void showCartFeedback(String message) {
        View content=a.findViewById(android.R.id.content);
        if(content==null){host.message(message);return;}
        Snackbar bar=Snackbar.make(content,message,Snackbar.LENGTH_LONG)
            .setAction("Xem giỏ",view -> host.navigate("cart"))
            .setBackgroundTint(a.getColor(R.color.forest))
            .setTextColor(a.getColor(R.color.white))
            .setActionTextColor(a.getColor(R.color.lime));
        View navigation=a.findViewById(R.id.navigation_bar);
        if(navigation!=null && navigation.isShown())bar.setAnchorView(navigation);
        TextView text=bar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if(text!=null){text.setMaxLines(4);text.setTextSize(15);}
        bar.show();
    }

    protected void setIcon(TextView target,int resource) {
        Drawable icon=a.getDrawable(resource);
        if(icon!=null) {
            icon=icon.mutate();
            icon.setTint(a.getColor(R.color.emerald));
            icon.setBounds(0,0,dp(a,22),dp(a,22));
        }
        target.setCompoundDrawables(icon,null,null,null);
    }

    protected TextView chip(String label,boolean selected) {
        TextView chip=new TextView(a);
        chip.setGravity(Gravity.CENTER);
        chip.setText(label);
        chip.setTextSize(14);
        chip.setMinHeight(dp(a,48));
        chip.setPadding(dp(a,14),dp(a,9),dp(a,14),dp(a,9));
        int[] selectedState = {android.R.attr.state_selected};
        int[][] states = {selectedState, new int[0]};
        ColorStateList ink = new ColorStateList(states, new int[]{Color.WHITE, EMERALD});
        chip.setTextColor(ink);
        android.graphics.drawable.StateListDrawable surface = new android.graphics.drawable.StateListDrawable();
        surface.addState(new int[]{android.R.attr.state_focused, android.R.attr.state_selected},
            chipSurface(EMERALD, 0xFFC3E88D));
        surface.addState(new int[]{android.R.attr.state_focused}, chipSurface(0xFFEAF3F1, EMERALD));
        surface.addState(selectedState, chipSurface(EMERALD));
        surface.addState(new int[0], chipSurface(0xFFEAF3F1));
        chip.setBackground(new RippleDrawable(
            new ColorStateList(states, new int[]{0x30FFFFFF, 0x16065640}), surface, null));
        Drawable categoryIcon=a.getDrawable(CatalogCategories.iconRes(label));
        if(categoryIcon!=null) {
            categoryIcon=categoryIcon.mutate();
            categoryIcon.setTintList(ink);
            categoryIcon.setBounds(0,0,dp(a,20),dp(a,20));
            chip.setCompoundDrawables(categoryIcon,null,null,null);
            chip.setCompoundDrawablePadding(dp(a,7));
        }
        chip.setFocusable(true);
        setChipSelected(chip, label, selected);
        UiMotion.press(chip);
        return chip;
    }

    private GradientDrawable chipSurface(int fill) {
        GradientDrawable surface = new GradientDrawable();
        surface.setCornerRadius(dp(a,24));
        surface.setColor(fill);
        return surface;
    }

    private GradientDrawable chipSurface(int fill, int outline) {
        GradientDrawable surface = chipSurface(fill);
        surface.setStroke(dp(a, 2), outline);
        return surface;
    }

    protected void setChipSelected(TextView chip, String label, boolean selected) {
        chip.setSelected(selected);
        chip.setContentDescription(label + (selected ? ", đang chọn" : ""));
    }

    protected LinearLayout.LayoutParams spacedChip() {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.setMargins(0,0,dp(a,8),0);
        return lp;
    }
}
