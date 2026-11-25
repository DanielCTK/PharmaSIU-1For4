package com.pharmasiu.mobile.ui.customer.catalog;

import android.app.Activity;
import android.app.Dialog;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.UiMotion;

import java.util.Locale;

import static com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.*;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport.NavFilter;

public final class CustomerProductScreen extends CatalogBase {
    private Dialog photoPreview;

    public CustomerProductScreen(Activity a, UiHost host, NavFilter nav) { super(a, host, nav); }

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

        View root = a.getLayoutInflater().inflate(R.layout.catalog_v2_product, null, false);
        View back = root.findViewById(R.id.catalog_v2_product_back);
        back.setOnClickListener(v -> a.onBackPressed());
        View backBottom = root.findViewById(R.id.catalog_v2_product_back_bottom);
        backBottom.setOnClickListener(v -> host.navigate("catalog"));
        View search = root.findViewById(R.id.catalog_v2_product_search);
        search.setOnClickListener(v -> {
            nav.category = ALL;
            nav.query = "";
            host.navigate("catalog");
        });
        TextView cart = root.findViewById(R.id.catalog_v2_product_cart);
        onCartChanged = () -> bindCart(cart);
        onCartChanged.run();

        FrameLayout media = root.findViewById(R.id.catalog_v2_product_media);
        int widthDp = a.getResources().getConfiguration().screenWidthDp;
        media.getLayoutParams().height = dp(a, Math.max(248, Math.min(340, widthDp * .82f)));
        View artwork = ProductImageLoader.createArtwork(a, p);
        artwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        media.addView(artwork, 0, new FrameLayout.LayoutParams(-1, -1));
        TextView mediaTag = root.findViewById(R.id.catalog_v2_product_media_tag);
        mediaTag.setText("Rx · Cần toa");
        mediaTag.setVisibility(p.requiresPrescription ? View.VISIBLE : View.GONE);
        View zoom = root.findViewById(R.id.catalog_v2_product_zoom);
        boolean hasPhoto = artwork instanceof ImageView;
        zoom.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        if (hasPhoto) {
            Runnable openPhoto = () -> {
                if (a.isFinishing() || a.isDestroyed() || (photoPreview != null && photoPreview.isShowing())) return;
                photoPreview = ProductDetailPhotoDialog.show(a, p);
                if (photoPreview != null) photoPreview.setOnDismissListener(dialog -> photoPreview = null);
            };
            zoom.setContentDescription("Phóng to ảnh " + p.name);
            zoom.setOnClickListener(v -> openPhoto.run());
            media.setOnClickListener(v -> openPhoto.run());
            media.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            UiMotion.press(zoom);
        }

        String category = CatalogCategories.categoryOf(p);
        ((TextView) root.findViewById(R.id.catalog_v2_product_category)).setText(category);
        TextView name = root.findViewById(R.id.catalog_v2_product_name);
        name.setText(p.name);
        name.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
        name.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        String pack = CatalogCategories.detailOf(p);
        TextView subtitle = root.findViewById(R.id.catalog_v2_product_subtitle);
        subtitle.setText(pack);
        subtitle.setVisibility(pack.isEmpty() || pack.equalsIgnoreCase(p.unit) ? View.GONE : View.VISIBLE);
        ((TextView) root.findViewById(R.id.catalog_v2_product_price)).setText(
            p.priceVnd > 0 ? money(p.priceVnd) : "Chờ báo giá");
        ((TextView) root.findViewById(R.id.catalog_v2_product_price_note)).setText(
            p.priceVnd > 0 ? "Giá tham khảo / " + p.unit : "Giá sẽ được nhà thuốc xác nhận.");
        ((TextView) root.findViewById(R.id.catalog_v2_product_description)).setText(description(p));
        ((TextView) root.findViewById(R.id.catalog_v2_product_ingredients)).setText("Thành phần: " + ingredients(p));
        ((TextView) root.findViewById(R.id.catalog_v2_product_spec)).setText(
            "Quy cách: " + (pack.isEmpty() ? p.unit : pack));
        ((TextView) root.findViewById(R.id.catalog_v2_product_sku)).setText("Mã sản phẩm: " + p.id);
        ((TextView) root.findViewById(R.id.catalog_v2_product_stock)).setText(
            state.stock.containsKey(p.id)
                ? "Tồn trên thiết bị: " + state.stock.get(p.id) + " " + p.unit.toLowerCase(new Locale("vi", "VN"))
                : "Chưa có dữ liệu tồn kho trên thiết bị.");
        TextView caveat = root.findViewById(R.id.catalog_v2_product_food_caveat);
        if ("Thực phẩm bổ sung".equals(category)) {
            caveat.setVisibility(View.VISIBLE);
            caveat.setText("Sản phẩm này không phải là thuốc và không có tác dụng thay thế thuốc chữa bệnh.");
        } else if ("Thực phẩm".equals(category)) {
            caveat.setVisibility(View.VISIBLE);
            caveat.setText("Đây là thực phẩm, không phải thuốc.");
        }
        ((TextView) root.findViewById(R.id.catalog_v2_product_process)).setText(safetyNotice(p));

        TextView favorite = root.findViewById(R.id.catalog_v2_product_favorite);
        Runnable syncFavorite = () -> {
            boolean saved = state.favorites.contains(p.id);
            setIcon(favorite, saved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            favorite.setSelected(saved);
            favorite.setContentDescription((saved ? "Bỏ yêu thích " : "Lưu yêu thích ") + p.name);
        };
        syncFavorite.run();
        favorite.setOnClickListener(v -> {
            state.toggleFavorite(p.id);
            syncFavorite.run();
            UiMotion.badge(favorite);
            host.message(state.favorites.contains(p.id) ? "Đã lưu vào danh sách yêu thích" : "Đã bỏ yêu thích");
        });

        Button add = root.findViewById(R.id.catalog_v2_product_add);
        add.setText(p.requiresPrescription ? "Thêm & chọn toa" : "Thêm vào giỏ");
        add.setContentDescription(p.requiresPrescription
            ? "Thêm " + p.name + " và chuyển sang chọn toa; chưa được dược sĩ thẩm định"
            : "Chọn số lượng " + p.name + " để thêm vào giỏ");
        add.setOnClickListener(v -> {
            if (!DemoState.CUSTOMER.equals(state.role)) {
                host.message("Vui lòng đăng nhập tài khoản khách hàng để mua hàng");
                return;
            }
            DemoState.Product current = state.product(p.id);
            if (current == null) {
                host.message("Sản phẩm đã thay đổi. Vui lòng chọn lại trong danh mục.");
                return;
            }
            if (!current.requiresPrescription) {
                showQuantityPicker(current);
                return;
            }
            if (!state.addToCart(current.id)) {
                host.message("Giỏ hàng đã đạt giới hạn 20 sản phẩm / loại.");
                return;
            }
            add.setEnabled(false);
            host.message("Đã thêm thuốc kê đơn vào giỏ hàng. Vui lòng đính kèm đơn thuốc.");
            host.navigate("prescription");
        });
        ((TextView) root.findViewById(R.id.catalog_v2_product_cta_note)).setText(
            p.requiresPrescription ? "Cần toa · Chưa được dược sĩ thẩm định"
                : "Demo cục bộ · Chưa xác nhận giá, tồn hoặc thanh toán");
        UiMotion.press(add);
        UiMotion.press(favorite);
        UiMotion.press(back);
        UiMotion.press(backBottom);
        UiMotion.press(search);
        UiMotion.press(cart);
        return root;
    }
}
