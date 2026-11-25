package com.pharmasiu.mobile.ui.customer.account;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AtomicFile;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.design.UiMotion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Account-scoped avatar copies stay in app-private storage; no upload or schema change. */
public final class CustomerAvatarStore {
    private static final int MAX_BYTES = 12 * 1024 * 1024;
    private static final int MAX_EDGE = 512;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(1024) {
        @Override protected int sizeOf(String key, Bitmap bitmap) {
            return Math.max(1, bitmap.getByteCount() / 1024);
        }
    };
    private static final Set<String> SAVING = ConcurrentHashMap.newKeySet();
    // Accessed only on the main thread. Weak views cannot retain a destroyed Activity.
    private static final List<WeakReference<AvatarView>> VIEWS = new ArrayList<>();
    private static final ThreadPoolExecutor WORKER = new ThreadPoolExecutor(
        1, 1, 20, TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), runnable -> {
            Thread thread = new Thread(runnable, "customer-avatar");
            thread.setDaemon(true);
            return thread;
        }, new ThreadPoolExecutor.AbortPolicy());

    static { WORKER.allowCoreThreadTimeOut(true); }
    private CustomerAvatarStore() { }

    private static String accountKey(DemoState state) {
        if (state == null || !DemoState.CUSTOMER.equals(state.role)
                || state.currentCustomer() == null || state.authenticatedAccountId() <= 0) return null;
        return "account_" + state.authenticatedAccountId();
    }

    public static boolean canEdit(DemoState state) { return accountKey(state) != null; }

    public static View createAvatar(Activity activity, DemoState state, String name,
                                    int sizeDp, boolean editable, Runnable onChange) {
        return new AvatarView(activity, state, name, sizeDp, editable, onChange);
    }

    /** MainActivity delivers only a result matching the customer who opened the picker. */
    public static void saveSelected(Activity activity, UiHost host, Uri uri) {
        DemoState state = host.state();
        String key = accountKey(state);
        if (key == null || !allowed(activity, uri)) {
            host.message("Không đọc được ảnh. Hãy chọn ảnh từ thiết bị.");
            return;
        }
        if (!SAVING.add(key)) { host.message("Ảnh đang được lưu, vui lòng chờ."); return; }
        Context application = activity.getApplicationContext();
        WeakReference<UiHost> owner = new WeakReference<>(host);
        try {
            WORKER.execute(() -> {
                Bitmap bitmap = null;
                try { bitmap = decodeSelected(application, uri); }
                catch (IOException | RuntimeException | OutOfMemoryError ignored) { }
                Bitmap selected = bitmap;
                MAIN.post(() -> {
                    // Recheck on the UI thread after decode, before any account file is changed.
                    if (!key.equals(accountKey(state))) { SAVING.remove(key); return; }
                    if (selected == null) {
                        SAVING.remove(key);
                        report(owner, state, key, "Ảnh không hợp lệ hoặc lớn hơn 12 MB.");
                        return;
                    }
                    try {
                        WORKER.execute(() -> {
                            boolean saved = write(application, key, selected);
                            if (saved) CACHE.put(key, selected);
                            SAVING.remove(key);
                            MAIN.post(() -> {
                                if (saved) refreshViews(key, selected);
                                report(owner, state, key, saved ? "Đã cập nhật ảnh đại diện trên thiết bị."
                                    : "Không thể lưu ảnh đại diện. Vui lòng thử lại.");
                            });
                        });
                    } catch (RejectedExecutionException busy) {
                        SAVING.remove(key);
                        report(owner, state, key, "Đang xử lý ảnh, vui lòng thử lại.");
                    }
                });
            });
        } catch (RejectedExecutionException busy) {
            SAVING.remove(key);
            host.message("Đang xử lý ảnh, vui lòng thử lại.");
        }
    }

    private static void report(WeakReference<UiHost> owner, DemoState state, String key, String message) {
        UiHost host = owner.get();
        if (host instanceof Activity && ((Activity) host).isDestroyed()) return;
        if (host != null && host.state() == state && key.equals(accountKey(state))) host.message(message);
    }

    private static boolean allowed(Context context, Uri uri) {
        if (uri == null || !"content".equalsIgnoreCase(uri.getScheme())
                || uri.getAuthority() == null || uri.getAuthority().isEmpty()
                || uri.getFragment() != null || uri.toString().length() > 4096) return false;
        try {
            String mime = context.getContentResolver().getType(uri);
            if (mime == null) return false;
            switch (mime.toLowerCase(Locale.ROOT).trim()) {
                case "image/jpeg": case "image/png": case "image/webp": case "image/gif":
                case "image/heic": case "image/heif": case "image/bmp": return true;
                default: return false;
            }
        } catch (RuntimeException unavailable) { return false; }
    }

    private static Bitmap decodeSelected(Context context, Uri uri) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null) return null;
            byte[] buffer = new byte[8192];
            int total = 0, count;
            while ((count = stream.read(buffer)) != -1) {
                total += count;
                if (total > MAX_BYTES) return null;
                bytes.write(buffer, 0, count);
            }
        }
        byte[] data = bytes.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (options.outWidth <= 0 || options.outHeight <= 0) return null;
        int sample = 1;
        while (Math.max(options.outWidth / sample, options.outHeight / sample) > MAX_EDGE
                && sample <= (1 << 20)) sample *= 2;
        options.inSampleSize = sample;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (bitmap == null) return null;
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            orientation = new ExifInterface(new ByteArrayInputStream(data)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException | RuntimeException ignored) { }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(-90); matrix.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(270); break;
            default: return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static File file(Context context, String key) {
        return new File(new File(context.getFilesDir(), "customer_avatars"), key + ".jpg");
    }

    private static boolean write(Context context, String key, Bitmap bitmap) {
        AtomicFile atomic = new AtomicFile(file(context, key));
        FileOutputStream stream = null;
        try {
            File directory = file(context, key).getParentFile();
            if (!directory.isDirectory() && !directory.mkdirs()) return false;
            stream = atomic.startWrite();
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)) throw new IOException("encode");
            atomic.finishWrite(stream);
            return true;
        } catch (IOException | RuntimeException | OutOfMemoryError failure) {
            if (stream != null) atomic.failWrite(stream);
            return false;
        }
    }

    private static Bitmap read(Context context, String key) {
        try (InputStream stream = new AtomicFile(file(context, key)).openRead()) {
            return BitmapFactory.decodeStream(stream);
        } catch (IOException | RuntimeException | OutOfMemoryError unavailable) {
            return null;
        }
    }

    private static void refreshViews(String key, Bitmap bitmap) {
        VIEWS.removeIf(reference -> reference.get() == null);
        for (WeakReference<AvatarView> reference : VIEWS) {
            AvatarView view = reference.get();
            if (view != null && key.equals(view.key) && key.equals(accountKey(view.state))) view.bind(bitmap);
        }
    }

    private static final class AvatarView extends FrameLayout {
        final DemoState state;
        final String key;
        final ImageView image;
        final TextView initials;

        AvatarView(Activity activity, DemoState state, String name, int sizeDp,
                   boolean editable, Runnable onChange) {
            super(activity);
            this.state = state;
            key = accountKey(state);
            image = new ImageView(activity);
            image.setBackgroundResource(R.drawable.customer_account_avatar);
            image.setClipToOutline(true);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(image, new FrameLayout.LayoutParams(-1, -1));
            initials = new TextView(activity);
            initials.setText(initials(name));
            initials.setTextColor(activity.getColor(R.color.forest));
            initials.setTextSize(sizeDp * .34f);
            initials.setTypeface(null, android.graphics.Typeface.BOLD);
            initials.setGravity(Gravity.CENTER);
            initials.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(initials, new FrameLayout.LayoutParams(-1, -1));
            if (editable && key != null && onChange != null) {
                ImageView badge = new ImageView(activity);
                badge.setBackgroundResource(R.drawable.customer_account_profile_camera_surface);
                badge.setImageResource(R.drawable.customer_account_profile_camera);
                int padding = dp(activity, 6);
                badge.setPadding(padding, padding, padding, padding);
                badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                addView(badge, new FrameLayout.LayoutParams(dp(activity, 28), dp(activity, 28), Gravity.END | Gravity.BOTTOM));
                PharmaUi.pressable(this);
                UiMotion.press(this);
                setContentDescription("Đổi ảnh đại diện của " + name);
                setOnClickListener(v -> onChange.run());
            } else setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            if (key != null) bind(CACHE.get(key));
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            VIEWS.add(new WeakReference<>(this));
            if (key == null) return;
            Bitmap cached = CACHE.get(key);
            if (cached != null) { bind(cached); return; }
            Context application = getContext().getApplicationContext();
            WeakReference<AvatarView> target = new WeakReference<>(this);
            String account = key;
            try {
                WORKER.execute(() -> {
                    Bitmap bitmap = read(application, account);
                    if (bitmap != null) CACHE.put(account, bitmap);
                    MAIN.post(() -> {
                        AvatarView view = target.get();
                        if (view != null && view.isAttachedToWindow() && account.equals(accountKey(view.state))) view.bind(bitmap);
                    });
                });
            } catch (RejectedExecutionException busy) { /* Initials remain readable. */ }
        }

        @Override protected void onDetachedFromWindow() {
            VIEWS.removeIf(reference -> reference.get() == null || reference.get() == this);
            super.onDetachedFromWindow();
        }

        void bind(Bitmap bitmap) {
            image.setImageBitmap(bitmap);
            initials.setVisibility(bitmap == null ? View.VISIBLE : View.GONE);
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static String initials(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "KH";
        String[] parts = value.split("\\s+");
        String text = new String(Character.toChars(parts[0].codePointAt(0)));
        if (parts.length > 1) text += new String(Character.toChars(parts[parts.length - 1].codePointAt(0)));
        return text.toUpperCase(new Locale("vi", "VN"));
    }
}
