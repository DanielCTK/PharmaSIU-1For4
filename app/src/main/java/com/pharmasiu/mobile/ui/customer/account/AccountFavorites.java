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
public abstract class AccountFavorites extends AccountAddress {
    protected AccountFavorites(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}
    protected void favorites() {
        hero("DANH SÁCH  /  ĐÃ LƯU", "Sản phẩm yêu thích",
            state.favorites.size() + " mặt hàng trong danh sách yêu thích.", true);
        if (state.favorites.isEmpty()) {
            LinearLayout empty = card();
            text(empty, "Chưa có sản phẩm yêu thích", 18, R.color.forest, true, 0);
            text(empty, "Lưu sản phẩm từ danh mục hoặc màn chi tiết để xem lại.",
                13, R.color.muted, false, 8);
            action(empty, "Khám phá danh mục", true, () -> host.navigate("catalog"));
        } else {
            for (String id : new ArrayList<>(state.favorites)) {
                DemoState.Product product = state.product(id);
                if (product == null) continue;
                LinearLayout c = card();
                text(c, product.name, 17, R.color.ink, true, 0);
                text(c, product.subtitle, 13, R.color.muted, false, 6);
                text(c, product.requiresPrescription ? "Cần thẩm định toa • RX" :
                    "Thông tin tham khảo • " + product.category, 12,
                    product.requiresPrescription ? R.color.warning : R.color.emerald, true, 8);
                text(c, product.priceVnd <= 0 ? "Giá chưa xác nhận" :
                    String.format(new Locale("vi", "VN"), "%,d ₫ (Đã gồm VAT)", product.priceVnd),
                    15, R.color.emerald, true, 8);
                action(c, "Xem chi tiết", true, () -> {
                    state.selectedProductId = id;
                    host.navigate("product");
                });
                action(c, "Bỏ yêu thích", false, () -> {
                    state.toggleFavorite(id);
                    draw();
                });
            }
        }
        disclaimer();
    }
    protected View onboarding() {
        ScrollView root = (ScrollView) LayoutInflater.from(activity)
            .inflate(R.layout.customer_account_onboarding, null, false);
        TextView index = root.findViewById(R.id.customer_onboarding_index);
        TextView title = root.findViewById(R.id.customer_onboarding_title);
        TextView detail = root.findViewById(R.id.customer_onboarding_detail);
        TextView artwork = root.findViewById(R.id.customer_onboarding_artwork);
        TextView progress = root.findViewById(R.id.customer_onboarding_progress);
        Button next = root.findViewById(R.id.customer_onboarding_next);
        Button skip = root.findViewById(R.id.customer_onboarding_skip);
        String[] headings = {
            "Chăm sóc mỗi ngày.", "Khám phá theo nhu cầu.", "Đặt trước • nhận tại quầy."
        };
        String[] descriptions = {
            "Khám phá PharmaSIU với giao diện Emerald thân thiện, các nhóm hàng được trình bày rõ ràng.",
            "Tìm sản phẩm chăm sóc sức khỏe, mỹ phẩm, thực phẩm và thiết bị theo nhu cầu.",
            "Đặt giữ hàng nhanh chóng và nhận hàng tiện lợi tại quầy thuốc PharmaSIU gần nhất."
        };
        String[] symbols = { "✦", "✳", "⌂" };
        Runnable update = () -> {
            int page = Math.max(0, Math.min(2, onboardingProgress.step));
            index.setText(String.format(Locale.ROOT, "0%d  /  03", page + 1));
            title.setText(headings[page]);
            detail.setText(descriptions[page]);
            artwork.setText(symbols[page]);
            progress.setText(page == 0 ? "●  ○  ○" : page == 1 ? "○  ●  ○" : "○  ○  ●");
            next.setText(page == 2 ? "Khám phá ngay" : "Tiếp tục");
        };
        Runnable finish = () -> {
            onboardingProgress.viewed = true;
            onboardingProgress.step = 0;
            host.navigate("home");
        };
        next.setOnClickListener(v -> {
            if (onboardingProgress.step >= 2) finish.run();
            else { onboardingProgress.step++; update.run(); }
        });
        skip.setOnClickListener(v -> finish.run());
        update.run();
        return root;
    }
}
