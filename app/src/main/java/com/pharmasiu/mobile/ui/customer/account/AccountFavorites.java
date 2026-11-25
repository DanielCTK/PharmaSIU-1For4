package com.pharmasiu.mobile.ui.customer.account;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.TextUtils;

import com.google.android.material.button.MaterialButton;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.customer.catalog.ProductImageLoader;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.design.UiMotion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Image-led saved items and a concise, native onboarding flow. */
public abstract class AccountFavorites extends AccountAddress {
    protected AccountFavorites(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}

    protected void favorites() {
        List<DemoState.Product> saved = new ArrayList<>();
        for (String id : new ArrayList<>(state.favorites)) {
            DemoState.Product item = state.product(id);
            if (item != null) saved.add(item);
        }
        hero("ĐÃ LƯU","Sản phẩm yêu thích",saved.size()+" sản phẩm",true);
        if (saved.isEmpty()) {
            LinearLayout empty = card();
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.setPadding(dp(20),dp(26),dp(20),dp(22));
            ImageView artwork=icon(R.drawable.customer_account_heart,R.color.emerald);
            artwork.setBackground(activity.getDrawable(R.drawable.customer_account_avatar));
            artwork.setPadding(dp(19),dp(19),dp(19),dp(19));
            empty.addView(artwork,new LinearLayout.LayoutParams(dp(76),dp(76)));
            TextView heading=label("Chưa có mục yêu thích",19,R.color.forest,true);
            heading.setGravity(Gravity.CENTER);
            empty.addView(heading,params(-1,-2,16));
            TextView detail=label("Lưu sản phẩm bằng biểu tượng trái tim.",14,R.color.muted,false);
            detail.setGravity(Gravity.CENTER);
            empty.addView(detail,params(-1,-2,8));
            action(empty,"Khám phá sản phẩm",true,() -> host.navigate("catalog"));
        } else {
            for (DemoState.Product product : saved) favoriteCard(product);
        }
        disclaimer();
    }

    private void favoriteCard(DemoState.Product product) {
        LinearLayout card=card();
        card.setPadding(dp(12),dp(12),dp(12),dp(12));
        LinearLayout row=new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(88));
        card.addView(row,new LinearLayout.LayoutParams(-1,-2));
        FrameLayout thumbnail=new FrameLayout(activity);
        thumbnail.setBackground(activity.getDrawable(R.drawable.customer_account_icon_surface));
        thumbnail.setClipToOutline(true);
        thumbnail.setPadding(dp(4),dp(4),dp(4),dp(4));
        View artwork=ProductImageLoader.createThumbnail(activity,product);
        artwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        thumbnail.addView(artwork,new FrameLayout.LayoutParams(-1,-1));
        row.addView(thumbnail,new LinearLayout.LayoutParams(dp(72),dp(84)));
        LinearLayout copy=stack();
        LinearLayout.LayoutParams copyParams=new LinearLayout.LayoutParams(0,-2,1);
        copyParams.setMarginStart(dp(12)); copyParams.setMarginEnd(dp(8));
        row.addView(copy,copyParams);
        TextView name=label(product.name,16,R.color.ink,true);
        name.setMaxLines(2); name.setEllipsize(TextUtils.TruncateAt.END);
        name.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(name,params(-1,-2,0));
        String price=product.priceVnd<=0 ? "Chờ báo giá" :
            String.format(new Locale("vi","VN"),"%,d ₫",product.priceVnd);
        TextView amount=label(price,15,R.color.emerald,true);
        amount.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(amount,params(-1,-2,6));
        TextView kind=label(product.requiresPrescription ? "Rx · Cần toa" : com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories.categoryOf(product),
            13,product.requiresPrescription ? R.color.warning : R.color.muted,false);
        kind.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(kind,params(-1,-2,3));
        MaterialButton remove=new MaterialButton(activity);
        remove.setText("");
        remove.setIconResource(R.drawable.ic_heart_filled);
        remove.setIconTint(ColorStateList.valueOf(color(R.color.emerald)));
        remove.setIconSize(dp(22)); remove.setIconPadding(0);
        remove.setPadding(dp(12),dp(12),dp(12),dp(12));
        remove.setInsetTop(0); remove.setInsetBottom(0);
        remove.setMinWidth(0); remove.setMinimumWidth(0); remove.setMinHeight(dp(48));
        remove.setCornerRadius(dp(14));
        remove.setBackgroundTintList(ColorStateList.valueOf(color(R.color.mint)));
        remove.setContentDescription("Bỏ yêu thích "+product.name);
        remove.setOnClickListener(v -> { state.toggleFavorite(product.id); draw(); });
        UiMotion.press(remove);
        row.addView(remove,new LinearLayout.LayoutParams(dp(48),dp(48)));
        PharmaUi.pressable(row);
        UiMotion.press(row);
        row.setContentDescription("Xem "+product.name+". "+price+". "+
            (product.requiresPrescription ? "Thuốc kê đơn, cần toa." : com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories.categoryOf(product)));
        row.setOnClickListener(v -> {
            state.selectedProductId=product.id;
            host.navigate("product");
        });
    }

    protected View onboarding() {
        ScrollView root=(ScrollView) LayoutInflater.from(activity)
            .inflate(R.layout.customer_account_onboarding,null,false);
        TextView index=root.findViewById(R.id.customer_onboarding_index);
        TextView title=root.findViewById(R.id.customer_onboarding_title);
        TextView detail=root.findViewById(R.id.customer_onboarding_detail);
        ImageView artwork=root.findViewById(R.id.customer_onboarding_artwork);
        TextView progress=root.findViewById(R.id.customer_onboarding_progress);
        Button next=root.findViewById(R.id.customer_onboarding_next);
        Button skip=root.findViewById(R.id.customer_onboarding_skip);
        String[] headings={"Khám phá theo nhu cầu","Lưu điều bạn thích","Tạo yêu cầu tại máy"};
        String[] descriptions={
            "Duyệt danh mục và tìm sản phẩm bạn cần.",
            "Chạm trái tim để lưu sản phẩm và xem lại nhanh.",
            "Yêu cầu demo lưu trên thiết bị; chưa giữ hàng hay thanh toán thật."
        };
        int[] images={R.drawable.customer_account_orders,R.drawable.customer_account_heart,
            R.drawable.customer_account_location};
        Runnable update=() -> {
            int page=Math.max(0,Math.min(2,onboardingProgress.step));
            index.setText(String.format(Locale.ROOT,"0%d  /  03",page+1));
            title.setText(headings[page]);
            detail.setText(descriptions[page]);
            artwork.setImageResource(images[page]);
            progress.setText(page==0 ? "●  ○  ○" : page==1 ? "○  ●  ○" : "○  ○  ●");
            progress.setContentDescription("Giới thiệu "+(page+1)+" trên 3");
            next.setText(page==2 ? "Bắt đầu khám phá" : "Tiếp tục");
        };
        Runnable finish=() -> {
            onboardingProgress.viewed=true;
            onboardingProgress.step=0;
            host.navigate("home");
        };
        next.setOnClickListener(v -> {
            if (onboardingProgress.step>=2) finish.run();
            else { onboardingProgress.step++; update.run(); }
        });
        skip.setOnClickListener(v -> finish.run());
        UiMotion.press(next);
        UiMotion.press(skip);
        update.run();
        return root;
    }
}
