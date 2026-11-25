package com.pharmasiu.mobile.ui.design;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;

/** Short property animations keep movement independent of layout. */
public final class UiMotion {
    private static final DecelerateInterpolator GLIDE = new DecelerateInterpolator(1.6f);

    private UiMotion() { }

    public static int horizontalDirection(View container, int previous, int next) {
        int direction = Integer.compare(next, previous);
        return container.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? -direction : direction;
    }

    public static void slideIn(View target, int direction) {
        target.animate().cancel();
        target.setTranslationX(0f);
        if (direction == 0 || !target.isAttachedToWindow() || !canAnimate(target)) return;
        float distance = 24f * target.getResources().getDisplayMetrics().density;
        target.setTranslationX(direction * distance);
        target.animate().translationX(0f).setDuration(220)
            .setInterpolator(GLIDE).start();
    }

    public static void enterPage(View target, int horizontalDirection, boolean returning) {
        if (!canAnimate(target)) return;
        float density = target.getResources().getDisplayMetrics().density;
        if (horizontalDirection != 0) {
            target.setTranslationX(horizontalDirection * 16f * density);
            target.animate().translationX(0f).setDuration(200).setInterpolator(GLIDE).start();
        } else {
            target.setTranslationY((returning ? -8f : 8f) * density);
            target.animate().translationY(0f).setDuration(180).setInterpolator(GLIDE).start();
        }
    }

    public static boolean canAnimate(View target) {
        AccessibilityManager accessibility = (AccessibilityManager) target.getContext()
            .getSystemService(Context.ACCESSIBILITY_SERVICE);
        return ValueAnimator.areAnimatorsEnabled()
            && (accessibility == null || !accessibility.isTouchExplorationEnabled());
    }

    public static void press(View target) {
        target.setOnTouchListener((view,event) -> {
            int action=event.getActionMasked();
            if (action==MotionEvent.ACTION_DOWN) {
                if (!canAnimate(view) || !view.isEnabled()) return false;
                view.animate().cancel();
                view.animate().scaleX(.97f).scaleY(.97f).setDuration(90).start();
            } else if (action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL) {
                view.animate().cancel();
                if (canAnimate(view)) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                } else {
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }
            }
            // Let the normal View click path retain keyboard/TalkBack behavior.
            return false;
        });
    }

    public static void badge(View target) {
        if (target==null || !target.isAttachedToWindow() || !canAnimate(target)) return;
        target.animate().cancel();
        target.setScaleX(1f);
        target.setScaleY(1f);
        target.animate().scaleX(1.08f).scaleY(1.08f).setDuration(110)
            .withEndAction(() -> target.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
    }
}
