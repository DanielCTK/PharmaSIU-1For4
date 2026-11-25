package com.pharmasiu.mobile.ui.staff.media;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.ui.customer.catalog.ProductImageLoader;
import java.io.IOException;
import java.io.InputStream;
import static com.pharmasiu.mobile.ui.staff.media.StaffPhotoStore.isAllowedLocalPhoto;

/** Staff photos: safe local override, exact bundled SKU photo, then an honest missing state. */
public final class MediaImageDecoder {
    private static final long MAX_PHOTO_BYTES=16L*1024L*1024L;
    private static final int MAX_DECODE_DIMENSION=1280;
    public enum PictureSource { LOCAL, BUNDLED, UNAVAILABLE }
    private MediaImageDecoder() { }

    /** Compatibility return value: true only when a chosen local photo was decoded. */
    public static boolean addPicture(Activity activity, FrameLayout frame,
                                    DemoState.Product product, Uri photoUri, String label) {
        return bindPicture(activity,frame,product,photoUri,label)==PictureSource.LOCAL;
    }

    public static PictureSource bindPicture(Activity activity, FrameLayout frame,
                                            DemoState.Product product, Uri photoUri, String label) {
        frame.removeAllViews();
        frame.setBackgroundColor(activity.getColor(R.color.white));
        Bitmap bitmap=null;
        PictureSource source=PictureSource.UNAVAILABLE;
        if(photoUri!=null) {
            try {
                String mime=activity.getContentResolver().getType(photoUri);
                if(isAllowedLocalPhoto(photoUri.toString(),mime)) bitmap=decodePreview(activity,photoUri);
                if(bitmap!=null)source=PictureSource.LOCAL;
            } catch(RuntimeException ignored) {
                // Revoked grants and unsafe providers must not hide a valid bundled SKU photo.
            }
        }
        if(bitmap==null) {
            bitmap=ProductImageLoader.loadBitmap(activity,product.id);
            if(bitmap!=null)source=PictureSource.BUNDLED;
        }
        if(bitmap!=null) {
            ImageView image=new ImageView(activity);
            image.setImageBitmap(bitmap);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding=dp(activity,6);
            image.setPadding(padding,padding,padding,padding);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            frame.addView(image,new FrameLayout.LayoutParams(-1,-1));
        } else {
            frame.addView(ProductImageLoader.createMissingArtwork(activity,product),
                new FrameLayout.LayoutParams(-1,-1));
        }
        frame.setContentDescription(label+": "+product.name+" · "+product.id
            +(source==PictureSource.UNAVAILABLE?". Chưa có ảnh.":source==PictureSource.LOCAL?". Ảnh đã chọn trên thiết bị.":". Ảnh danh mục."));
        return source;
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

    public static int dp(Activity activity,float value) {
        return Math.round(value*activity.getResources().getDisplayMetrics().density);
    }
}
