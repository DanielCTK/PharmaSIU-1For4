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
import static com.pharmasiu.mobile.ui.staff.media.MediaImageDecoder.addPicture;
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
        boolean localImage = addPicture(activity, media, product, photoUri, "Ảnh sản phẩm mẫu");
        TextView note = root.findViewById(R.id.staff_media_preview_note);
        note.setText(localImage
            ? "Ảnh đã chọn từ thiết bị · chỉ hiển thị trong phiên DEMO. Chưa được xác thực."
            : photoUri == null
                ? "Hình minh họa theo nhóm hàng · không phải ảnh chụp sản phẩm thật."
                : "Không thể đọc ảnh hoặc định dạng không được hỗ trợ. Đang hiển thị hình minh họa, chưa xác minh sản phẩm.");
        ((TextView) root.findViewById(R.id.staff_media_preview_title)).setText(product.name);
        ((TextView) root.findViewById(R.id.staff_media_preview_subtitle)).setText(product.subtitle);
        ((TextView) root.findViewById(R.id.staff_media_preview_category)).setText(product.category);
        ((TextView) root.findViewById(R.id.staff_media_preview_price)).setText(
            product.requiresPrescription ? "Rx · CẦN DƯỢC SĨ DUYỆT" :
                product.priceVnd <= 0 ? "Chưa có giá xác minh" :
                    String.format(new Locale("vi", "VN"), "%,d ₫ · DEMO", product.priceVnd));

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
                    host.message("Chỉ Quản lý DEMO được chọn ảnh hàng hóa.");
                    return;
                }
                onReplace.run(); // The integration owner launches the staff image picker.
            });
        }
        if (edit && photoUri != null && onRemove != null) {
            remove.setOnClickListener(v -> {
                if (!mayEdit(host.state().role)) {
                    host.message("Chỉ Quản lý DEMO được xóa ảnh đã chọn.");
                    return;
                }
                onRemove.run(); // The integration owner clears its own RAM photo mapping.
            });
        }
        return root;
    }

    /**
     * A 72dp tappable thumbnail with a category illustration if no safe local
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
        addPicture(activity, picture, product, photoUri, "Hình sản phẩm mẫu");
        root.setContentDescription("Xem hình sản phẩm " + product.name +
            (photoUri == null ? ". Đây là hình minh họa." : ". Ảnh chọn từ máy hoặc hình minh họa dự phòng."));
        if (onOpen != null) {
            root.setClickable(true);
            root.setFocusable(true);
            root.setOnClickListener(v -> {
                if (mayView(host.state().role)) onOpen.run();
                else host.message("Không đủ quyền xem hình hàng hóa DEMO.");
            });
        } else {
            root.setContentDescription("Hình minh họa sản phẩm " + product.name);
        }
        return root;
    }
    private static void requireInputs(Activity activity, UiHost host, DemoState.Product product) {
        if (activity == null || host == null || host.state() == null || product == null)
            throw new IllegalArgumentException("Activity, UiHost and DemoState.Product are required");
    }

    private static View denied(Activity activity) {
        TextView error = new TextView(activity);
        error.setText("Không đủ quyền xem ảnh hàng hóa. Chỉ khu nhân viên DEMO được sử dụng.");
        error.setTextColor(WARNING);
        error.setTextSize(15);
        int p = dp(activity, 20);
        error.setPadding(p, p, p, p);
        error.setMinHeight(dp(activity, 56));
        return error;
    }
}
