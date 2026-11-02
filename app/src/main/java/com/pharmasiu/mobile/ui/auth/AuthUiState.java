package com.pharmasiu.mobile.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.AuthScreen;

/** Error messages, keyboard, modes and form visual state. */
abstract class AuthUiState extends AuthFormBase {
    AuthUiState(Activity a,UiHost host,AuthMode initial){super(a,host,initial);}
    protected void setPasswordVisible(boolean show) {
        showPassword = show;
        int position = Math.max(0, password.getSelectionStart());
        password.setTransformationMethod(show
            ? HideReturnsTransformationMethod.getInstance()
            : PasswordTransformationMethod.getInstance());
        password.setSelection(Math.min(position, password.length()));
        togglePassword.setText(show ? "Ẩn" : "Hiện");
        togglePassword.setContentDescription(show ? "Ẩn mật khẩu" : "Hiện mật khẩu");
    }

    protected void setMode(AuthMode target, boolean clear) {
        mode = target;
        suppressTextEvents = true;
        if (clear) {
            identifier.setText("");
            name.setText("");
            password.setText("");
            passwordAgain.setText("");
        }
        suppressTextEvents = false;
        showPassword = false;
        setPasswordVisible(false);
        clearError();
        clearSuccess();
        demoExpanded = false;
        demoAccounts.setVisibility(View.GONE);
        demoToggle.setText("Tài khoản dùng thử  ▾");
        demoToggle.setContentDescription("Xem tài khoản demo");
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            demoToggle.setStateDescription("Đang đóng");
        }

        boolean login = mode == AuthMode.LOGIN;
        roles.setVisibility(login ? View.VISIBLE : View.GONE);
        nameSection.setVisibility(mode == AuthMode.REGISTER ? View.VISIBLE : View.GONE);
        confirmSection.setVisibility(login ? View.GONE : View.VISIBLE);
        modeActions.setVisibility(login ? View.VISIBLE : View.GONE);
        demoPanel.setVisibility(View.GONE);
        back.setVisibility(login ? View.GONE : View.VISIBLE);
        identifierLabel.setText(login ? "Email hoặc số điện thoại" : "Số điện thoại");
        identifier.setHint(login ? "Email / số điện thoại" : "Số điện thoại");
        identifier.setInputType(login ?
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS :
            InputType.TYPE_CLASS_PHONE);
        identifier.setFilters(new InputFilter[]{
            new InputFilter.LengthFilter(login ? MAX_IDENTIFIER : PHONE_LENGTH)
        });
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME)});
        password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PASSWORD)});
        passwordAgain.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PASSWORD)});
        password.setImeOptions(login ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
        switch (mode) {
            case REGISTER:
                title.setText("Đăng ký tài khoản khách hàng");
                subtitle.setText("Đăng ký tài khoản thành viên PharmaSIU.");
                submit.setText("Tạo tài khoản khách hàng");
                disclosure.setText("Mật khẩu được lưu dạng băm trên thiết bị. Chưa đồng bộ máy chủ, chưa có xác minh OTP.");
                break;
            case RESET:
                title.setText("Khôi phục tài khoản");
                subtitle.setText("Cần xác minh danh tính qua máy chủ để đặt lại mật khẩu.");
                submit.setText("Cập nhật mật khẩu");
                disclosure.setText("Tính năng này chưa khả dụng; không đổi mật khẩu qua số điện thoại chưa được xác minh.");
                break;
            default:
                title.setText("Đăng nhập PharmaSIU");
                subtitle.setText("Dành cho khách hàng, nhân viên và quản lý.");
                submit.setText("Đăng nhập");
                disclosure.setText("Hệ thống quản lý dược phẩm và nhà thuốc PharmaSIU.");
        }
        refreshSubmit();
    }

    protected void refreshSubmit() {
        boolean filled = !identifier.getText().toString().trim().isEmpty() &&
            !password.getText().toString().trim().isEmpty();
        submit.setEnabled(filled);
        submit.setAlpha(filled ? 1f : .54f);
    }

    protected void clearError() {
        error.setVisibility(View.GONE);
        error.setText("");
        identifier.setError(null);
        password.setError(null);
        passwordAgain.setError(null);
        name.setError(null);
    }

    protected void clearSuccess() {
        success.setVisibility(View.GONE);
        success.setText("");
    }

    protected void showError(String text) {
        clearSuccess();
        error.setText(text);
        error.setVisibility(View.VISIBLE);
        error.announceForAccessibility(text);
        if (text.startsWith("Số điện thoại")) identifier.setError(text);
        else if (text.startsWith("Mật khẩu nhập lại")) passwordAgain.setError(text);
        else if (text.startsWith("Mật khẩu")) password.setError(text);
        else if (text.contains("họ tên")) name.setError(text);
    }

    protected void showSuccess(String text) {
        clearError();
        success.setText(text);
        success.setVisibility(View.VISIBLE);
        success.announceForAccessibility(text);
    }

    protected void hideKeyboard() {
        try {
            InputMethodManager manager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null)
                manager.hideSoftInputFromWindow(root.getWindowToken(), 0);
            root.clearFocus();
        } catch (RuntimeException ignored) {
            // Unattached preview view must not crash if keyboard service is unavailable.
        }
    }

    protected void fillDemo(String account, String secret) {
        if (mode != AuthMode.LOGIN) return;
        suppressTextEvents = true;
        identifier.setText(account);
        password.setText(secret);
        suppressTextEvents = false;
        clearError();
        clearSuccess();
        hideKeyboard();
        refreshSubmit();
        host.message("Đã điền thông tin tài khoản; bấm Đăng nhập để tiếp tục.");
    }

    protected String modeName() {
        switch (mode) {
            case REGISTER: return "register";
            case RESET: return "reset";
            default: return "login";
        }
    }
}
