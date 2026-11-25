package com.pharmasiu.mobile.ui.customer.catalog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.ui.design.UiMotion;
import java.util.function.Consumer;

/** Three local slides, five-second rotation, with content-driven stable bounds. */
public final class HomeBannerCarousel extends LinearLayout {
    private static final long INTERVAL_MS = 5_000L;
    private static final int[] PHOTOS = {R.drawable.home_banner_skincare,
        R.drawable.home_banner_fresh, R.drawable.home_banner_care};
    private static final String[] TITLES = {"Chăm sóc mỗi ngày", "Thêm sắc xanh mỗi ngày", "Chăm chút từ điều nhỏ"};
    private static final String[] NOTES = {"Dành thời gian cho bản thân.", "Khám phá thực phẩm và đồ uống.", "Đồ dùng cho thói quen hằng ngày."};
    private static final String[] CATEGORIES = {"Mỹ phẩm", "Thực phẩm", "Chăm sóc"};
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Rect visibleRect = new Rect();
    private final View[] pages = new View[3];
    private final LinearLayout[] copyLayers = new LinearLayout[3];
    private final TextView[] dots = new TextView[3];
    private final MaterialButton[] exploreButtons = new MaterialButton[3];
    private FrameLayout stage;
    private LinearLayout controls;
    private int index;
    private ValueAnimator slideAnimator;
    private int animationGeneration;
    private boolean attached, touching;
    private float downX, downY;
    private Consumer<String> onExplore = category -> { };
    private final ViewTreeObserver.OnGlobalFocusChangeListener focusChanges = (oldFocus, newFocus) -> schedule();
    private final Runnable advance = new Runnable() {
        @Override public void run() {
            if (!canRotate()) return;
            if (getGlobalVisibleRect(visibleRect)) showSlide((index + 1) % pages.length, true);
            schedule();
        }
    };

