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
public final class AccountPresenter extends AccountFavorites {
    public AccountPresenter(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}
    public View create() {
        if ("onboarding".equals(route)) return onboarding();
        scroll = (ScrollView) LayoutInflater.from(activity)
            .inflate(R.layout.customer_account_page, null, false);
        header = scroll.findViewById(R.id.customer_account_header);
        body = scroll.findViewById(R.id.customer_account_content);
        draw();
        return scroll;
    }

    protected void draw() {
        header.removeAllViews();
        body.removeAllViews();
        scroll.scrollTo(0, 0);
        switch (route) {
            case "profile": profile(); break;
            case "addresses": addresses(); break;
            case "change-password": password(); break;
            case "favorites": favorites(); break;
            default: account(); break;
        }
    }
}
