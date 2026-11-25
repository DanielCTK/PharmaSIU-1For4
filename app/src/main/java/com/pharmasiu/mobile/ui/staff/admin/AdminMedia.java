package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.pharmasiu.mobile.ui.staff.StaffMediaViews;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;

/** Local media selection and preview, admin-only. */
public final class AdminMedia {
    private AdminMedia() {}

    interface PhotoSelection { void onSelected(Uri uri); }

    /** Android platform fragment owns the picker result, so the Activity needs no new callback. */
    public static final class DemoPhotoPickerFragment extends Fragment {
        private static final int REQUEST_IMAGE = 2207;
        private PhotoSelection callback;

        void start(UiHost host, PhotoSelection selection) {
            callback = selection;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(intent, REQUEST_IMAGE);
            } catch (ActivityNotFoundException | SecurityException exception) {
                callback = null;
                host.message("Thiết bị không có ứng dụng chọn ảnh phù hợp");
            }
        }

        @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode != REQUEST_IMAGE) return;
            PhotoSelection selected = callback;
            callback = null;
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null ||
                selected == null || getActivity() == null) return;
            Uri uri = data.getData();
            String mime;
            try {
                mime = getActivity().getContentResolver().getType(uri);
            } catch (RuntimeException exception) {
                mime = null;
            }
            if (!StaffMediaViews.isAllowedLocalPhoto(uri.toString(), mime)) {
                android.widget.Toast.makeText(getActivity(),
                    "Ảnh không hợp lệ; chỉ nhận JPEG, PNG, WebP, GIF, HEIF hoặc BMP từ thiết bị",
                    android.widget.Toast.LENGTH_LONG).show();
                return;
            }
            selected.onSelected(uri);
        }
    }

    static void pickPhoto(Activity activity, UiHost host, PhotoSelection selected) {
        if (!host.state().canAdmin()) {
            host.message("Chỉ Quản lý mới có quyền cập nhật ảnh hàng hóa");
            return;
        }
        android.app.FragmentManager manager = activity.getFragmentManager();
        Fragment existing = manager.findFragmentByTag(PHOTO_PICKER_TAG);
        DemoPhotoPickerFragment picker;
        if (existing instanceof DemoPhotoPickerFragment) picker = (DemoPhotoPickerFragment) existing;
        else {
            picker = new DemoPhotoPickerFragment();
            manager.beginTransaction().add(picker, PHOTO_PICKER_TAG).commitAllowingStateLoss();
            manager.executePendingTransactions();
        }
        picker.start(host, selected);
    }

    static void showProductPhoto(Activity activity, UiHost host, DemoState.Product product) {
        showProductPhoto(activity,host,product,"catalog-admin-v2");
    }

    static void showProductPhoto(Activity activity, UiHost host, DemoState.Product product, String returnRoute) {
        DemoState state = host.state();
        if (state.product(product.id) == null) return;
        Uri existingPhoto = StaffMediaViews.demoPhoto(state, product.id);
        AlertDialog dialog = new PhotoDialog(activity);
        View content = StaffMediaViews.createPreview(activity, host, product, existingPhoto,
            () -> {
                dialog.dismiss();
                pickPhoto(activity, host, selected -> {
                    String mime;
                    try { mime = activity.getContentResolver().getType(selected); }
                    catch (RuntimeException unavailableImage) {
                        host.message("Không thể đọc ảnh đã chọn; vui lòng chọn lại ảnh");
                        return;
                    }
                    if (StaffMediaViews.changeDemoPhoto(state, product.id, selected, mime)) {
                        host.message("Đã cập nhật ảnh sản phẩm");
                        host.navigate(returnRoute);
                    } else host.message("Không thể gắn ảnh cho sản phẩm");
                });
            },
            () -> {
                if (StaffMediaViews.changeDemoPhoto(state, product.id, null, null)) {
                    dialog.dismiss();
                    host.message("Đã khôi phục ảnh mặc định");
                    host.navigate(returnRoute);
                }
            }, dialog::dismiss);
        dialog.setView(content);
        dialog.show();
        if (dialog.getWindow() != null) {
            int height = Math.round(activity.getResources().getDisplayMetrics().heightPixels * .86f);
            dialog.getWindow().setLayout(-1, height);
        }
    }
    /** Removes the preview window before its owning Activity is recreated/stopped. */
    private static final class PhotoDialog extends AlertDialog
            implements android.app.Application.ActivityLifecycleCallbacks {
        private final Activity owner;
        private boolean registered;
        PhotoDialog(Activity activity) { super(activity); owner=activity; setOwnerActivity(activity); }
        @Override protected void onStart() {
            super.onStart();
            if(!registered) { owner.getApplication().registerActivityLifecycleCallbacks(this); registered=true; }
        }
        private void unregister() {
            if(registered) { owner.getApplication().unregisterActivityLifecycleCallbacks(this); registered=false; }
        }
        @Override public void dismiss() { unregister(); super.dismiss(); }
        @Override public void cancel() { unregister(); super.cancel(); }
        @Override protected void onStop() { unregister(); super.onStop(); }
        @Override public void onActivityStopped(Activity activity) { if(activity==owner)dismiss(); }
        @Override public void onActivityPreDestroyed(Activity activity) { if(activity==owner)dismiss(); }
        @Override public void onActivityDestroyed(Activity activity) { if(activity==owner)dismiss(); }
        @Override public void onActivityCreated(Activity activity, Bundle bundle) { }
        @Override public void onActivityStarted(Activity activity) { }
        @Override public void onActivityResumed(Activity activity) { }
        @Override public void onActivityPaused(Activity activity) { }
        @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) { }
    }

}
