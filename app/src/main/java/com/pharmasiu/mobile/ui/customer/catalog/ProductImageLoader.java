package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.View;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pharmasiu.mobile.R;

import com.pharmasiu.mobile.core.DemoState;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Loads bundled real product assets with an in-memory LRU cache and safe fallback. */
public final class ProductImageLoader {
    private ProductImageLoader() {}
    private static final int MAX_BITMAP_EDGE = 1280;
    private static final int THUMBNAIL_EDGE = 512;
    private static final String[] IMAGE_EXTENSIONS = {".png", ".webp", ".jpg", ".jpeg"};
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> THUMBNAILS = new LruCache<String, Bitmap>(4 * 1024) {
        @Override protected int sizeOf(String key, Bitmap bitmap) {
            return Math.max(1, bitmap.getByteCount() / 1024);
        }
    };
    private static final LruCache<String, Boolean> MISSING_THUMBNAILS = new LruCache<>(128);
    private static final ThreadPoolExecutor THUMBNAIL_WORKERS = new ThreadPoolExecutor(
        2, 2, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(12), runnable -> {
            Thread thread = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                runnable.run();
            }, "product-thumbnail");
            thread.setDaemon(true);
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());

    static { THUMBNAIL_WORKERS.allowCoreThreadTimeOut(true); }

    private static final LruCache<String, Bitmap> BITMAP_CACHE = new LruCache<String, Bitmap>(8 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    public static Bitmap loadBitmap(Context context, String productId) {
        if (productId == null || productId.trim().isEmpty() || context == null) return null;
        String key = productId.trim().toUpperCase(Locale.ROOT);
        // This asset duplicates OTC-004 (plain Hapacol), a different catalog variant.
        // Keep an honest missing state until a matching Hapacol C photo is supplied.
        if ("OTC-015".equals(key)) return null;
        Bitmap cached = BITMAP_CACHE.get(key);
        if (cached != null) return cached;

        for (String ext : IMAGE_EXTENSIONS) {
            String asset = "products/" + key + ext;
            try {
                Bitmap bm = decodeAsset(context, asset, MAX_BITMAP_EDGE);
                if (bm != null) {
                    BITMAP_CACHE.put(key, bm);
                    return bm;
                }
            } catch (Exception | OutOfMemoryError ignored) {
                // Try next extension
            }
        }
        return null;
    }

    private static Bitmap decodeAsset(Context context, String asset, int maximumEdge) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = context.getAssets().open(asset)) {
            BitmapFactory.decodeStream(stream, null, options);
        }
        if (options.outWidth <= 0 || options.outHeight <= 0) return null;
        int sample = 1;
        while (Math.max(options.outWidth / sample, options.outHeight / sample) > maximumEdge
                && sample <= (1 << 20)) sample *= 2;
        options.inSampleSize = sample;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try (InputStream stream = context.getAssets().open(asset)) {
            return BitmapFactory.decodeStream(stream, null, options);
        }
    }

    public static View createArtwork(Activity activity, DemoState.Product p) {
        if (activity == null || p == null) return new View(activity);
        Bitmap bm = loadBitmap(activity, p.id);
        if (bm != null) {
            ImageView iv = new ImageView(activity);
            iv.setImageBitmap(bm);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setBackgroundColor(0xFFFFFFFF);
            int pad = Math.round(10 * activity.getResources().getDisplayMetrics().density);
            iv.setPadding(pad, pad, pad, pad);
            return iv;
        }
        return createMissingArtwork(activity, p);
    }

    /** Thumbnail lists: no asset reads or bitmap decoding on the UI thread. */
    public static View createThumbnail(Activity activity, DemoState.Product product) {
        return createGridArtwork(activity, product);
    }

    /** Catalog grid entry point retained for existing callers. */
    static View createGridArtwork(Activity activity, DemoState.Product product) {
        if (product == null || product.id == null || product.id.trim().isEmpty())
            return new View(activity);
        return new GridArtwork(activity, product);
    }

    /**
     * Submit only after a visible draw. ScrollView attaches all of its cards at once,
     * including offscreen cards, so attachment alone must not queue the whole catalog.
     */
    private static final class GridArtwork extends FrameLayout {
        private final DemoState.Product product;
        private final String key;
        private final Context application;
        private final ImageView image;
        private final Rect visibleBounds = new Rect();
        private final Runnable startRequest = this::requestThumbnail;
        private final ViewTreeObserver.OnScrollChangedListener scrollListener = this::scheduleIfVisible;
        private ViewTreeObserver observedTree;
        private ThumbRequest request;
        private FutureTask<Void> pending;
        private boolean requestPosted;
        private boolean resolved;

        GridArtwork(Activity activity, DemoState.Product product) {
            super(activity);
            this.product = product;
            key = product.id.trim().toUpperCase(Locale.ROOT);
            application = activity.getApplicationContext();
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            setBackgroundColor(activity.getColor(R.color.paper));
            image = new ImageView(activity);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            image.setImageResource(R.drawable.staff_media_missing);
            addView(image, new FrameLayout.LayoutParams(-1, -1));
            Bitmap cached = THUMBNAILS.get(key);
            if (cached != null) showBitmap(cached);
            else if ("OTC-015".equals(key) || MISSING_THUMBNAILS.get(key) != null) showMissing();
        }

        @Override protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            scheduleIfVisible();
        }

        private void scheduleIfVisible() {
            if (!resolved && request == null && !requestPosted && isAttachedToWindow()
                    && getGlobalVisibleRect(visibleBounds)) {
                requestPosted = true;
                postOnAnimation(startRequest);
            }
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (!resolved) {
                observedTree = getViewTreeObserver();
                observedTree.addOnScrollChangedListener(scrollListener);
                scheduleIfVisible();
            }
        }

        private void stopObservingScroll() {
            if (observedTree != null && observedTree.isAlive())
                observedTree.removeOnScrollChangedListener(scrollListener);
            observedTree = null;
        }

        private void requestThumbnail() {
            requestPosted = false;
            if (resolved || request != null || !isAttachedToWindow()
                    || !getGlobalVisibleRect(visibleBounds)) return;
            Bitmap cached = THUMBNAILS.get(key);
            if (cached != null) { showBitmap(cached); return; }
            if (MISSING_THUMBNAILS.get(key) != null || application == null) { showMissing(); return; }
            ThumbRequest next = new ThumbRequest(application, key, this);
            FutureTask<Void> task = new FutureTask<>(next, null);
            request = next;
            pending = task;
            try {
                THUMBNAIL_WORKERS.execute(task);
            } catch (RejectedExecutionException fullQueue) {
                request = null;
                pending = null;
                // A visible card may retry; offscreen/detached cards never fill the queue.
                requestPosted = true;
                postDelayed(startRequest, 80);
            }
        }

        private void showBitmap(Bitmap bitmap) {
            resolved = true;
            stopObservingScroll();
            setBackgroundColor(0xFFFFFFFF);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding = Math.round(10 * getResources().getDisplayMetrics().density);
            image.setPadding(padding, padding, padding, padding);
            image.setImageBitmap(bitmap);
        }

        private void showMissing() {
            resolved = true;
            stopObservingScroll();
            removeAllViews();
            addView(createMissingArtwork((Activity) getContext(), product),
                new FrameLayout.LayoutParams(-1, -1));
        }

        @Override protected void onDetachedFromWindow() {
            stopObservingScroll();
            removeCallbacks(startRequest);
            requestPosted = false;
            if (request != null) request.cancelled.set(true);
            if (pending != null) {
                pending.cancel(false);
                THUMBNAIL_WORKERS.remove(pending);
            }
            pending = null;
            request = null;
            super.onDetachedFromWindow();
        }
    }

    /** Holds no Activity or strong View reference while decoding on a worker. */
    private static final class ThumbRequest implements Runnable {
        private final Context application;
        private final String key;
        private final WeakReference<GridArtwork> target;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        ThumbRequest(Context application, String key, GridArtwork target) {
            this.application = application;
            this.key = key;
            this.target = new WeakReference<>(target);
        }

        @Override public void run() {
            if (cancelled.get()) return;
            Bitmap cached = THUMBNAILS.get(key);
            ThumbResult result = cached == null ? decodeThumbnail(application, key)
                : new ThumbResult(cached, false);
            if (result.bitmap != null) THUMBNAILS.put(key, result.bitmap);
            else if (result.permanentlyMissing) MISSING_THUMBNAILS.put(key, true);
            if (cancelled.get()) return;
            MAIN.post(() -> {
                GridArtwork view = target.get();
                if (view == null || cancelled.get() || view.request != this || !view.isAttachedToWindow()) return;
                view.request = null;
                view.pending = null;
                if (result.bitmap != null) view.showBitmap(result.bitmap);
                else view.showMissing();
            });
        }
    }

    private static final class ThumbResult {
        final Bitmap bitmap;
        final boolean permanentlyMissing;
        ThumbResult(Bitmap bitmap, boolean permanentlyMissing) {
            this.bitmap = bitmap;
            this.permanentlyMissing = permanentlyMissing;
        }
    }

    private static ThumbResult decodeThumbnail(Context application, String key) {
        for (String extension : IMAGE_EXTENSIONS) {
            String asset = "products/" + key + extension;
            try {
                Bitmap bitmap = decodeAsset(application, asset, THUMBNAIL_EDGE);
                if (bitmap != null) return new ThumbResult(bitmap, false);
            } catch (IOException missingAsset) {
                // Try the next supported bundled extension.
            } catch (RuntimeException | OutOfMemoryError transientFailure) {
                return new ThumbResult(null, false);
            }
        }
        return new ThumbResult(null, true);
    }

    public static View createMissingArtwork(Activity activity, DemoState.Product product) {
        LinearLayout missing = new LinearLayout(activity);
        missing.setOrientation(LinearLayout.VERTICAL);
        missing.setGravity(Gravity.CENTER);
        missing.setBackgroundColor(activity.getColor(R.color.paper));
        int padding = Math.round(4 * activity.getResources().getDisplayMetrics().density);
        missing.setPadding(padding,padding,padding,padding);
        ImageView icon = new ImageView(activity);
        icon.setImageResource(R.drawable.staff_media_missing);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        int size = Math.round(28 * activity.getResources().getDisplayMetrics().density);
        missing.addView(icon,new LinearLayout.LayoutParams(size,size));
        TextView label = new TextView(activity);
        label.setText("Chưa có ảnh"); label.setTextSize(12);
        label.setTextColor(activity.getColor(R.color.muted)); label.setGravity(Gravity.CENTER);
        missing.addView(label,new LinearLayout.LayoutParams(-1,-2));
        missing.setContentDescription("Chưa có ảnh phù hợp cho " + product.name + " · " + product.id);
        return missing;
    }
}
