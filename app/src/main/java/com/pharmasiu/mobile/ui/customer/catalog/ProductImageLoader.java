package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.pharmasiu.mobile.core.DemoState;

import java.io.InputStream;
import java.util.Locale;

/** Loads bundled real product assets with an in-memory LRU cache and safe fallback. */
public final class ProductImageLoader {
    private ProductImageLoader() {}

    private static final LruCache<String, Bitmap> BITMAP_CACHE = new LruCache<String, Bitmap>(32) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    public static Bitmap loadBitmap(Context context, String productId) {
        if (productId == null || productId.trim().isEmpty() || context == null) return null;
        String key = productId.trim().toUpperCase(Locale.ROOT);
        Bitmap cached = BITMAP_CACHE.get(key);
        if (cached != null) return cached;

        String[] extensions = new String[]{".png", ".webp", ".jpg", ".jpeg"};
        for (String ext : extensions) {
            try (InputStream is = context.getAssets().open("products/" + key + ext)) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bm = BitmapFactory.decodeStream(is, null, opts);
                if (bm != null) {
                    BITMAP_CACHE.put(key, bm);
                    return bm;
                }
            } catch (Exception ignored) {
                // Try next extension
            }
        }
        return null;
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
        return new CatalogArtwork(activity, p);
    }
}
