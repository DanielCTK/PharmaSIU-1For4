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

/**
 * Owns XML bindings only; subclasses divide UI state and event handlers.
 * No auth decisions happen here (DemoState.login is the source of truth).
 */
abstract class AuthFormBase {
    protected static final int MAX_IDENTIFIER=120, MAX_NAME=70, MAX_PASSWORD=64, PHONE_LENGTH=10;

    final Activity activity;
    final UiHost host;
    final View root;

    final LinearLayout roles;
    final LinearLayout nameSection;
    final LinearLayout confirmSection;
    final LinearLayout modeActions;
    final LinearLayout demoPanel;
    final LinearLayout demoAccounts;
    final TextView title;
    final TextView subtitle;
    final TextView identifierLabel;
    final TextView error;
    final TextView success;
    final TextView disclosure;
    final EditText name;
    final EditText identifier;
    final EditText password;
    final EditText passwordAgain;
    final Button togglePassword;
    final Button submit;
    final Button register;
    final Button forgot;
    final Button back;
    final Button demoToggle;
    final Button demoCustomer;
    final Button demoCashier;
    final Button demoManager;
    AuthMode mode;
    boolean showPassword;
    boolean demoExpanded;
    boolean suppressTextEvents;

    AuthFormBase(Activity activity, UiHost host, AuthMode initial) {
        this.activity = activity;
        this.host = host;
        root = activity.getLayoutInflater().inflate(R.layout.auth_unified, null, false);
        roles = require(R.id.auth_roles);
        nameSection = require(R.id.auth_name_section);
        confirmSection = require(R.id.auth_confirm_section);
        modeActions = require(R.id.auth_mode_actions);
        demoPanel = require(R.id.auth_demo_panel);
        demoAccounts = require(R.id.auth_demo_accounts);
        title = require(R.id.auth_mode_title);
        subtitle = require(R.id.auth_mode_subtitle);
        identifierLabel = require(R.id.auth_identifier_label);
        error = require(R.id.auth_error);
        success = require(R.id.auth_success);
        disclosure = require(R.id.auth_disclaimer);
        name = require(R.id.auth_name);
        identifier = require(R.id.auth_identifier);
        password = require(R.id.auth_password);
        passwordAgain = require(R.id.auth_password_confirm);
        togglePassword = require(R.id.auth_toggle_password);
        submit = require(R.id.auth_submit);
        register = require(R.id.auth_register);
        forgot = require(R.id.auth_forgot);
        back = require(R.id.auth_back);
        demoToggle = require(R.id.auth_demo_toggle);
        demoCustomer = require(R.id.auth_demo_customer);
        demoCashier = require(R.id.auth_demo_cashier);
        demoManager = require(R.id.auth_demo_manager);

        attachListeners();
        setMode(initial, true);
    }

    protected <T extends View> T require(int viewId) {
        T found = root.findViewById(viewId);
        if (found == null)
            throw new IllegalStateException("Missing native auth layout view: " + viewId);
        return found;
    }

    public View view(){return root;}
    protected abstract void attachListeners();
    protected abstract void setMode(AuthMode mode,boolean clear);
}
