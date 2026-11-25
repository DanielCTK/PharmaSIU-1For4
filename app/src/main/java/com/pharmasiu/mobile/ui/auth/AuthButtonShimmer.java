package com.pharmasiu.mobile.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.pharmasiu.mobile.ui.design.UiMotion;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/** One gentle capsule sheen; never changes the button's input or enabled state. */
public final class AuthButtonShimmer {
    private static final WeakHashMap<View, Sweep> ACTIVE = new WeakHashMap<>();

    private AuthButtonShimmer() { }

    /** Explicitly play once, including an intro on a currently disabled button. */
    public static void play(View button) {
        if (button == null) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            button.post(() -> play(button));
            return;
        }
        Sweep previous = ACTIVE.get(button);
        if (previous != null) previous.finish();
        if (!UiMotion.canAnimate(button)) return;
        Sweep sweep = new Sweep(button);
        ACTIVE.put(button, sweep);
        button.addOnAttachStateChangeListener(sweep);
        if (button.isAttachedToWindow()) button.post(sweep.begin);
    }

    private static final class Sweep implements View.OnAttachStateChangeListener,
            View.OnLayoutChangeListener {
        private final WeakReference<View> owner;
        private final Runnable begin = this::start;
        private Drawable originalForeground;
        private Drawable overlay;
        private ValueAnimator animator;
        private boolean finished;
        private boolean started;

        Sweep(View button) { owner = new WeakReference<>(button); }

        private void start() {
            if (finished || started) return;
            View button = owner.get();
            if (button == null || !button.isAttachedToWindow() || !button.isShown()
                    || !UiMotion.canAnimate(button)) {
                finish();
                return;
            }
            if (button.getWidth() == 0 || button.getHeight() == 0) {
                button.removeOnLayoutChangeListener(this);
                button.addOnLayoutChangeListener(this);
                return;
            }
            started = true;
            button.removeOnLayoutChangeListener(this);
            originalForeground = button.getForeground();
            Sheen sheen = new Sheen();
            overlay = originalForeground == null ? sheen
                : new LayerDrawable(new Drawable[]{originalForeground, sheen});
            button.setForeground(overlay);
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(900);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(animation -> {
                View current = owner.get();
                if (current == null || !current.isAttachedToWindow() || !current.isShown()
                        || !UiMotion.canAnimate(current) || current.getForeground() != overlay) {
                    finish();
                    return;
                }
                sheen.progress = (float) animation.getAnimatedValue();
                sheen.invalidateSelf();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) { finish(); }
            });
            animator.start();
        }

        private void finish() {
            if (finished) return;
            finished = true;
            if (animator != null) {
                animator.removeAllUpdateListeners();
                animator.removeAllListeners();
                animator.cancel();
            }
            View button = owner.get();
            if (button != null) {
                button.removeCallbacks(begin);
                button.removeOnAttachStateChangeListener(this);
                button.removeOnLayoutChangeListener(this);
                if (overlay != null && button.getForeground() == overlay)
                    button.setForeground(originalForeground);
                if (ACTIVE.get(button) == this) ACTIVE.remove(button);
            }
            overlay = null;
            originalForeground = null;
        }

        @Override public void onViewAttachedToWindow(View button) { button.post(begin); }
        @Override public void onViewDetachedFromWindow(View button) { finish(); }
        @Override public void onLayoutChange(View view, int l, int t, int r, int b,
                int oldL, int oldT, int oldR, int oldB) { start(); }
    }

    private static final class Sheen extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path capsule = new Path();
        private final RectF area = new RectF();
        private float bandWidth;
        private float progress;
        private int alpha = 255;

        @Override protected void onBoundsChange(Rect bounds) {
            area.set(bounds);
            capsule.reset();
            float radius = bounds.height() * .5f;
            capsule.addRoundRect(area, radius, radius, Path.Direction.CW);
            bandWidth = Math.max(1f, bounds.width() * .28f);
            paint.setShader(new LinearGradient(0f, 0f, bandWidth, 0f,
                new int[]{0x00FFFFFF, 0x22FFFFFF, 0x00FFFFFF},
                new float[]{0f, .5f, 1f}, Shader.TileMode.CLAMP));
        }

        @Override public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            if (bounds.isEmpty()) return;
            float offset = -bandWidth
                + (bounds.width() + bandWidth + bounds.height() * .45f) * progress;
            paint.setAlpha(Math.round(alpha * (float) Math.sin(Math.PI * progress)));
            int save = canvas.save();
            canvas.clipPath(capsule);
            canvas.translate(bounds.left + offset, bounds.top);
            canvas.skew(-.45f, 0f);
            canvas.drawRect(0f, 0f, bandWidth, bounds.height(), paint);
            canvas.restoreToCount(save);
        }

        @Override public void setAlpha(int alpha) { this.alpha = alpha; invalidateSelf(); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
