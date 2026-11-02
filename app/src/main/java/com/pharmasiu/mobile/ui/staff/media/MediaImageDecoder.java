package com.pharmasiu.mobile.ui.staff.media;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.content.res.AssetFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import static com.pharmasiu.mobile.ui.staff.media.StaffPhotoStore.isAllowedLocalPhoto;

/** Bounded local bitmap decode and offline placeholder illustration. */
public final class MediaImageDecoder {
    private static final int EMERALD=0xFF064D3A,LIME=0xFFB9F48B;
    private static final long MAX_PHOTO_BYTES=16L*1024L*1024L;
    private static final int MAX_DECODE_DIMENSION=1280;
    private MediaImageDecoder(){}
    /** Draw fallback first; overlay actual ImageView only if URI/MIME/load pass. */
    public static boolean addPicture(Activity activity, FrameLayout frame,
                                      DemoState.Product product, Uri photoUri, String label) {
        frame.removeAllViews();
        frame.setBackground(rounded(0xFFFFFFFF, 0));
        frame.setClipToOutline(true);
        ProductArtwork artwork = new ProductArtwork(activity, product.category);
        frame.addView(artwork, new FrameLayout.LayoutParams(-1, -1));
        artwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        boolean safe = false;
        if (photoUri != null) {
            try {
                // Avoid interpreting untrusted file:/http: URLs as Android images.
                String mime = activity.getContentResolver().getType(photoUri);
                if (isAllowedLocalPhoto(photoUri.toString(), mime)) {
                    Bitmap bitmap = decodePreview(activity, photoUri);
                    if (bitmap != null) {
                        ImageView picture = new ImageView(activity);
                        picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        picture.setContentDescription(label + ": " + product.name +
                            ". Ảnh sản phẩm đã chọn.");
                        picture.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                        picture.setImageBitmap(bitmap);
                        frame.addView(picture, new FrameLayout.LayoutParams(-1, -1));
                        safe = true;
                    }
                }
            } catch (RuntimeException failure) {
                // Revoked URI grants, corrupt images or unsupported decoders
                // produce a safe local fallback, not a crash or network request.
                safe = false;
            }
        }
        frame.setContentDescription(safe
            ? "Ảnh đã chọn cho " + product.name
            : "Hình minh họa trừu tượng theo nhóm " + product.category +
                " cho " + product.name);
        return safe;
    }

    /** Bound both the provider's declared bytes and decoded dimensions. */
    private static Bitmap decodePreview(Activity activity, Uri uri) {
        try (AssetFileDescriptor descriptor = activity.getContentResolver().openAssetFileDescriptor(uri, "r")) {
            if (descriptor == null || descriptor.getLength() > MAX_PHOTO_BYTES) return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                if (stream == null) return null;
                BitmapFactory.decodeStream(stream, null, options);
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) return null;
            int sample = 1;
            while (Math.max(options.outWidth / sample, options.outHeight / sample) > MAX_DECODE_DIMENSION
                && sample <= (1 << 20)) sample *= 2;
            options.inSampleSize = sample;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try (InputStream stream = activity.getContentResolver().openInputStream(uri)) {
                return stream == null ? null : BitmapFactory.decodeStream(stream, null, options);
            }
        } catch (IOException | RuntimeException | OutOfMemoryError ignored) {
            return null;
        }
    }

    private static GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    public static int dp(Activity a, float value) {
        return Math.round(value * a.getResources().getDisplayMetrics().density);
    }
    /** Drawn locally, abstract, and never presented as a verified product photo. */
    private static final class ProductArtwork extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int tint;
        private final int background;
        private final String category;

        ProductArtwork(Activity a, String category) {
            super(a);
            this.category = category == null ? "Khác" : category;
            switch (this.category) {
                case "Mỹ phẩm": tint=0xFFB87B94; background=0xFFF8E3E9; break;
                case "Thực phẩm":
                case "Thực phẩm bổ sung": tint=0xFFB38858; background=0xFFF7EAD8; break;
                case "Thiết bị": tint=0xFF6A98BD; background=0xFFE5EEF8; break;
                case "Chăm sóc": tint=0xFF5EAAA3; background=0xFFE4F5F1; break;
                default: tint=EMERALD; background=0xFFE0F4E6; break;
            }
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, w, h, background, Color.WHITE, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
            paint.setColor(0x66FFFFFF);
            canvas.drawCircle(w * .88f, h * .19f, w * .38f, paint);
            paint.setColor(0x334D7366);
            canvas.drawOval(w * .21f, h * .79f, w * .79f, h * .88f, paint);
            if ("Thiết bị".equals(category)) drawDevice(canvas, w, h);
            else if ("Thực phẩm".equals(category) || "Thực phẩm bổ sung".equals(category))
                drawFood(canvas, w, h);
            else drawBottle(canvas, w, h);
        }

        private void drawBottle(Canvas c, float w, float h) {
            paint.setColor(tint);
            c.drawRoundRect(new RectF(w*.43f, h*.23f, w*.57f, h*.33f),
                w*.025f, w*.025f, paint);
            paint.setColor(0xFFFDFDF9);
            c.drawRoundRect(new RectF(w*.31f,h*.32f,w*.69f,h*.79f),
                w*.07f,w*.07f,paint);
            paint.setColor(0xAA064D3A);
            c.drawRoundRect(new RectF(w*.37f,h*.49f,w*.63f,h*.55f),
                w*.025f,w*.025f,paint);
            paint.setColor(0x334D7366);
            c.drawRoundRect(new RectF(w*.41f,h*.60f,w*.59f,h*.62f),
                w*.012f,w*.012f,paint);
        }

        private void drawDevice(Canvas c, float w, float h) {
            paint.setColor(0xFFFFFFFF);
            c.drawRoundRect(new RectF(w*.23f,h*.28f,w*.77f,h*.77f),
                w*.07f,w*.07f,paint);
            paint.setColor(tint);
            c.drawRoundRect(new RectF(w*.31f,h*.37f,w*.69f,h*.58f),
                w*.02f,w*.02f,paint);
            paint.setColor(0xFFE9F4F0);
            c.drawRoundRect(new RectF(w*.37f,h*.41f,w*.63f,h*.54f),
                w*.014f,w*.014f,paint);
            paint.setColor(tint);
            c.drawCircle(w*.50f,h*.67f,w*.035f,paint);
        }

        private void drawFood(Canvas c, float w, float h) {
            paint.setColor(0xFFFFFFFF);
            c.drawOval(w*.20f,h*.32f,w*.80f,h*.75f,paint);
            paint.setColor(tint);
            c.drawOval(w*.29f,h*.40f,w*.71f,h*.68f,paint);
            paint.setColor(LIME);
            for (int i=0;i<7;i++) c.drawCircle(
                w*(.37f+.085f*(i%3)),h*(.47f+.065f*(i/3)),w*.026f,paint);
        }
    }
}
