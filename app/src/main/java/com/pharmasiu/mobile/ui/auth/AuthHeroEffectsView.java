package com.pharmasiu.mobile.ui.auth;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;

/** Decorative hero light, deliberately confined away from the left-hand copy. */
public final class AuthHeroEffectsView extends View {
    private static final long FRAME_MS = 33L;
    private static final float TAU = (float) (Math.PI * 2d);
    private static final int CYAN = 0xFF8CE7ED;
    private static final int EMERALD = 0xFF67D5B4;
    private static final float[] GLINT_X = {.62f, .88f, .95f, .73f, .84f};
    private static final float[] GLINT_Y = {.17f, .12f, .59f, .64f, .89f};

    private final Paint light = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect visibleBounds = new Rect();
    private final RectF orbitBounds = new RectF();
    private final Matrix glowMatrix = new Matrix();
    private final Path heartbeat = new Path();
    private final Path heartbeatHighlight = new Path();
    private final PathMeasure heartbeatMeasure = new PathMeasure();
    private final float[] heartbeatPoint = new float[2];
    private final RadialGradient glowShader = new RadialGradient(0f, 0f, 1f,
        new int[]{0x3694EFE2, 0x0067D5B4}, null, Shader.TileMode.CLAMP);
    private final long startedAt = SystemClock.elapsedRealtime();
    private final float density;
    private final AccessibilityManager accessibility;
    private boolean attached;
    private boolean frameScheduled;
    private boolean settingsObserved;
    private boolean accessibilityObserved;
    private float durationScale = 1f;
    private float heartbeatLength;
    private ViewTreeObserver observedTree;

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!canRun()) {
                invalidate();
                return;
            }
            postInvalidateOnAnimation();
            frameScheduled = true;
            postDelayed(this, FRAME_MS);
        }
    };
    private final ViewTreeObserver.OnScrollChangedListener scrollChanged = this::updateAnimationState;
    private final ViewTreeObserver.OnGlobalLayoutListener layoutChanged = this::updateAnimationState;
    private final AccessibilityManager.TouchExplorationStateChangeListener explorationChanged =
        enabled -> updateAnimationState();
    private final ContentObserver animationSettings = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override public void onChange(boolean selfChange) {
            readDurationScale();
            updateAnimationState();
        }
    };

    public AuthHeroEffectsView(Context context) { this(context, null); }

    public AuthHeroEffectsView(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public AuthHeroEffectsView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        density = getResources().getDisplayMetrics().density;
        accessibility = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        setClickable(false);
        setLongClickable(false);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        stroke.setStrokeJoin(Paint.Join.ROUND);
        glow.setShader(glowShader);
        readDurationScale();
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        orbitBounds.set(width * .63f, height * .18f, width * .95f, height * .68f);
        float left = width * .64f;
        float span = width * .32f;
        float baseline = height * .82f;
        float amplitude = height * .065f;
        heartbeat.reset();
        heartbeat.moveTo(left, baseline);
        heartbeat.lineTo(left + span * .22f, baseline);
        heartbeat.lineTo(left + span * .31f, baseline - amplitude * .32f);
        heartbeat.lineTo(left + span * .39f, baseline + amplitude * .35f);
        heartbeat.lineTo(left + span * .49f, baseline - amplitude);
        heartbeat.lineTo(left + span * .59f, baseline + amplitude * .55f);
        heartbeat.lineTo(left + span * .69f, baseline);
        heartbeat.lineTo(left + span, baseline);
        heartbeatMeasure.setPath(heartbeat, false);
        heartbeatLength = heartbeatMeasure.getLength();
        updateAnimationState();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canRun()) return;
        float seconds = (SystemClock.elapsedRealtime() - startedAt) / (1_000f * durationScale);
        int saved = canvas.save();
        // The left side contains the hero's readable message and remains completely clear.
        canvas.clipRect(getWidth() * .56f, getHeight() * .04f, getWidth() * .99f, getHeight() * .96f);

        stroke.setStrokeWidth(density);
        stroke.setColor(CYAN);
        stroke.setAlpha(22);
        canvas.drawOval(orbitBounds, stroke);
        float orbitAngle = seconds * .27f;
        stroke.setAlpha(74);
        canvas.drawArc(orbitBounds, orbitAngle * 180f / (float) Math.PI, 40f, false, stroke);
        stroke.setColor(EMERALD);
        stroke.setAlpha(52);
        canvas.drawArc(orbitBounds, orbitAngle * 180f / (float) Math.PI + 185f, 27f, false, stroke);
        float radiusX = orbitBounds.width() * .5f;
        float radiusY = orbitBounds.height() * .5f;
        float cyanX = orbitBounds.centerX() + radiusX * (float) Math.cos(orbitAngle);
        float cyanY = orbitBounds.centerY() + radiusY * (float) Math.sin(orbitAngle);
        halo(canvas, cyanX, cyanY, 15f * density);
        light.setColor(CYAN);
        light.setAlpha(154);
        canvas.drawCircle(cyanX, cyanY, 1.8f * density, light);
        float greenX = orbitBounds.centerX() + radiusX * (float) Math.cos(orbitAngle + Math.PI);
        float greenY = orbitBounds.centerY() + radiusY * (float) Math.sin(orbitAngle + Math.PI);
        light.setColor(EMERALD);
        light.setAlpha(112);
        canvas.drawCircle(greenX, greenY, 1.5f * density, light);

        for (int i = 0; i < GLINT_X.length; i++) {
            float phase = seconds * .5f + i * 1.7f;
            float twinkle = .5f + .5f * (float) Math.sin(phase);
            float x = getWidth() * GLINT_X[i] + (float) Math.sin(phase * .61f) * density * 3f;
            float y = getHeight() * GLINT_Y[i] + (float) Math.cos(phase * .47f) * density * 4f;
            light.setColor(i % 2 == 0 ? CYAN : EMERALD);
            light.setAlpha(38 + (int) (60f * twinkle));
            canvas.drawCircle(x, y, density * (.8f + twinkle * .45f), light);
            if (i == 0 || i == 3) {
                float arm = density * (2f + twinkle);
                stroke.setColor(CYAN);
                stroke.setAlpha(30 + (int) (25f * twinkle));
                stroke.setStrokeWidth(density * .8f);
                canvas.drawLine(x - arm, y, x + arm, y, stroke);
                canvas.drawLine(x, y - arm, x, y + arm, stroke);
            }
        }

        float beat = .5f + .5f * (float) Math.sin(seconds * TAU / 3.2f);
        stroke.setStrokeWidth(density * 1.15f);
        stroke.setColor(EMERALD);
        stroke.setAlpha(22 + (int) (beat * 15f));
        canvas.drawPath(heartbeat, stroke);
        if (heartbeatLength > 0f) {
            float progress = (seconds / 4.6f) % 1f;
            float end = heartbeatLength * progress;
            heartbeatHighlight.reset();
            heartbeatMeasure.getSegment(Math.max(0f, end - heartbeatLength * .24f), end,
                heartbeatHighlight, true);
            stroke.setColor(CYAN);
            stroke.setAlpha(68);
            canvas.drawPath(heartbeatHighlight, stroke);
            if (heartbeatMeasure.getPosTan(end, heartbeatPoint, null)) {
                light.setColor(CYAN);
                light.setAlpha(96);
                canvas.drawCircle(heartbeatPoint[0], heartbeatPoint[1], density * 1.3f, light);
            }
        }
        canvas.restoreToCount(saved);
    }

    private void halo(Canvas canvas, float x, float y, float radius) {
        glowMatrix.setScale(radius, radius);
        glowMatrix.postTranslate(x, y);
        glowShader.setLocalMatrix(glowMatrix);
        canvas.drawCircle(x, y, radius, glow);
    }

    private boolean canRun() {
        return attached && getWidth() > 0 && getHeight() > 0 && isShown()
            && getWindowVisibility() == VISIBLE && hasWindowFocus()
            && durationScale > 0f && ValueAnimator.areAnimatorsEnabled()
            && (accessibility == null || !accessibility.isTouchExplorationEnabled())
            && getGlobalVisibleRect(visibleBounds);
    }

    private void updateAnimationState() {
        if (frame == null) return;
        if (canRun()) {
            if (!frameScheduled) {
                frameScheduled = true;
                postOnAnimation(frame);
            }
        } else {
            boolean wasScheduled = frameScheduled;
            frameScheduled = false;
            removeCallbacks(frame);
            if (wasScheduled) invalidate();
        }
    }

    private void readDurationScale() {
        try {
            durationScale = Settings.Global.getFloat(getContext().getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        } catch (RuntimeException unavailable) {
            durationScale = 1f;
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        readDurationScale();
        observedTree = getViewTreeObserver();
        observedTree.addOnScrollChangedListener(scrollChanged);
        observedTree.addOnGlobalLayoutListener(layoutChanged);
        if (accessibility != null)
            accessibilityObserved = accessibility.addTouchExplorationStateChangeListener(explorationChanged);
        try {
            getContext().getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, animationSettings);
            settingsObserved = true;
        } catch (RuntimeException unavailable) {
            settingsObserved = false;
        }
        updateAnimationState();
    }

    @Override protected void onDetachedFromWindow() {
        attached = false;
        frameScheduled = false;
        removeCallbacks(frame);
        if (observedTree != null && observedTree.isAlive()) {
            observedTree.removeOnScrollChangedListener(scrollChanged);
            observedTree.removeOnGlobalLayoutListener(layoutChanged);
        }
        observedTree = null;
        if (accessibilityObserved && accessibility != null)
            accessibility.removeTouchExplorationStateChangeListener(explorationChanged);
        accessibilityObserved = false;
        if (settingsObserved) getContext().getContentResolver().unregisterContentObserver(animationSettings);
        settingsObserved = false;
        super.onDetachedFromWindow();
    }

    @Override protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateAnimationState();
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateAnimationState();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateAnimationState();
    }
}
