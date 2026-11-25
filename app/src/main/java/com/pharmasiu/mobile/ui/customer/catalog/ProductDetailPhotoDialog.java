package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.google.android.material.button.MaterialButton;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.ui.design.UiMotion;

/** A single real catalog photo; no synthetic gallery pages or staff-only imagery. */
final class ProductDetailPhotoDialog extends Dialog implements Application.ActivityLifecycleCallbacks {
    private final Activity owner;
    private boolean registered;

    private ProductDetailPhotoDialog(Activity activity) {
        super(activity);
        owner = activity;
        setOwnerActivity(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static Dialog show(Activity activity, DemoState.Product product) {
        if (activity.isFinishing() || activity.isDestroyed()) return null;
        View artwork = ProductImageLoader.createArtwork(activity, product);
        if (!(artwork instanceof ImageView)) return null;
        Drawable picture = ((ImageView) artwork).getDrawable();
        if (picture == null) return null;

        ProductDetailPhotoDialog dialog = new ProductDetailPhotoDialog(activity);
        LinearLayout page = new LinearLayout(activity);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.WHITE);
        ViewCompat.setAccessibilityPaneTitle(page, "Ảnh " + product.name);
        LinearLayout toolbar = new LinearLayout(activity);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(activity, 12), dp(activity, 8), dp(activity, 16), dp(activity, 8));
        ImageView close = new ImageView(activity);
        close.setImageResource(R.drawable.ic_arrow_back);
        close.setColorFilter(activity.getColor(R.color.emerald));
        close.setBackgroundResource(R.drawable.catalog_detail_icon_button);
        close.setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12));
        close.setContentDescription("Đóng ảnh sản phẩm");
        close.setFocusable(true);
        close.setOnClickListener(view -> dialog.dismiss());
        UiMotion.press(close);
        toolbar.addView(close, new LinearLayout.LayoutParams(dp(activity, 48), dp(activity, 48)));
        TextView title = new TextView(activity);
        title.setText(product.name);
        title.setTextSize(16);
        title.setTextColor(activity.getColor(R.color.forest));
        title.setPadding(dp(activity, 12), 0, 0, 0);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        page.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));

        ZoomPhoto photo = new ZoomPhoto(activity, picture);
        photo.setContentDescription("Ảnh " + product.name + ". Chụm hai ngón để phóng to, kéo để xem.");
        page.addView(photo, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout controls = new LinearLayout(activity);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setPadding(dp(activity, 16), dp(activity, 8), dp(activity, 16), dp(activity, 16));
        MaterialButton minus = control(activity, "−", "Thu nhỏ ảnh");
        controls.addView(minus, new LinearLayout.LayoutParams(dp(activity, 52), dp(activity, 52)));
        TextView amount = new TextView(activity);
        amount.setGravity(Gravity.CENTER);
        amount.setTextSize(15);
        amount.setTextColor(activity.getColor(R.color.forest));
        controls.addView(amount, new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton plus = control(activity, "+", "Phóng to ảnh");
        controls.addView(plus, new LinearLayout.LayoutParams(dp(activity, 52), dp(activity, 52)));
        MaterialButton reset = control(activity, "Đặt lại", "Đưa ảnh về kích thước ban đầu");
        reset.setTextSize(14);
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(-2, -2);
        resetParams.setMarginStart(dp(activity, 12));
        controls.addView(reset, resetParams);
        page.addView(controls, new LinearLayout.LayoutParams(-1, -2));
        photo.onZoom = () -> {
            amount.setText(Math.round(photo.zoom * 100) + "%");
            minus.setEnabled(photo.zoom > 1.01f);
            plus.setEnabled(photo.zoom < 3.99f);
        };
        minus.setOnClickListener(view -> photo.zoomTo(photo.zoom / 1.5f, photo.getWidth() * .5f, photo.getHeight() * .5f));
        plus.setOnClickListener(view -> photo.zoomTo(photo.zoom * 1.5f, photo.getWidth() * .5f, photo.getHeight() * .5f));
        reset.setOnClickListener(view -> photo.fit());
        photo.onZoom.run();
        dialog.setContentView(page);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        return dialog;
    }

    private static int dp(Activity activity, float value) {
        return Math.round(activity.getResources().getDisplayMetrics().density * value);
    }

    private static MaterialButton control(Activity activity, String label, String description) {
        MaterialButton button = new MaterialButton(activity);
        button.setText(label);
        button.setTextSize(22);
        button.setTextColor(new android.content.res.ColorStateList(
            new int[][]{new int[]{-android.R.attr.state_enabled}, new int[0]},
            new int[]{activity.getColor(R.color.muted), activity.getColor(R.color.forest)}));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activity.getColor(R.color.mint)));
        button.setContentDescription(description);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(dp(activity, 52));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setPadding(dp(activity, 12), dp(activity, 8), dp(activity, 12), dp(activity, 8));
        button.setCornerRadius(dp(activity, 16));
        UiMotion.press(button);
        return button;
    }

    @Override protected void onStart() {
        super.onStart();
        if (!registered) {
            owner.getApplication().registerActivityLifecycleCallbacks(this);
            registered = true;
        }
    }

    private void unregister() {
        if (registered) {
            owner.getApplication().unregisterActivityLifecycleCallbacks(this);
            registered = false;
        }
    }

    @Override public void dismiss() { unregister(); super.dismiss(); }
    @Override public void cancel() { unregister(); super.cancel(); }
    @Override protected void onStop() { unregister(); super.onStop(); }
    @Override public void onActivityStopped(Activity activity) { if (activity == owner) dismiss(); }
    @Override public void onActivityPreDestroyed(Activity activity) { if (activity == owner) dismiss(); }
    @Override public void onActivityDestroyed(Activity activity) { if (activity == owner) dismiss(); }
    @Override public void onActivityCreated(Activity activity, Bundle state) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityResumed(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }

    private static final class ZoomPhoto extends ImageView {
        private final Matrix transform = new Matrix();
        private final float[] values = new float[9];
        private final ScaleGestureDetector scaling;
        private final GestureDetector taps;
        private float zoom = 1f;
        private float lastX, lastY;
        private Runnable onZoom = () -> { };

        ZoomPhoto(Activity activity, Drawable picture) {
            super(activity);
            setScaleType(ScaleType.MATRIX);
            setImageDrawable(picture);
            scaling = new ScaleGestureDetector(activity, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector detector) {
                    zoomTo(zoom * detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                    return true;
                }
            });
            taps = new GestureDetector(activity, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onDown(MotionEvent event) { return true; }
                @Override public boolean onDoubleTap(MotionEvent event) {
                    if (zoom > 1.01f) fit(); else zoomTo(2f, event.getX(), event.getY());
                    return true;
                }
                @Override public boolean onSingleTapConfirmed(MotionEvent event) { return performClick(); }
            });
        }

        @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
            super.onSizeChanged(w, h, oldW, oldH);
            fit();
        }

        private void fit() {
            Drawable picture = getDrawable();
            if (picture == null || getWidth() <= 0 || getHeight() <= 0
                    || picture.getIntrinsicWidth() <= 0 || picture.getIntrinsicHeight() <= 0) return;
            float scale = Math.min(getWidth() / (float) picture.getIntrinsicWidth(),
                getHeight() / (float) picture.getIntrinsicHeight());
            transform.setScale(scale, scale);
            transform.postTranslate((getWidth() - picture.getIntrinsicWidth() * scale) * .5f,
                (getHeight() - picture.getIntrinsicHeight() * scale) * .5f);
            zoom = 1f;
            setImageMatrix(transform);
            onZoom.run();
        }

        private void zoomTo(float requested, float focusX, float focusY) {
            if (getWidth() <= 0 || getHeight() <= 0) return;
            float next = Math.max(1f, Math.min(4f, requested));
            float factor = next / zoom;
            transform.postScale(factor, factor, focusX, focusY);
            zoom = next;
            constrain();
            onZoom.run();
        }

        private void constrain() {
            Drawable picture = getDrawable();
            if (picture == null) return;
            transform.getValues(values);
            float width = picture.getIntrinsicWidth() * values[Matrix.MSCALE_X];
            float height = picture.getIntrinsicHeight() * values[Matrix.MSCALE_Y];
            float x = values[Matrix.MTRANS_X], y = values[Matrix.MTRANS_Y];
            float nextX = width <= getWidth() ? (getWidth() - width) * .5f
                : Math.max(getWidth() - width, Math.min(0f, x));
            float nextY = height <= getHeight() ? (getHeight() - height) * .5f
                : Math.max(getHeight() - height, Math.min(0f, y));
            transform.postTranslate(nextX - x, nextY - y);
            setImageMatrix(transform);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            scaling.onTouchEvent(event);
            taps.onTouchEvent(event);
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1
                    && !scaling.isInProgress() && zoom > 1f) {
                transform.postTranslate(event.getX() - lastX, event.getY() - lastY);
                constrain();
            }
            int pointer = event.getActionMasked() == MotionEvent.ACTION_POINTER_UP
                && event.getActionIndex() == 0 && event.getPointerCount() > 1 ? 1 : 0;
            lastX = event.getX(pointer);
            lastY = event.getY(pointer);
            return true;
        }

        @Override public boolean performClick() { super.performClick(); return true; }
    }
}
