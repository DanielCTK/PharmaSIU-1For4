package com.pharmasiu.mobile.ui.customer.account;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import com.google.android.material.button.MaterialButton;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import com.pharmasiu.mobile.ui.design.UiMotion;

import java.util.Locale;

/** Focused edits for fields actually persisted by the customer account repository. */
final class CustomerProfilePage {
    private static final int NAME = 0, PHONE = 1, EMAIL = 2;
    private final AccountProfile ui;
    private AlertDialog editor;

    CustomerProfilePage(AccountProfile ui) { this.ui = ui; }

    void show() {
        ui.header.setBackgroundColor(ui.color(R.color.white));
        ui.header.setClipToOutline(false);
        ui.header.setPadding(ui.dp(16), ui.dp(12), ui.dp(16), ui.dp(12));
        toolbar();
        identity();
        ui.section("Thông tin cá nhân");
        LinearLayout personal = group();
        row(personal, R.drawable.customer_account_profile, "Họ và tên", ui.profile.displayName, NAME);
        ui.section("Thông tin liên hệ");
        LinearLayout contact = group();
        row(contact, R.drawable.customer_account_profile_phone, "Số điện thoại đăng nhập", ui.profile.phone, PHONE);
        row(contact, R.drawable.customer_account_profile_email, "Email", ui.profile.email, EMAIL);
        ui.disclaimer();
        ui.body.getChildAt(0).addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) { }
            @Override public void onViewDetachedFromWindow(View view) {
                if (editor != null) editor.dismiss();
                view.removeOnAttachStateChangeListener(this);
            }
        });
    }

    private void toolbar() {
        LinearLayout row = new LinearLayout(ui.activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        MaterialButton back = new MaterialButton(ui.activity);
        back.setText("");
        back.setIconResource(R.drawable.customer_account_back);
        back.setIconTint(ColorStateList.valueOf(ui.color(R.color.forest)));
        back.setIconSize(ui.dp(24)); back.setIconPadding(0);
        back.setPadding(ui.dp(12), ui.dp(12), ui.dp(12), ui.dp(12));
        back.setInsetTop(0); back.setInsetBottom(0);
        back.setMinWidth(0); back.setMinimumWidth(0); back.setMinHeight(ui.dp(48));
        back.setCornerRadius(ui.dp(15));
        back.setBackgroundTintList(ColorStateList.valueOf(ui.color(R.color.mint)));
        back.setContentDescription("Quay lại Cá nhân");
        back.setOnClickListener(v -> ui.activity.onBackPressed());
        UiMotion.press(back);
        row.addView(back, new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48)));
        TextView heading = ui.label("Hồ sơ cá nhân", 23, R.color.forest, true);
        ViewCompat.setAccessibilityHeading(heading, true);
        LinearLayout.LayoutParams copy = new LinearLayout.LayoutParams(0, -2, 1);
        copy.setMarginStart(ui.dp(12));
        row.addView(heading, copy);
        ui.header.addView(row, ui.params(-1, -2, 0));
    }

    private void identity() {
        LinearLayout identity = new LinearLayout(ui.activity);
        identity.setGravity(Gravity.CENTER_VERTICAL);
        identity.setPadding(0, ui.dp(16), 0, ui.dp(10));
        boolean canEdit = CustomerAvatarStore.canEdit(ui.state);
        identity.addView(CustomerAvatarStore.createAvatar(ui.activity, ui.state, ui.profile.displayName,
            80, canEdit, ui.host::chooseCustomerAvatar), new LinearLayout.LayoutParams(ui.dp(80), ui.dp(80)));
        LinearLayout copy = ui.stack();
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1);
        copyParams.setMarginStart(ui.dp(18));
        identity.addView(copy, copyParams);
        TextView name = ui.label(ui.profile.displayName, 21, R.color.forest, true);
        copy.addView(name, ui.params(-1, -2, 0));
        ui.text(copy, "Khách hàng PharmaSIU", 13, R.color.muted, false, 4);
        if (canEdit) {
            TextView photo = ui.label("Đổi ảnh đại diện", 14, R.color.emerald, true);
            photo.setMinHeight(ui.dp(48)); photo.setGravity(Gravity.CENTER_VERTICAL);
            photo.setOnClickListener(v -> ui.host.chooseCustomerAvatar());
            photo.setContentDescription("Chọn ảnh đại diện từ thiết bị");
            PharmaUi.pressable(photo); UiMotion.press(photo);
            copy.addView(photo, ui.params(-1, -2, 0));
        }
        ui.body.addView(identity, ui.params(-1, -2, 0));
    }

    private LinearLayout group() {
        LinearLayout group = ui.stack();
        group.setBackgroundResource(R.drawable.customer_account_profile_group);
        group.setClipToOutline(true);
        ui.body.addView(group, ui.params(-1, -2, 8));
        return group;
    }

    private void row(LinearLayout group, int icon, String label, String value, int field) {
        if (group.getChildCount() != 0) {
            View line = new View(ui.activity);
            line.setBackgroundColor(ui.color(R.color.line));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, ui.dp(1));
            lineParams.setMarginStart(ui.dp(54)); lineParams.setMarginEnd(ui.dp(14));
            group.addView(line, lineParams);
        }
        LinearLayout row = new LinearLayout(ui.activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(ui.dp(80));
        row.setPadding(ui.dp(14), ui.dp(13), ui.dp(14), ui.dp(13));
        row.addView(ui.icon(icon, R.color.emerald), new LinearLayout.LayoutParams(ui.dp(22), ui.dp(22)));
        LinearLayout copy = ui.stack();
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, -2, 1);
        copyParams.setMarginStart(ui.dp(16)); copyParams.setMarginEnd(ui.dp(12));
        row.addView(copy, copyParams);
        TextView caption = ui.label(label, 13, R.color.muted, false);
        caption.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(caption, ui.params(-1, -2, 0));
        boolean empty = value == null || value.isEmpty();
        TextView content = ui.label(empty ? "Chưa cập nhật" : value, 16,
            empty ? R.color.muted : R.color.ink, !empty);
        content.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        copy.addView(content, ui.params(-1, -2, 4));
        ImageView edit = ui.icon(R.drawable.customer_account_profile_edit, R.color.emerald);
        row.addView(edit, new LinearLayout.LayoutParams(ui.dp(20), ui.dp(20)));
        PharmaUi.pressable(row); UiMotion.press(row);
        row.setContentDescription(label + ", " + (empty ? "chưa cập nhật" : value) + ". Chỉnh sửa");
        row.setOnClickListener(v -> edit(field));
        group.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void edit(int kind) {
        if (ui.state.currentCustomer() != ui.profile) return;
        String title = kind == NAME ? "Họ và tên" : kind == PHONE ? "Số điện thoại đăng nhập" : "Email";
        String before = kind == NAME ? ui.profile.displayName : kind == PHONE ? ui.profile.phone : ui.profile.email;
        LinearLayout form = ui.stack();
        form.setPadding(ui.dp(20), 0, ui.dp(20), ui.dp(10));
        int type = kind == PHONE ? InputType.TYPE_CLASS_PHONE : kind == EMAIL
            ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS;
        EditText input = ui.field(form, title, before, type, kind == NAME ? 70 : kind == PHONE ? 11 : 100);
        input.setId(View.generateViewId());
        ((TextView) form.getChildAt(0)).setLabelFor(input.getId());
        input.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        if (kind == PHONE) ui.text(form, "Số mới sẽ được dùng cho lần đăng nhập tiếp theo.", 13, R.color.muted, false, 12);
        if (kind == EMAIL) ui.text(form, "Không bắt buộc. Bạn có thể để trống.", 13, R.color.muted, false, 12);
        editor = new AlertDialog.Builder(ui.activity).setTitle("Chỉnh sửa " + title.toLowerCase(new Locale("vi", "VN")))
            .setView(form).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create();
        AlertDialog dialog = editor;
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (ui.state.currentCustomer() != ui.profile) { dialog.dismiss(); return; }
                String value = ui.value(input);
                String issue = validate(kind, value);
                if (issue != null) { input.setError(issue); input.requestFocus(); return; }
                if (kind == EMAIL) value = value.toLowerCase(Locale.ROOT);
                boolean saved = kind == PHONE ? ui.state.changeCustomerPhone(value)
                    : ui.state.updateCustomer(kind == NAME ? value : ui.profile.displayName,
                        kind == EMAIL ? value : ui.profile.email,
                        ui.state.isDatabaseBacked() ? "" : ui.profile.birthday);
                if (!saved) {
                    input.setError(kind == PHONE ? "Không thể lưu số điện thoại; số này có thể đã được sử dụng."
                        : "Không thể lưu thông tin. Vui lòng kiểm tra lại.");
                    return;
                }
                dialog.dismiss();
                ui.host.message("Đã lưu thông tin cá nhân");
                ui.draw();
            });
            input.setOnEditorActionListener((v, action, event) -> {
                if (action != EditorInfo.IME_ACTION_DONE) return false;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                return true;
            });
            input.requestFocus(); input.setSelection(input.length());
            if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        });
        dialog.show();
    }

    private String validate(int kind, String value) {
        if (kind == NAME && value.length() < 2) return "Họ tên cần ít nhất 2 ký tự.";
        if (kind == PHONE) {
            if (!ui.phoneValid(value)) return "Nhập 10–11 số, bắt đầu bằng 0.";
            if (!value.equals(ui.profile.phone) && ui.state.customerAccounts.containsKey(value)) return "Số điện thoại đã được sử dụng.";
        }
        if (kind == EMAIL) {
            if (!ui.emailValid(value)) return "Địa chỉ email chưa hợp lệ.";
            for (DemoState.CustomerProfile other : ui.state.customerAccounts.values())
                if (other != ui.profile && !value.isEmpty() && value.equalsIgnoreCase(other.email)) return "Email đã được sử dụng.";
        }
        return null;
    }
}
