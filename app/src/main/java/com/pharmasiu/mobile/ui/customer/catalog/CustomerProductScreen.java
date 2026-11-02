package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.NavFilter;
public final class CustomerProductScreen extends CatalogBase {
    public CustomerProductScreen(Activity a, UiHost host, NavFilter nav){super(a,host,nav);}
    public View product() {
        DemoState.Product p = state.product(state.selectedProductId);
        if (p == null) {
            LinearLayout missing = new LinearLayout(a);
            missing.setOrientation(LinearLayout.VERTICAL);
            missing.setPadding(dp(a,20),dp(a,25),dp(a,20),dp(a,20));
            TextView detail = new TextView(a);
            detail.setText("Không tìm thấy sản phẩm. Vui lòng quay lại danh mục để chọn sản phẩm khác.");
            detail.setTextColor(WARNING);
            detail.setTextSize(15);
            missing.addView(detail, new LinearLayout.LayoutParams(-1,-2));
            Button back = new Button(a);
            back.setText("Quay lại danh mục");
            back.setAllCaps(false);
            LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1,dp(a,52));
            backParams.topMargin = dp(a,16);
            missing.addView(back,backParams);
            back.setOnClickListener(v -> host.navigate("catalog"));
            return missing;
        }
        View root = a.getLayoutInflater().inflate(R.layout.catalog_v2_product,null,false);
        root.findViewById(R.id.catalog_v2_product_back).setOnClickListener(v -> host.navigate("catalog"));
        FrameLayout media = root.findViewById(R.id.catalog_v2_product_media);
        media.addView(ProductImageLoader.createArtwork(a,p),0,new FrameLayout.LayoutParams(-1,-1));
        ((TextView) root.findViewById(R.id.catalog_v2_product_media_tag)).setText(
            p.requiresPrescription ? "THUỐC KÊ ĐƠN · Rx" : "THÔNG TIN SẢN PHẨM");
        ((TextView) root.findViewById(R.id.catalog_v2_product_category)).setText(p.category.toUpperCase(new Locale("vi","VN")));
        ((TextView) root.findViewById(R.id.catalog_v2_product_name)).setText(p.name);
        ((TextView) root.findViewById(R.id.catalog_v2_product_subtitle)).setText(subtitle(p));
        ((TextView) root.findViewById(R.id.catalog_v2_product_price)).setText(p.priceVnd > 0 ? money(p.priceVnd) : "Chờ nhà thuốc báo giá");
        ((TextView) root.findViewById(R.id.catalog_v2_product_price_note)).setText(
            p.priceVnd > 0 ? "Giá niêm yết / " + p.unit + " (Đã bao gồm VAT)" :
                "Không có giá xác minh. Không tính mặt hàng chưa có giá vào tạm tính.");
        ((TextView) root.findViewById(R.id.catalog_v2_product_description)).setText(description(p));
        ((TextView) root.findViewById(R.id.catalog_v2_product_ingredients)).setText(
            "Thành phần      Vui lòng tham khảo bao bì hoặc hỏi dược sĩ");
        ((TextView) root.findViewById(R.id.catalog_v2_product_spec)).setText("Quy cách        " + subtitle(p));
        TextView caveat = root.findViewById(R.id.catalog_v2_product_food_caveat);
        if ("Thực phẩm bổ sung".equals(p.category)) {
            caveat.setVisibility(View.VISIBLE);
            caveat.setText("Sản phẩm này không phải là thuốc và không có tác dụng thay thế thuốc chữa bệnh.");
        } else if ("Thực phẩm".equals(p.category)) {
            caveat.setVisibility(View.VISIBLE);
            caveat.setText("Đây là thực phẩm, không phải thuốc.");
        }
        ((TextView) root.findViewById(R.id.catalog_v2_product_process)).setText(p.requiresPrescription ?
            "LƯU Ý VỀ TOA THUỐC\nThuốc kê đơn chỉ cung cấp khi có đơn thuốc hợp lệ từ bác sĩ. Dược sĩ PharmaSIU sẽ liên hệ để thẩm định toa và tư vấn chi tiết trước khi giao nhận." :
            "QUY TRÌNH ĐẶT TRƯỚC\nĐơn hàng được chuẩn bị sẵn tại quầy nhà thuốc PharmaSIU. Quý khách kiểm tra hàng và nhận tại quầy hoặc nhận giao tận nơi.");
        TextView favorite = root.findViewById(R.id.catalog_v2_product_favorite);
        Runnable syncFavorite = () -> {
            boolean saved = state.favorites.contains(p.id);
            favorite.setText("");
            favorite.setCompoundDrawablesWithIntrinsicBounds(saved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline, 0, 0, 0);
            favorite.setContentDescription((saved ? "Bỏ yêu thích " : "Lưu yêu thích ") + p.name);
        };
        syncFavorite.run();
        favorite.setOnClickListener(v -> {
            state.toggleFavorite(p.id);syncFavorite.run();
            host.message(state.favorites.contains(p.id) ? "Đã lưu vào danh sách yêu thích" : "Đã bỏ yêu thích");
        });
        Button add = root.findViewById(R.id.catalog_v2_product_add);
        add.setText(p.requiresPrescription ? "Thêm và gửi kèm toa xét duyệt" : "Thêm vào giỏ hàng");
        add.setOnClickListener(v -> {
            if (!DemoState.CUSTOMER.equals(state.role)) {host.message("Vui lòng đăng nhập tài khoản khách hàng để mua hàng");return;}
            if (!state.addToCart(p.id)) { host.message("Giỏ hàng đã đạt giới hạn 20 sản phẩm / loại."); return; }
            // Disable immediately so a quick double tap cannot add the same item twice.
            add.setEnabled(false);
            if (p.requiresPrescription) {
                host.message("Đã thêm thuốc kê đơn vào giỏ hàng. Vui lòng đính kèm đơn thuốc.");
                host.navigate("prescription");
            } else {
                host.message("Đã thêm sản phẩm vào giỏ hàng thành công.");
                host.navigate("cart");
            }
        });
        ((TextView) root.findViewById(R.id.catalog_v2_product_cta_note)).setText(
            p.requiresPrescription ? "Thuốc kê đơn cần đơn thuốc hợp lệ từ bác sĩ." :
                "Cam kết hàng chính hãng 100% · Đổi trả trong 30 ngày.");
        return root;
    }
}
