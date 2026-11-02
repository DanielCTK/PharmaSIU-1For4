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

/** RAM photo URI ownership and validation. No network or filesystem fetches. */
public final class StaffPhotoStore {
    private StaffPhotoStore(){}
    // Session-local and scoped to the current demo store; no URI survives process exit.
    private static final Map<DemoState, Map<String, Uri>> DEMO_PHOTOS = new WeakHashMap<>();
    /** Returns the current staff preview URI; customer catalog never receives it. */
    public static synchronized Uri demoPhoto(DemoState state, String productId) {
        if (state == null || productId == null || !mayView(state.role)) return null;
        Map<String, Uri> rows = DEMO_PHOTOS.get(state);
        return rows == null ? null : rows.get(productId);
    }

    /** In-memory only; validates picker MIME, content URI, product and admin role. */
    public static synchronized boolean changeDemoPhoto(DemoState state, String productId,
                                                       Uri photoUri, String mime) {
        if (state == null || !mayEdit(state.role) || state.product(productId) == null) return false;
        if (photoUri != null && !isAllowedLocalPhoto(photoUri.toString(), mime)) return false;
        Map<String, Uri> rows = DEMO_PHOTOS.get(state);
        if (photoUri == null) {
            if (rows != null) rows.remove(productId);
            return true;
        }
        if (rows == null) {
            rows = new HashMap<>();
            DEMO_PHOTOS.put(state, rows);
        }
        rows.put(productId, photoUri);
        return true;
    }
    /**
     * Android UI-safe media URL classification with no Android dependency; JVM
     * tests may call it directly. Never accept http(s), file://, data: or
     * unknown/missing image MIME types.
     */
    public static boolean isAllowedLocalPhoto(String value, String mime) {
        if (value == null || mime == null || value.length() > 4096) return false;
        String text = value.trim();
        String format = mime.trim().toLowerCase(Locale.ROOT);
        switch (format) {
            case "image/jpeg":
            case "image/png":
            case "image/webp":
            case "image/gif":
            case "image/heic":
            case "image/heif":
            case "image/bmp":
                break;
            default:
                return false;
        }
        if (text.isEmpty() || text.indexOf('\\') >= 0) return false;
        try {
            URI parsed = new URI(text);
            String authority = parsed.getRawAuthority();
            return "content".equalsIgnoreCase(parsed.getScheme())
                && authority != null && !authority.trim().isEmpty()
                && authority.indexOf('@') < 0
                && parsed.getRawFragment() == null
                && parsed.getRawPath() != null && !parsed.getRawPath().isEmpty();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Role check also applied to the actual callback, not only button visibility. */
    public static boolean mayView(String role) {
        return DemoState.CASHIER.equals(role) || DemoState.ADMIN.equals(role);
    }

    public static boolean mayEdit(String role) {
        return DemoState.ADMIN.equals(role);
    }
}
