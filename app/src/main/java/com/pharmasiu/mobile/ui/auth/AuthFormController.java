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

import static com.pharmasiu.mobile.ui.AuthScreen.validate;

/** Event listeners and explicit client-side DEMO login/register/reset actions. */
public final class AuthFormController extends AuthUiState {
    public AuthFormController(Activity a,UiHost h,AuthMode mode){super(a,h,mode);}
    protected void attachListeners() {
        togglePassword.setOnClickListener(v -> setPasswordVisible(!showPassword));
        register.setOnClickListener(v -> setMode(AuthMode.REGISTER, true));
        forgot.setOnClickListener(v -> setMode(AuthMode.RESET, true));
        back.setOnClickListener(v -> setMode(AuthMode.LOGIN, true));
        submit.setOnClickListener(v -> submit());

        View tabCustomer = root.findViewById(R.id.auth_role_tab_customer);
        View tabCashier = root.findViewById(R.id.auth_role_tab_cashier);
        View tabManager = root.findViewById(R.id.auth_role_tab_manager);

        if (tabCustomer != null) {
            tabCustomer.setOnClickListener(v -> {
                identifier.setText("0900000000");
                password.setText("demo1234");
                hideKeyboard();
                host.message("Đã điền tài khoản Khách hàng (0900000000)");
            });
        }
        if (tabCashier != null) {
            tabCashier.setOnClickListener(v -> {
                identifier.setText("0901111111");
                password.setText("demo1234");
                hideKeyboard();
                host.message("Đã điền tài khoản Thu ngân (0901111111)");
            });
        }
        if (tabManager != null) {
            tabManager.setOnClickListener(v -> {
                identifier.setText("0902222222");
                password.setText("demo1234");
                hideKeyboard();
                host.message("Đã điền tài khoản Quản lý (0902222222)");
            });
        }
        // No hard-coded demo identities/passwords in the installed application.
        demoPanel.setVisibility(View.GONE);
        demoAccounts.setVisibility(View.GONE);
        forgot.setOnClickListener(v -> showError(
            "Chức năng khôi phục cần xác minh danh tính qua máy chủ; chưa được kích hoạt."));

        attachChange(name);
        attachChange(identifier);
        attachChange(password);
        attachChange(passwordAgain);
        name.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                identifier.requestFocus();
                return true;
            }
            return false;
        });
        identifier.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus();
                return true;
            }
            return false;
        });
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (mode == AuthMode.LOGIN) {
                    if (identifier.length() == 0 || password.length() == 0)
                        showError("Hãy nhập email và mật khẩu.");
                    else submit();
                } else {
                    passwordAgain.requestFocus();
                }
                return true;
            }
            return false;
        });
        passwordAgain.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN)) {
                submit();
                return true;
            }
            return false;
        });
    }

    protected void attachChange(EditText field) {
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressTextEvents) return;
                if (field == identifier && mode != AuthMode.LOGIN) {
                    String digits = asciiDigits(s.toString(), PHONE_LENGTH);
                    if (!digits.equals(s.toString())) {
                        int cursor = Math.min(digits.length(), field.getSelectionStart());
                        suppressTextEvents = true;
                        field.setText(digits);
                        field.setSelection(Math.max(0, cursor));
                        suppressTextEvents = false;
                    }
                }
                clearError();
                if (field == identifier) clearSuccess();
                refreshSubmit();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    protected static String asciiDigits(String text, int max) {
        StringBuilder filtered = new StringBuilder(max);
        for (int i = 0; i < text.length() && filtered.length() < max; i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') filtered.append(c);
        }
        return filtered.toString();
    }
    protected void submit() {
        if (!DemoState.NONE.equals(host.state().role)) {
            showError("Phiên đã đăng nhập. Vui lòng đăng xuất trước khi đổi tài khoản.");
            return;
        }
        String account = identifier.getText().toString().trim();
        String secret = password.getText().toString();
        String confirmation = passwordAgain.getText().toString();
        String fullName = name.getText().toString();
        String issue = validate(modeName(), account, fullName, secret, confirmation);
        if (issue != null) {
            showError(issue);
            return;
        }
        if (mode == AuthMode.LOGIN) {
            if (!host.state().login(account, secret)) {
                showError("Email/số điện thoại hoặc mật khẩu không đúng, hoặc tài khoản chưa kích hoạt.");
                return;
            }
            String role = host.state().role;
            if (!DemoState.CUSTOMER.equals(role) && !DemoState.CASHIER.equals(role)
                && !DemoState.ADMIN.equals(role)) {
                host.state().logout();
                showError("Tài khoản không có phân quyền được hỗ trợ.");
                return;
            }
            password.setText("");
            hideKeyboard();
            host.navigate(DemoState.ADMIN.equals(role) ? "admin" :
                DemoState.CASHIER.equals(role) ? "pos" : "home");
            return;
        }
        if (mode == AuthMode.REGISTER) {
            if (!host.state().registerCustomer(fullName.trim(), account, "", secret)) {
                showError("Không thể tạo tài khoản. Kiểm tra dữ liệu hoặc số điện thoại đã đăng ký.");
                return;
            }
            if (!host.state().login(account, secret) ||
                !DemoState.CUSTOMER.equals(host.state().role)) {
                // No fake success or role grant if the state resolver disagrees.
                showError("Tài khoản đã tạo nhưng phiên đăng nhập không hợp lệ.");
                return;
            }
            password.setText("");
            passwordAgain.setText("");
            hideKeyboard();
            host.message("Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay.");
            host.navigate("home");
            return;
        }
        // Never allow password reset based on a phone number alone in DB mode.
        if (host.state().isDatabaseBacked()) {
            showError("Cần xác minh danh tính qua dịch vụ khôi phục; hiện chưa hỗ trợ.");
            return;
        }
        if (!host.state().resetCustomerPasswordDemo(account, secret)) {
            showError("Không tìm thấy tài khoản này hoặc chưa thể đặt lại mật khẩu.");
            return;
        }
        hideKeyboard();
        setMode(AuthMode.LOGIN, true);
        showSuccess("Đã cập nhật mật khẩu mới. Vui lòng đăng nhập lại.");
    }
}
