package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.app.Application;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.money;

/** Non-Rx quantity selection. Uses existing cart commands, never stock or payment claims. */
final class CatalogQuantitySheet {
    private static final int MAX_PER_PRODUCT=20;
    interface Added { void onAdded(DemoState.Product product,int quantity); }

    private CatalogQuantitySheet() { }

    static BottomSheetDialog show(Activity activity,UiHost host,DemoState.Product product,Added added) {
        DemoState state=host.state();
        if (activity.isFinishing() || activity.isDestroyed() || product==null
            || product.requiresPrescription || !DemoState.CUSTOMER.equals(state.role)) return null;
        int existing=state.cart.getOrDefault(product.id,0);
        if (existing>=MAX_PER_PRODUCT) return null;

        BottomSheetDialog dialog=new OwnedBottomSheetDialog(activity);
        View content=activity.getLayoutInflater().inflate(R.layout.catalog_v2_quantity_sheet,null,false);
        dialog.setContentView(content);
        ViewCompat.setAccessibilityPaneTitle(content,"Chọn số lượng " + product.name);
        ViewCompat.setAccessibilityHeading(content.findViewById(R.id.catalog_v2_quantity_title),true);
        FrameLayout media=content.findViewById(R.id.catalog_v2_quantity_media);
        media.addView(ProductImageLoader.createArtwork(activity,product),new FrameLayout.LayoutParams(-1,-1));
        ((TextView)content.findViewById(R.id.catalog_v2_quantity_name)).setText(product.name);
        ((TextView)content.findViewById(R.id.catalog_v2_quantity_unit)).setText(product.unit + " · " + CatalogCategories.categoryOf(product));
        ((TextView)content.findViewById(R.id.catalog_v2_quantity_price)).setText(
            product.priceVnd>0 ? money(product.priceVnd) + " / " + product.unit : "Chờ nhà thuốc báo giá");
        TextView quantity=content.findViewById(R.id.catalog_v2_quantity_value);
        TextView limit=content.findViewById(R.id.catalog_v2_quantity_limit);
        TextView total=content.findViewById(R.id.catalog_v2_quantity_total);
        Button minus=content.findViewById(R.id.catalog_v2_quantity_decrease);
        Button plus=content.findViewById(R.id.catalog_v2_quantity_increase);
        Button confirm=content.findViewById(R.id.catalog_v2_quantity_confirm);
        Button close=content.findViewById(R.id.catalog_v2_quantity_close);
        minus.setContentDescription("Giảm số lượng " + product.name);
        plus.setContentDescription("Tăng số lượng " + product.name);
        ColorStateList stepColors=new ColorStateList(
            new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
            new int[]{activity.getColor(R.color.muted),activity.getColor(R.color.forest)});
        minus.setTextColor(stepColors);
        plus.setTextColor(stepColors);
        int[] selected={1};
        boolean[] submitted={false};
        Runnable render=() -> {
            int current=state.cart.getOrDefault(product.id,0);
            int capacity=Math.max(0,MAX_PER_PRODUCT-current);
            selected[0]=Math.max(1,Math.min(selected[0],Math.max(1,capacity)));
            quantity.setText(String.valueOf(selected[0]));
            quantity.setContentDescription("Số lượng chọn: " + selected[0] + " " + product.unit);
            minus.setEnabled(selected[0]>1);
            plus.setEnabled(selected[0]<capacity);
            limit.setText(current>0 ? "Đang có " + current + " trong giỏ · tối đa 20 mỗi mặt hàng."
                : "Có thể chọn từ 1 đến 20 " + product.unit.toLowerCase(new java.util.Locale("vi","VN")) + ".");
            String amount=product.priceVnd>0 ? money(product.priceVnd*selected[0]) : "Chờ báo giá";
            total.setText(amount);
            confirm.setText("Thêm " + selected[0] + " vào giỏ · " + amount);
            confirm.setEnabled(capacity>0 && !submitted[0]);
        };
        minus.setOnClickListener(v -> { if(selected[0]>1){selected[0]--;render.run();} });
        plus.setOnClickListener(v -> {
            if(selected[0]<MAX_PER_PRODUCT-state.cart.getOrDefault(product.id,0)){selected[0]++;render.run();}
        });
        close.setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            if(submitted[0])return;
            DemoState.Product currentProduct=state.product(product.id);
            if(!DemoState.CUSTOMER.equals(state.role) || currentProduct==null || currentProduct.requiresPrescription) {
                dialog.dismiss();
                host.message("Thông tin sản phẩm hoặc phiên đăng nhập đã thay đổi. Vui lòng mở lại sản phẩm.");
                return;
            }
            int current=state.cart.getOrDefault(product.id,0);
            int remaining=MAX_PER_PRODUCT-current;
            if(selected[0]>remaining || remaining<=0) {
                render.run();
                limit.setText("Giỏ hàng đã thay đổi. Mỗi mặt hàng tối đa 20 đơn vị.");
                return;
            }
            submitted[0]=true;
            confirm.setEnabled(false);
            int count=selected[0];
            state.changeCartQty(product.id,current+count);
            if(state.cart.getOrDefault(product.id,0)!=current+count) {
                submitted[0]=false;
                render.run();
                limit.setText("Chưa thể thêm sản phẩm. Vui lòng thử lại.");
                return;
            }
            dialog.dismiss();
            if(added!=null)added.onAdded(currentProduct,count);
        });
        UiMotion.press(minus);
        UiMotion.press(plus);
        UiMotion.press(confirm);
        UiMotion.press(close);
        render.run();
        dialog.setOnShowListener(ignored -> {
            FrameLayout sheet=dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if(sheet!=null)sheet.setBackgroundColor(Color.TRANSPARENT);
            dialog.getBehavior().setSkipCollapsed(true);
            dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        dialog.show();
        return dialog;
    }

    /**
     * MainActivity is a plain Activity, so the dialog owns a short-lived lifecycle
     * registration instead of depending on LifecycleOwner or an external listener.
     * In particular, font-size/rotation recreation must remove the old window
     * before Android tears down the old Activity's window manager.
     */
    private static final class OwnedBottomSheetDialog extends BottomSheetDialog
            implements Application.ActivityLifecycleCallbacks {
        private final Activity owner;
        private boolean registered;

        OwnedBottomSheetDialog(Activity owner) {
            super(owner);
            this.owner=owner;
            setOwnerActivity(owner);
            setDismissWithAnimation(false);
        }

        @Override protected void onStart() {
            super.onStart();
            if(!registered) {
                owner.getApplication().registerActivityLifecycleCallbacks(this);
                registered=true;
            }
        }

        private void unregister() {
            if(registered) {
                owner.getApplication().unregisterActivityLifecycleCallbacks(this);
                registered=false;
            }
        }

        @Override public void dismiss() {
            // Do not defer removal through a hide animation during Activity stop.
            setDismissWithAnimation(false);
            unregister();
            super.dismiss();
        }

        @Override public void cancel() {
            setDismissWithAnimation(false);
            unregister();
            super.cancel();
        }

        @Override protected void onStop() {
            unregister();
            super.onStop();
        }

        @Override public void onActivityStopped(Activity activity) {
            if(activity==owner)dismiss();
        }

        @Override public void onActivityPreDestroyed(Activity activity) {
            if(activity==owner)dismiss();
        }

        @Override public void onActivityDestroyed(Activity activity) {
            if(activity==owner)dismiss();
        }

        @Override public void onActivityCreated(Activity activity,Bundle state) { }
        @Override public void onActivityStarted(Activity activity) { }
        @Override public void onActivityResumed(Activity activity) { }
        @Override public void onActivityPaused(Activity activity) { }
        @Override public void onActivitySaveInstanceState(Activity activity,Bundle state) { }
    }
}
