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

import static com.pharmasiu.mobile.ui.staff.media.StaffPhotoStore.mayView;
import static com.pharmasiu.mobile.ui.staff.media.StaffPhotoStore.mayEdit;
import static com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.bindPicture;
import com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.PictureSource;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories;
import static com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.dp;

/** Admin preview/thumbnail UI, permission check at callback-time. */
public final class StaffMediaPreview {
    private static final int WARNING=0xFF9A5318;
    private StaffMediaPreview(){}
    /**
     * Standalone preview content; the host may put this view inside a Dialog,
     * BottomSheet alternative, or an Activity page. Does not dismiss host UI.
     * Only ADMIN may invoke replace/remove callbacks; CASHIER can view.
     */
    public static View createPreview(Activity activity, UiHost host, DemoState.Product product,
                                     Uri photoUri, Runnable onReplace, Runnable onRemove,
                                     Runnable onClose) {
        requireInputs(activity, host, product);
        if (onClose == null) throw new IllegalArgumentException("onClose callback is required");
        if (!mayView(host.state().role)) return denied(activity);

        View root = activity.getLayoutInflater().inflate(R.layout.staff_media_preview, null, false);
        FrameLayout media = root.findViewById(R.id.staff_media_preview_picture);
        PictureSource source = bindPicture(activity, media, product, photoUri, "Ảnh sản phẩm");
        TextView note = root.findViewById(R.id.staff_media_preview_note);
        note.setText(source==PictureSource.LOCAL ? "Ảnh đã chọn trên thiết bị" :
            source==PictureSource.BUNDLED ? (photoUri==null ? "Ảnh danh mục · " + product.id
                : "Không đọc được ảnh đã chọn; đang dùng ảnh danh mục.") : "Chưa có ảnh cho mặt hàng này.");
        ((TextView) root.findViewById(R.id.staff_media_preview_title)).setText(product.name);
        ((TextView) root.findViewById(R.id.staff_media_preview_subtitle)).setText(product.id + " · " + product.unit);
        ((TextView) root.findViewById(R.id.staff_media_preview_category)).setText(CatalogCategories.categoryOf(product));
        ((TextView) root.findViewById(R.id.staff_media_preview_price)).setText(
            product.requiresPrescription ? "Rx · CẦN DƯỢC SĨ DUYỆT" :
                product.priceVnd <= 0 ? "Chưa có giá xác minh" :
                    String.format(new Locale("vi", "VN"), "%,d ₫", product.priceVnd));

        View.OnClickListener dismiss = v -> onClose.run();
        root.findViewById(R.id.staff_media_preview_close_top).setOnClickListener(dismiss);
        root.findViewById(R.id.staff_media_preview_close_bottom).setOnClickListener(dismiss);

        Button replace = root.findViewById(R.id.staff_media_preview_replace);
        Button remove = root.findViewById(R.id.staff_media_preview_remove);
        boolean edit = mayEdit(host.state().role);
        replace.setVisibility(edit && onReplace != null ? View.VISIBLE : View.GONE);
        remove.setVisibility(edit && photoUri != null && onRemove != null ? View.VISIBLE : View.GONE);
        if (edit && onReplace != null) {
            replace.setOnClickListener(v -> {
                if (!mayEdit(host.state().role)) {
                    host.message("Chỉ Quản lý mới có quyền chọn ảnh sản phẩm.");
                    return;
                }
                onReplace.run(); // The integration owner launches the staff image picker.
            });
        }
        if (edit && photoUri != null && onRemove != null) {
            remove.setOnClickListener(v -> {
                if (!mayEdit(host.state().role)) {
                    host.message("Chỉ Quản lý mới có quyền xóa ảnh sản phẩm.");
                    return;
                }
                onRemove.run(); // The integration owner clears its own RAM photo mapping.
            });
        }
        return root;
    }

    /**
     * A tappable thumbnail with the exact bundled SKU photo if no safe local
     * photo is available; use onOpen to display createPreview in the host UI.
     */
    public static View createThumbnail(Activity activity, UiHost host,
                                       DemoState.Product product, Uri photoUri,
                                       Runnable onOpen) {
        requireInputs(activity, host, product);
        if (!mayView(host.state().role)) return denied(activity);
        FrameLayout root = (FrameLayout) activity.getLayoutInflater().inflate(
            R.layout.staff_media_thumbnail, null, false);
        FrameLayout picture = root.findViewById(R.id.staff_media_thumbnail_picture);
        root.setClipToOutline(true);
        PictureSource source=bindPicture(activity,picture,product,photoUri,"Ảnh sản phẩm");
        root.setContentDescription("Xem ảnh " + product.name + " · " + product.id
            + (source==PictureSource.UNAVAILABLE ? ". Chưa có ảnh." : ". Ảnh sản phẩm."));
        if (onOpen != null) {
            root.setClickable(true);
            root.setFocusable(true);
            root.setOnClickListener(v -> {
                if (mayView(host.state().role)) onOpen.run();
                else host.message("Không đủ quyền xem hình ảnh sản phẩm.");
            });
        } else {
            root.setContentDescription("Ảnh " + product.name + " · " + product.id
                + (source==PictureSource.UNAVAILABLE ? ". Chưa có ảnh." : ""));
        }
        return root;
    }
    private static void requireInputs(Activity activity, UiHost host, DemoState.Product product) {
        if (activity == null || host == null || host.state() == null || product == null)
            throw new IllegalArgumentException("Activity, UiHost and DemoState.Product are required");
    }

    private static View denied(Activity activity) {
        TextView error = new TextView(activity);
        error.setText("Không đủ quyền xem ảnh sản phẩm.");
        error.setTextColor(WARNING);
        error.setTextSize(15);
        int p = dp(activity, 20);
        error.setPadding(p, p, p, p);
        error.setMinHeight(dp(activity, 56));
        return error;
    }
}