    public HomeBannerCarousel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundColor(context.getColor(R.color.mint));
        setClipToOutline(false);
        stage = new FrameLayout(context) {
            @Override protected void onMeasure(int widthSpec, int heightSpec) {
                // Only text/actions set card height. Bitmap intrinsic size is never a layout input.
                // Measure every slide so long copy cannot resize the banner on each rotation.
                int width = MeasureSpec.getSize(widthSpec);
                int height = dp(168);
                for (LinearLayout copy : copyLayers) {
                    if (copy == null) continue;
                    copy.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                    height = Math.max(height, copy.getMeasuredHeight());
                }
                super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        };
        stage.setClipChildren(true);
        addView(stage, new LayoutParams(-1, -2));
        for (int i = 0; i < pages.length; i++) {
            pages[i] = createPage(i);
            pages[i].setVisibility(INVISIBLE);
            setPageActive(i, false);
            stage.addView(pages[i], new FrameLayout.LayoutParams(-1, -1));
        }
        controls = new LinearLayout(context);
        controls.setGravity(Gravity.CENTER);
        // 48dp touch targets overlay the photo instead of adding another control bar.
        for (int i = 0; i < dots.length; i++) {
            final int position = i;
            TextView dot = new TextView(context);
            dot.setGravity(Gravity.CENTER);
            dot.setTextSize(18);
            dot.setFocusable(true);
            dot.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(0x18065640), null, null));
            dot.setOnClickListener(v -> { showSlide(position, true); schedule(); });
            controls.addView(dot, new LayoutParams(dp(48), dp(48)));
            dots[i] = dot;
        }
        FrameLayout.LayoutParams indicators = new FrameLayout.LayoutParams(dp(144), dp(48), Gravity.BOTTOM | Gravity.END);
        indicators.rightMargin = dp(12); indicators.bottomMargin = dp(14);
        stage.addView(controls, indicators);
        showSlide(0, false);
    }

    public void setOnExplore(Consumer<String> callback) { onExplore = callback; }

    private View createPage(int position) {
        Context context = getContext();
        FrameLayout frame = new FrameLayout(context);
        ImageView photo = new ImageView(context);
        photo.setId(R.id.home_banner_photo);
        photo.setImageResource(PHOTOS[position]);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(photo, new FrameLayout.LayoutParams(-1, -1));
        View shade = new View(context);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{0xF5C4DFD0, 0xDBD8EADF, 0x35E7F1EA}));
        frame.addView(shade, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout copy = new LinearLayout(context);
        copyLayers[position] = copy;
        copy.setOrientation(VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);
        copy.setPadding(dp(16), dp(14), dp(16), dp(14));
        copy.setMinimumHeight(dp(168));
        frame.addView(copy, new FrameLayout.LayoutParams(-1, -1));
        TextView title = text(TITLES[position], 20, context.getColor(R.color.forest), true);
        title.setId(R.id.home_banner_title);
        androidx.core.view.ViewCompat.setAccessibilityHeading(title, true);
        title.setMinLines(2);
        LayoutParams titleParams = new LayoutParams(-1, -2);
        titleParams.rightMargin = dp(86);
        copy.addView(title, titleParams);
        TextView note = text(NOTES[position], 13, context.getColor(R.color.forest), false);
        note.setId(R.id.home_banner_note);
        note.setMinLines(2);
        LayoutParams noteParams = new LayoutParams(-1, -2);
        noteParams.topMargin = dp(4);
        noteParams.rightMargin = dp(60);
        copy.addView(note, noteParams);
        MaterialButton explore = new MaterialButton(context);
        exploreButtons[position] = explore;
        explore.setId(R.id.catalog_v2_home_explore);
        boolean compactAction = getResources().getConfiguration().screenWidthDp < 360
            || getResources().getConfiguration().fontScale >= 1.3f;
        explore.setText(compactAction ? "" : "Khám phá");
        explore.setContentDescription("Khám phá nhóm " + CATEGORIES[position]);
        if (compactAction) { explore.setMinimumWidth(0); explore.setMinWidth(0); explore.setPadding(0,0,0,0); }
        explore.setTextSize(14);
        explore.setAllCaps(false);
        explore.setMinHeight(dp(48));
        explore.setCornerRadius(dp(24));
        explore.setInsetTop(0);
        explore.setInsetBottom(0);
        explore.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.emerald)));
        explore.setTextColor(context.getColor(R.color.white));
        explore.setIconResource(R.drawable.ic_arrow_forward);
        explore.setIconTint(android.content.res.ColorStateList.valueOf(context.getColor(R.color.white)));
        explore.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_END);
        explore.setIconSize(dp(18));
        explore.setOnClickListener(v -> onExplore.accept(CATEGORIES[position]));
        UiMotion.press(explore);
        LayoutParams buttonParams = new LayoutParams(compactAction ? dp(48) : -2, dp(48));
        buttonParams.topMargin = dp(8);
        copy.addView(explore, buttonParams);
        return frame;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView text = new TextView(getContext());
        text.setText(value); text.setTextSize(size); text.setTextColor(color);
        if (bold) text.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return text;
    }

    private void showSlide(int next, boolean animate) {
        if (next == index && pages[index].getVisibility() == VISIBLE) {
            updateIndicators();
            return;
        }
        int previous = index;
        settlePages();
        index = next;
        for (int i = 0; i < pages.length; i++) setPageActive(i, i == index);
        View incoming = pages[index];
        incoming.setVisibility(VISIBLE);
        incoming.bringToFront();
        controls.bringToFront();
        updateIndicators();
        if (!animate || previous == next || !isAttachedToWindow()
            || !UiMotion.canAnimate(this) || stage.getWidth() == 0) {
            settlePages();
            return;
        }

        View outgoing = pages[previous];
        outgoing.setVisibility(VISIBLE);
        // Keep the full outgoing photo opaque until the incoming one covers it.
        // This avoids a flash of the stage's mint background between frames.
        int direction = (next - previous + pages.length) % pages.length == 1 ? 1 : -1;
        float travel = Math.min(dp(32), stage.getWidth() * .09f) * direction;
        incoming.setTranslationX(travel);
        incoming.setAlpha(0f);
        final int generation = animationGeneration;
        slideAnimator = ValueAnimator.ofFloat(0f, 1f);
        slideAnimator.setDuration(260L);
        slideAnimator.setInterpolator(new PathInterpolator(.2f, 0f, 0f, 1f));
        slideAnimator.addUpdateListener(animation -> {
            if (generation != animationGeneration) return;
            float progress = (float) animation.getAnimatedValue();
            incoming.setTranslationX(travel * (1f - progress));
            incoming.setAlpha(progress);
            copyLayers[previous].setAlpha(1f - progress);
        });
        slideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (generation != animationGeneration) return;
                slideAnimator = null;
                settlePages();
            }
        });
        slideAnimator.start();
    }

    /** All pages retain identical bounds; only one owns input and accessibility. */
    private void setPageActive(int position, boolean active) {
        View page = pages[position];
        page.setImportantForAccessibility(active ? IMPORTANT_FOR_ACCESSIBILITY_AUTO
            : IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        ((ViewGroup) page).setDescendantFocusability(active ? ViewGroup.FOCUS_AFTER_DESCENDANTS
            : ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        MaterialButton explore = exploreButtons[position];
        explore.setEnabled(active);
        explore.setFocusable(active);
        if (!active) {
            explore.animate().cancel();
            explore.setScaleX(1f);
            explore.setScaleY(1f);
        }
    }

    /** Cancel before resetting so a stale animation cannot hide a newly selected page. */
    private void settlePages() {
        animationGeneration++;
        if (slideAnimator != null) {
            ValueAnimator old = slideAnimator;
            slideAnimator = null;
            old.removeAllListeners();
            old.removeAllUpdateListeners();
            old.cancel();
        }
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) continue;
            pages[i].setTranslationX(0f);
            pages[i].setAlpha(1f);
            copyLayers[i].setAlpha(1f);
            pages[i].setVisibility(i == index ? VISIBLE : INVISIBLE);
            setPageActive(i, i == index);
        }
        if (controls != null) controls.bringToFront();
    }

    private void updateIndicators() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setText(i == index ? "●" : "○");
            dots[i].setSelected(i == index);
            dots[i].setTextColor(getContext().getColor(R.color.forest));
            dots[i].setContentDescription("Xem ảnh " + (i + 1) + ": " + TITLES[i]
                + (i == index ? ", đang chọn" : ""));
        }
        setContentDescription("Ảnh " + (index + 1) + " trên 3: " + TITLES[index]);
    }

    private void resetPressFeedback() {
        for (MaterialButton button : exploreButtons) {
            if (button == null) continue;
            button.animate().cancel();
            button.setScaleX(1f);
            button.setScaleY(1f);
        }
    }

    private boolean canRotate() {
        AccessibilityManager accessibility = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        return attached && !touching && !hasFocus() && getWindowVisibility() == VISIBLE && hasWindowFocus()
            && (accessibility == null || !accessibility.isTouchExplorationEnabled());
    }
    private void schedule() {
        handler.removeCallbacks(advance);
        if (canRotate()) handler.postDelayed(advance, INTERVAL_MS);
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow(); attached = true;
        getViewTreeObserver().addOnGlobalFocusChangeListener(focusChanges); schedule();
    }
    @Override protected void onDetachedFromWindow() {
        attached = false; touching = false; handler.removeCallbacks(advance);
        if (getViewTreeObserver().isAlive()) getViewTreeObserver().removeOnGlobalFocusChangeListener(focusChanges);
        settlePages();
        resetPressFeedback();
        super.onDetachedFromWindow();
    }
    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (!focused) { settlePages(); resetPressFeedback(); }
        schedule();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (handler != null) {
            if (visibility != VISIBLE) { settlePages(); resetPressFeedback(); }
            schedule();
        }
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            downX = event.getX(); downY = event.getY(); touching = true; schedule();
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            touching = false;
            float delta = event.getX() - downX;
            if (event.getActionMasked() == MotionEvent.ACTION_UP && Math.abs(delta) > dp(48)
                && Math.abs(delta) > Math.abs(event.getY() - downY) * 1.5f) {
                showSlide((index + (delta < 0 ? 1 : pages.length - 1)) % pages.length, true);
                schedule(); return true;
            }
            schedule();
        }
        return super.dispatchTouchEvent(event);
    }
    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            float dx = Math.abs(event.getX() - downX), dy = Math.abs(event.getY() - downY);
            if (dx > ViewConfiguration.get(getContext()).getScaledTouchSlop() && dx > dy * 1.5f) {
                getParent().requestDisallowInterceptTouchEvent(true); return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }
    @Override public boolean onTouchEvent(MotionEvent event) { return true; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
