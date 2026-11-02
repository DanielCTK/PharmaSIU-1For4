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
public abstract class AccountAddress extends AccountProfile {
    protected AccountAddress(Activity a,UiHost h,DemoState.CustomerProfile p,
        AccountOnboardingProgress progress,String route){super(a,h,p,progress,route);}
    protected void addresses() {
        hero("SỔ ĐỊA CHỈ", "Địa chỉ nhận hàng",
            "Địa chỉ hồ sơ không phải địa điểm giữ hàng hay nơi nhận đã xác nhận.", true);
        if (state.myAddresses().isEmpty()) {
            LinearLayout empty = card();
            text(empty, "Chưa có địa chỉ", 19, R.color.forest, true, 0);
            text(empty, "Bạn chưa lưu địa chỉ nhận hàng nào. Bấm nút bên dưới để thêm địa chỉ.",
                13, R.color.muted, false, 8);
        }
        for (DemoState.Address address : new ArrayList<>(state.myAddresses())) {
            LinearLayout c = card();
            text(c, address.label + (address.isDefault ? "   ✓ Mặc định" : ""),
                17, R.color.emerald, true, 0);
            text(c, address.receiver + "  •  " + address.phone, 14, R.color.ink, true, 7);
            text(c, address.detail, 13, R.color.muted, false, 5);
            action(c, "Sửa địa chỉ", false, () -> addressForm(address));
            if (!address.isDefault) {
                action(c, "Đặt làm mặc định", false, () -> {
                    if (state.saveAddress(address.id, address.label, address.receiver,
                        address.phone, address.detail, true)) draw();
                    else host.message("Không thể thay đổi địa chỉ mặc định.");
                });
            }
            action(c, "Xóa địa chỉ", false, () ->
                new AlertDialog.Builder(activity)
                    .setTitle("Xóa địa chỉ này?")
                    .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này?")
                    .setNegativeButton("Giữ lại", null)
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (state.deleteAddress(address.id)) draw();
                        else host.message("Không thể xóa địa chỉ này.");
                    }).show());
        }
        action(body, "+ Thêm địa chỉ mới", true, () -> addressForm(null));
        disclaimer();
    }

    protected void addressForm(DemoState.Address edit) {
        header.removeAllViews(); body.removeAllViews(); scroll.scrollTo(0, 0);
        hero("SỔ ĐỊA CHỈ", edit == null ? "Thêm địa chỉ" : "Sửa địa chỉ",
            "Điểm nhận Click & Collect được chọn riêng khi tạo yêu cầu.", false);
        LinearLayout form = card();
        EditText label = field(form, "Nhãn (Nhà / Công ty)", edit == null ? "Nhà" : edit.label,
            InputType.TYPE_CLASS_TEXT, 24);
        EditText receiver = field(form, "Người liên hệ", edit == null ? "" : edit.receiver,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS, 70);
        EditText number = field(form, "Số điện thoại", edit == null ? profile.phone : edit.phone,
            InputType.TYPE_CLASS_PHONE, 11);
        EditText detail = field(form, "Địa chỉ chi tiết (tối thiểu 8 ký tự)", edit == null ? "" : edit.detail,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, 190);
        detail.setSingleLine(false); detail.setMinLines(3);
        detail.setLayoutParams(params(-1, dp(94), 7));
        action(form, "Lưu địa chỉ", true, () -> {
            String l = value(label), r = value(receiver), p = value(number), d = value(detail);
            if (l.isEmpty() || r.length() < 2 || !phoneValid(p) || d.length() < 8) {
                host.message("Kiểm tra nhãn, người liên hệ, SĐT 10–11 số và địa chỉ tối thiểu 8 ký tự.");
                return;
            }
            if (state.saveAddress(edit == null ? null : edit.id, l, r, p, d,
                    edit == null ? state.myAddresses().isEmpty() : edit.isDefault)) {
                host.message("Đã lưu địa chỉ thành công");
                draw();
            } else host.message("Không thể lưu địa chỉ.");
        });
        action(body, "Quay lại sổ địa chỉ", false, this::draw);
        disclaimer();
    }
}
