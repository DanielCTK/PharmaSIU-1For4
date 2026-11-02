package com.pharmasiu.mobile.ui;

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
import com.pharmasiu.mobile.ui.auth.AuthFormController;
import com.pharmasiu.mobile.ui.auth.AuthMode;


/**
 * Complete native-Views counterpart of the original UnifiedLoginScreen.kt.
 *
 * Device-local SQLite authentication. Server identity/OTP recovery is not integrated.
 *
 * This screen owns only its Views and listeners. It does not change role or
 * business logic directly: DemoState.login determines the actual role.
 */
public final class AuthScreen {
    private static final int MAX_IDENTIFIER = 120;
    private static final int MAX_NAME = 70;
    private static final int MAX_PASSWORD = 64;
    private static final int PHONE_LENGTH = 10;

    private AuthScreen() { }

    public static View create(Activity activity, UiHost host, String route) {
        if (activity == null || host == null || host.state() == null)
            throw new IllegalArgumentException("Activity and DEMO UiHost are required");
        if (!DemoState.NONE.equals(host.state().role)) {
            TextView denied = new TextView(activity);
            denied.setText("Bạn đã đăng nhập. Đăng xuất trước khi mở cổng đăng nhập.");
            denied.setTextColor(activity.getColor(R.color.warning));
            denied.setTextSize(15);
            int p = dp(activity, 20);
            denied.setPadding(p, p, p, p);
            return denied;
        }
        AuthMode start = "register".equals(route) ? AuthMode.REGISTER :
            ("reset".equals(route) || "forgot".equals(route)
                || "forgot-password".equals(route)) ? AuthMode.RESET : AuthMode.LOGIN;
        return new AuthFormController(activity, host, start).view();
    }

    /** Exact Compose UI contract (10 ASCII digits starting with zero). */
    static boolean validDemoPhone(String value) {
        return value != null && value.matches("0[0-9]{9}");
    }

    /** UI form validation before invoking DemoState, never an authentication claim. */
    public static String validate(String mode, String identifier, String name,
                           String password, String confirmation) {
        String account = identifier == null ? "" : identifier.trim();
        String secret = password == null ? "" : password;
        if (account.isEmpty() || secret.trim().isEmpty())
            return "Vui lòng nhập thông tin tài khoản và mật khẩu.";
        if (!"login".equals(mode)) {
            if (!validDemoPhone(account))
                return "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.";
            if (secret.length() < 8)
                return "Mật khẩu cần ít nhất 8 ký tự.";
            if (!secret.equals(confirmation))
                return "Mật khẩu nhập lại chưa khớp.";
            if ("register".equals(mode) && (name == null || name.trim().length() < 2))
                return "Vui lòng nhập họ tên hợp lệ.";
        }
        return null;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
