package com.pharmasiu.mobile.ui.customer.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/** Dedicated account feature controller. */
public abstract class AccountProfile extends AccountHome {
    protected AccountProfile(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}
    protected EditText field(LinearLayout parent, String hint, String value, int type, int limit) {
        text(parent, hint, 13, R.color.forest, true, 12);
        EditText field = new EditText(activity);
        field.setText(value == null ? "" : value);
        field.setHint(hint);
        field.setSingleLine(true);
        field.setTextSize(16);
        field.setTextColor(color(R.color.ink));
        field.setHintTextColor(color(R.color.muted));
        field.setInputType(type);
        field.setFilters(new InputFilter[] { new InputFilter.LengthFilter(limit) });
        field.setPadding(dp(12), dp(12), dp(12), dp(12));
        field.setBackground(activity.getDrawable(R.drawable.bg_panel));
        parent.addView(field, params(-1, dp(54), 7));
        return field;
    }

    protected String value(EditText edit) { return edit.getText().toString().trim(); }

    // DemoState permits both 10- and 11-digit local contact numbers.
    protected boolean phoneValid(String number) { return number.matches("0[0-9]{9,10}"); }

    protected boolean emailValid(String value) {
        return value.isEmpty() || value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    protected void profile() {
        hero("HỒ SƠ CÁ NHÂN", "Chỉnh sửa hồ sơ",
            "Quản lý và cập nhật thông tin liên hệ của bạn.", true);
        LinearLayout form = card();
        EditText name = field(form, "Họ và tên", profile.displayName,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, 70);
        EditText phone = field(form, "Số điện thoại đăng nhập", profile.phone,
            InputType.TYPE_CLASS_PHONE, 11);
        EditText email = field(form, "Email (không bắt buộc)", profile.email,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 100);
        EditText birthday = field(form, "Ngày sinh", profile.birthday,
            InputType.TYPE_CLASS_TEXT, 20);
        text(form, "Số điện thoại được sử dụng để đăng nhập và xác thực đơn hàng của bạn.",
            12, R.color.muted, false, 12);
        action(form, "Lưu thay đổi", true, () -> {
            String n = value(name), p = value(phone), e = value(email);
            if (n.length() < 2 || !phoneValid(p) || !emailValid(e)) {
                host.message("Tên tối thiểu 2 ký tự, SĐT gồm 10–11 số bắt đầu bằng 0 và email hợp lệ.");
                return;
            }
            if (!p.equals(profile.phone) && state.customerAccounts.containsKey(p)) {
                host.message("Số điện thoại này đã được sử dụng.");
                return;
            }
            for (DemoState.CustomerProfile other : state.customerAccounts.values()) {
                if (other != profile && !e.isEmpty() && e.equalsIgnoreCase(other.email)) {
                    host.message("Email này đã được sử dụng.");
                    return;
                }
            }
            if (!state.updateCustomer(n, e, value(birthday))) {
                host.message("Không thể lưu hồ sơ. Vui lòng kiểm tra lại thông tin.");
                return;
            }
            if (!state.changeCustomerPhone(p)) {
                host.message("Không thể đổi số điện thoại.");
                return;
            }
            host.message("Đã cập nhật thông tin thành công");
            host.navigate("account");
        });
        action(body, "Bỏ thay đổi", false, () -> {
            boolean dirty = !profile.displayName.equals(value(name)) ||
                !profile.phone.equals(value(phone)) || !profile.email.equals(value(email)) ||
                !profile.birthday.equals(value(birthday));
            if (!dirty) { host.navigate("account"); return; }
            new AlertDialog.Builder(activity).setTitle("Bỏ thay đổi hồ sơ?")
                .setMessage("Các nội dung chưa lưu sẽ bị bỏ.")
                .setNegativeButton("Tiếp tục chỉnh sửa", null)
                .setPositiveButton("Bỏ thay đổi", (d, which) -> host.navigate("account"))
                .show();
        });
        disclaimer();
    }

    protected void password() {
        hero("BẢO MẬT", "Đổi mật khẩu",
            "Sử dụng mật khẩu thử nghiệm; không nhập mật khẩu thật.", true);
        LinearLayout form = card();
        int masked = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        EditText before = field(form, "Mật khẩu hiện tại", "", masked, 80);
        EditText after = field(form, "Mật khẩu mới (tối thiểu 6 ký tự)", "", masked, 80);
        EditText repeat = field(form, "Nhập lại mật khẩu mới", "", masked, 80);
        action(form, "Cập nhật mật khẩu trong phiên", true, () -> {
            String oldPassword = before.getText().toString();
            String newPassword = after.getText().toString();
            if (!profile.password.equals(oldPassword)) {
                host.message("Mật khẩu hiện tại không chính xác.");
            } else if (newPassword.length() < 6 || !newPassword.equals(repeat.getText().toString())) {
                host.message("Mật khẩu mới tối thiểu 6 ký tự và phải nhập khớp.");
            } else if (state.changePassword(oldPassword, newPassword)) {
                before.setText(""); after.setText(""); repeat.setText("");
                host.message("Đã cập nhật mật khẩu thành công");
                host.navigate("account");
            } else host.message("Không thể cập nhật mật khẩu.");
        });
        text(body, "Mật khẩu mới sẽ có hiệu lực ngay cho các lần đăng nhập tiếp theo.",
            13, R.color.muted, false, 16);
        disclaimer();
    }
}
