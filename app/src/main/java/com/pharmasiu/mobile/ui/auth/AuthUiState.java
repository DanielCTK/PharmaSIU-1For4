package com.pharmasiu.mobile.ui.auth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.AuthScreen;
import com.pharmasiu.mobile.ui.design.UiMotion;

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
        hideKeyboard();
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
        setDemoExpanded(false);

        boolean login = mode == AuthMode.LOGIN;
        heroEffects.setVisibility(login ? View.VISIBLE : View.INVISIBLE);
        roles.setVisibility(login ? View.VISIBLE : View.GONE);
        nameSection.setVisibility(mode == AuthMode.REGISTER ? View.VISIBLE : View.GONE);
        confirmSection.setVisibility(login ? View.GONE : View.VISIBLE);
        modeActions.setVisibility(login ? View.VISIBLE : View.GONE);
        demoPanel.setVisibility(login ? View.VISIBLE : View.GONE);
        back.setVisibility(login ? View.GONE : View.VISIBLE);
        identifierLabel.setText(login ? "Email hoặc số điện thoại" : "Số điện thoại");
        identifier.setHint(login ? "Nhập email hoặc số điện thoại" : "Nhập số điện thoại");
        identifier.setInputType(login ?
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS :
            InputType.TYPE_CLASS_PHONE);
        identifier.setFilters(new InputFilter[]{
            new InputFilter.LengthFilter(login ? MAX_IDENTIFIER : PHONE_LENGTH)
        });
        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME)});
        password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PASSWORD)});
        passwordAgain.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PASSWORD)});
        password.setImeOptions((login ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT)
            | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        title.setTextSize(login ? 32 : 26);
        if (android.os.Build.VERSION.SDK_INT >= 28) title.setAccessibilityHeading(true);
        switch (mode) {
            case REGISTER:
                title.setText("Tạo tài khoản");
                subtitle.setText("Dành cho khách hàng PharmaSIU.");
                submit.setText("Tạo tài khoản");
                disclosure.setText("Mật khẩu được lưu dạng băm trên thiết bị. Chưa đồng bộ máy chủ, chưa có xác minh OTP.");
                break;
            case RESET:
                title.setText("Khôi phục tài khoản");
                subtitle.setText("Cần xác minh danh tính qua máy chủ để đặt lại mật khẩu.");
                submit.setText("Cập nhật mật khẩu");
                disclosure.setText("Tính năng này chưa khả dụng; không đổi mật khẩu qua số điện thoại chưa được xác minh.");
                break;
            default:
                SpannableString greeting = new SpannableString("Chào mừng\ntrở lại");
                int secondLine = greeting.toString().indexOf('\n') + 1;
                greeting.setSpan(new ForegroundColorSpan(0xFFBDF7DC),
                    secondLine, greeting.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                title.setText(greeting);
                subtitle.setText("Đăng nhập để tiếp tục.");
                submit.setText("Đăng nhập");
                disclosure.setText("Bản dùng thử trên thiết bị · Chưa kết nối máy chủ.");
        }
        refreshSubmit();
        root.post(() -> {
            if (!root.isAttachedToWindow() || mode != target) return;
            ((ScrollView) root).scrollTo(0, 0);
            glassForm.animate().cancel();
            glassForm.setTranslationY(0f);
            UiMotion.enterPage(glassForm, 0, false);
            if (login) AuthButtonShimmer.play(submit);
        });
    }

    protected void setDemoExpanded(boolean expanded) {
        demoExpanded = expanded;
        demoAccounts.setVisibility(expanded ? View.VISIBLE : View.GONE);
        demoToggle.setText(expanded ? "Tài khoản dùng thử  ▴" : "Tài khoản dùng thử  ▾");
        demoToggle.setContentDescription(expanded ? "Thu gọn tài khoản demo" : "Xem tài khoản demo");
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            demoToggle.setStateDescription(expanded ? "Đang mở" : "Đang đóng");
        }
        if (expanded) demoAccounts.post(() -> {
            if (!demoExpanded || !demoAccounts.isAttachedToWindow()) return;
            demoAccounts.requestRectangleOnScreen(
                new Rect(0, 0, demoAccounts.getWidth(), demoAccounts.getHeight()), false);
            UiMotion.enterPage(demoAccounts, 0, false);
        });
    }

    protected void refreshSubmit() {
        boolean filled = !identifier.getText().toString().trim().isEmpty() &&
            !password.getText().toString().trim().isEmpty();
        boolean newlyEnabled = filled && !submit.isEnabled();
        submit.setEnabled(filled);
        submit.setAlpha(filled ? 1f : .84f);
        if (newlyEnabled && mode == AuthMode.LOGIN) AuthButtonShimmer.play(submit);
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
        error.post(() -> error.requestRectangleOnScreen(
            new Rect(0, 0, error.getWidth(), error.getHeight()), false));
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
        submit.post(() -> submit.requestRectangleOnScreen(
            new Rect(0, 0, submit.getWidth(), submit.getHeight()), false));
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
