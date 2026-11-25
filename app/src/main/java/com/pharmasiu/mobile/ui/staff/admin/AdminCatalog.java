package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.view.Gravity;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.design.PharmaUi;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import com.google.android.material.button.MaterialButton;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogCategories;
import com.pharmasiu.mobile.ui.customer.catalog.CatalogSupport;
import com.pharmasiu.mobile.ui.staff.StaffMediaViews;
import com.pharmasiu.mobile.ui.design.PharmaUi;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;
import static com.pharmasiu.mobile.ui.staff.admin.AdminMedia.*;

/** Single feature screen and its closely related business UI. */
public final class AdminCatalog {
    private AdminCatalog() {}

    public static View catalog(Activity a, UiHost host) {
        DemoState s = host.state();
        Screen screen = new Screen(a, R.layout.staff_admin_catalog, host);
        screen.context(s.products.size() + " mặt hàng · Giá và ảnh sản phẩm");
        LinearLayout editor = productSection(screen);
        View editorPanel=(View)editor.getParent();
        editorPanel.setVisibility(View.GONE);
        title(editor, "Sửa mặt hàng đã có");
        Spinner select = picker(editor, productLabels(s), a);
        FrameLayout selectedPhoto=new FrameLayout(a);
        LinearLayout.LayoutParams selectedPhotoParams=new LinearLayout.LayoutParams(dp(a,92),dp(a,104));
        selectedPhotoParams.gravity=Gravity.CENTER_HORIZONTAL;
        selectedPhotoParams.topMargin=dp(a,8);
        editor.addView(selectedPhoto,selectedPhotoParams);
        TextView selectedInfo=line(editor,"",14,R.color.muted,false);
        Button editPhoto=button(editor,"Xem / đổi ảnh",false,() -> {
            int index=select.getSelectedItemPosition();
            if(index>=0 && index<s.products.size())showProductPhoto(a,host,s.products.get(index));
        });
        icon(editPhoto,R.drawable.admin_catalog_photo,false,a);
        EditText name = input(editor, "Tên mặt hàng", InputType.TYPE_CLASS_TEXT, "");
        EditText price = input(editor, "Giá niêm yết (VND)", InputType.TYPE_CLASS_NUMBER, "");
        Runnable load = () -> {
            int index = select.getSelectedItemPosition();
            if (index < 0 || index >= s.products.size()) return;
            DemoState.Product p = s.products.get(index);
            selectedPhoto.removeAllViews();
            selectedPhoto.addView(StaffMediaViews.createThumbnail(a,host,p,StaffMediaViews.demoPhoto(s,p.id),
                () -> showProductPhoto(a,host,p)),new FrameLayout.LayoutParams(-1,-1));
            selectedInfo.setText(CatalogCategories.categoryOf(p)+" · "+p.id);
            name.setText(p.name);
            price.setText(p.priceVnd > 0 ? String.valueOf(p.priceVnd) : "");
            boolean allowed = !p.requiresPrescription;
            name.setEnabled(allowed);
            price.setEnabled(allowed);
        };
        select.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { load.run(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        load.run();
        button(editor, "Lưu thông tin sản phẩm", true, () -> {
            if (!s.canAdmin()) { host.message("Không đủ quyền Quản lý"); return; }
            int index = select.getSelectedItemPosition();
            if (index < 0 || index >= s.products.size()) return;
            Long amount = positiveLong(price.getText().toString(), MAX_PRICE);
            if (amount == null || s.posCart.containsKey(s.products.get(index).id) ||
                !s.renameProduct(s.products.get(index).id,
                name.getText().toString(), amount)) {
                host.message("Tên/giá không hợp lệ, Rx bị khóa hoặc mặt hàng đang ở giỏ POS");
            } else {
                host.message("Đã cập nhật thông tin sản phẩm thành công");
                host.navigate("catalog-admin-v2");
            }
        });
        warning(editor, "Thuốc Rx bị khóa. Mặt hàng đang ở giỏ POS chưa thể sửa.");
        button(editor,"Đóng chỉnh sửa",false,() -> hidePanel(a,editorPanel));

        LinearLayout create = productSection(screen);
        View createPanel=(View)create.getParent();
        createPanel.setVisibility(View.GONE);
        title(create, "Thêm mặt hàng mới");
        EditText newName = input(create, "Tên hàng mới", InputType.TYPE_CLASS_TEXT, "");
        Spinner category = picker(create, NON_RX_CATEGORIES, a);
        EditText unit = input(create, "Đơn vị (hộp, chai...)", InputType.TYPE_CLASS_TEXT, "");
        EditText newPrice = input(create, "Giá bán (VND)", InputType.TYPE_CLASS_NUMBER, "");
        EditText initial = input(create, "Tồn mở đầu từ 0 đến 999", InputType.TYPE_CLASS_NUMBER, "0");
        EditText lotCode = input(create, "Mã lô mở đầu (nếu có tồn)", InputType.TYPE_CLASS_TEXT, "");
        EditText expires = input(create, "Hạn YYYY-MM-DD (nếu có tồn)", InputType.TYPE_CLASS_DATETIME, "");
        final Uri[] newPhoto = {null};
        TextView newPhotoStatus=line(create,"Ảnh không bắt buộc",14,R.color.muted,false);
        button(create, "Chọn ảnh từ thiết bị (không bắt buộc)", false, () ->
            pickPhoto(a, host, uri -> {
                newPhoto[0] = uri;
                newPhotoStatus.setText("Đã chọn ảnh từ thiết bị");
                host.message("Đã chọn ảnh; hãy lưu mặt hàng để hoàn tất");
            }));
        button(create, "Lưu thông tin sản phẩm", true, () -> {
            Long amount = positiveLong(newPrice.getText().toString(), MAX_PRICE);
            Integer qty = boundedInt(initial.getText().toString(), 0, MAX_RECEIVE);
            String categoryValue = category.getSelectedItem() == null ? "" : category.getSelectedItem().toString();
            LocalDate expiry = parseDate(expires.getText().toString());
            Set<String> beforeProductIds=new HashSet<>();
            for(DemoState.Product existing:s.products)beforeProductIds.add(existing.id);
            if (amount == null || qty == null || !createDemoProduct(s,
                newName.getText().toString(), categoryValue, unit.getText().toString(),
                amount, qty, lotCode.getText().toString(), expiry)) {
                host.message("Kiểm tra tên, giá, tồn, mã lô và hạn dùng; không tạo thuốc Rx");
            } else {
                boolean photoApplied=newPhoto[0]==null;
                if(newPhoto[0]!=null) {
                    DemoState.Product created=null;
                    for(DemoState.Product candidate:s.products)if(!beforeProductIds.contains(candidate.id)) {
                        if(created!=null){created=null;break;}
                        created=candidate;
                    }
                    if(created!=null)try {
                        photoApplied=StaffMediaViews.changeDemoPhoto(s,created.id,newPhoto[0],
                            a.getContentResolver().getType(newPhoto[0]));
                    } catch(RuntimeException unavailableImage) { photoApplied=false; }
                }
                host.message(photoApplied?"Đã lưu mặt hàng":"Đã lưu mặt hàng; chưa thể gắn ảnh đã chọn");
                host.navigate("catalog-admin-v2");
            }
        });
        warning(create, "Có tồn mở đầu: cần mã lô và hạn dùng. Ảnh chỉ gắn trong phiên hiện tại.");
        button(create,"Đóng biểu mẫu",false,() -> hidePanel(a,createPanel));

        LinearLayout listing = productSection(screen);
        title(listing, "Danh mục");
        EditText catalogSearch = input(listing, "Tìm tên, mã hoặc nhóm hàng", InputType.TYPE_CLASS_TEXT, "");
        catalogSearch.setContentDescription("Tìm hàng hóa theo tên, mã hoặc nhóm");
        TextView catalogSummary = line(listing, "", 12, R.color.muted, false);
        LinearLayout itemsContainer = new LinearLayout(a);
        itemsContainer.setOrientation(LinearLayout.VERTICAL);
        listing.addView(itemsContainer, new LinearLayout.LayoutParams(-1, -2));

        final int PAGE_SIZE = 8;
        final int[] catalogPage = {0};

        Runnable renderList = new Runnable() {
            @Override
            public void run() {
                itemsContainer.removeAllViews();
                String query = CatalogSupport.normalize(catalogSearch.getText().toString());
                List<DemoState.Product> matched = new ArrayList<>();
                for (DemoState.Product p : s.products) {
                    if (query.isEmpty() || CatalogSupport.normalize(p.name+" "+p.id+" "+CatalogCategories.categoryOf(p)).contains(query)) {
                        matched.add(p);
                    }
                }
                int total = matched.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
                if (catalogPage[0] >= totalPages) catalogPage[0] = totalPages - 1;
                if (catalogPage[0] < 0) catalogPage[0] = 0;

                int start = catalogPage[0] * PAGE_SIZE;
                int end = Math.min(start + PAGE_SIZE, total);
                catalogSummary.setText(total+" sản phẩm · "+(total==0?0:start+1)+"–"+end);

                if (total == 0) {
                    line(itemsContainer, "Không có sản phẩm nào khớp tìm kiếm", 13, R.color.muted, false);
                } else {
                    for (int i = start; i < end; i++) {
                        DemoState.Product p = matched.get(i);
                        View row=productRow(a,host,p,() -> {
                            int selected=s.products.indexOf(p);
                            if(selected>=0)select.setSelection(selected);
                            createPanel.setVisibility(View.GONE);
                            editorPanel.setVisibility(View.VISIBLE);
                            scrollToPanel(screen,editorPanel);
                        });
                        LinearLayout.LayoutParams rowParams=new LinearLayout.LayoutParams(-1,-2);
                        itemsContainer.addView(row,rowParams);
                    }
                }

                if (totalPages > 1) {
                    PharmaUi.pagination(itemsContainer, catalogPage[0], totalPages, total, "sản phẩm", newPage -> {
                        catalogPage[0] = newPage;
                        this.run();
                        if (screen.scroll != null) {
                            scrollToPanel(screen,(View)listing.getParent());
                        }
                    });
                }
            }
        };

        catalogSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                catalogPage[0] = 0;
                renderList.run();
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        renderList.run();
        LinearLayout actions=new LinearLayout(a);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button addAction=button(actions,"Thêm hàng",true,() -> {
            editorPanel.setVisibility(View.GONE);
            createPanel.setVisibility(View.VISIBLE);
            scrollToPanel(screen,createPanel);
        });
        icon(addAction,R.drawable.admin_catalog_add,true,a);
        Button editAction=button(actions,"Sửa hàng",false,() -> {
            createPanel.setVisibility(View.GONE);
            editorPanel.setVisibility(View.VISIBLE);
            scrollToPanel(screen,editorPanel);
        });
        icon(editAction,R.drawable.admin_catalog_edit,false,a);
        LinearLayout.LayoutParams addParams=new LinearLayout.LayoutParams(0,-2,1f);
        addParams.setMargins(0,0,dp(a,5),dp(a,12));
        addAction.setLayoutParams(addParams);
        LinearLayout.LayoutParams editParams=new LinearLayout.LayoutParams(0,-2,1f);
        editParams.setMargins(dp(a,5),0,0,dp(a,12));
        editAction.setLayoutParams(editParams);
        screen.body.addView(actions,0);
        View listingPanel=(View)listing.getParent();
        screen.body.removeView(listingPanel);
        screen.body.addView(listingPanel,1);
        return screen.root;
    }

    private static View productRow(Activity activity,UiHost host,DemoState.Product product,Runnable edit) {
        View row=activity.getLayoutInflater().inflate(R.layout.admin_catalog_product_row,null,false);
        FrameLayout image=row.findViewById(R.id.admin_catalog_row_photo);
        image.addView(StaffMediaViews.createThumbnail(activity,host,product,StaffMediaViews.demoPhoto(host.state(),product.id),
            () -> showProductPhoto(activity,host,product)),new FrameLayout.LayoutParams(-1,-1));
        ((TextView)row.findViewById(R.id.admin_catalog_row_name)).setText(product.name);
        ((TextView)row.findViewById(R.id.admin_catalog_row_category)).setText(CatalogCategories.categoryOf(product));
        ((TextView)row.findViewById(R.id.admin_catalog_row_sku)).setText("SKU " + product.id);
        ((TextView)row.findViewById(R.id.admin_catalog_row_price)).setText(product.priceVnd>0?vnd(product.priceVnd):"Chờ báo giá");
        int stock = host.state().stock.getOrDefault(product.id, 0);
        ((TextView)row.findViewById(R.id.admin_catalog_row_stock)).setText("Tồn " + stock + " " + product.unit);
        TextView status = row.findViewById(R.id.admin_catalog_row_status);
        boolean hasLots = false;
        for (DemoState.Lot lot : host.state().lots) if (product.id.equals(lot.productId)) { hasLots = true; break; }
        status.setText(product.requiresPrescription ? "Rx · Khóa chỉnh sửa" :
            product.priceVnd <= 0 ? "Chưa có giá bán" : stock < 1 ? "Hết hàng" : lotSummary(host.state(), product.id));
        status.setTextColor(activity.getColor(product.requiresPrescription || product.priceVnd <= 0 || stock < 1
            || (hasLots && host.state().eligibleLotQty(product.id, LocalDate.now()) < 1)
            ? R.color.warning : R.color.emerald));
        ImageButton photoAction=row.findViewById(R.id.admin_catalog_row_photo_action);
        photoAction.setContentDescription("Xem hoặc đổi ảnh "+product.name);
        photoAction.setOnClickListener(v -> showProductPhoto(activity,host,product));
        com.pharmasiu.mobile.ui.design.UiMotion.press(photoAction);
        row.setContentDescription("Chỉnh sửa "+product.name);
        row.setOnClickListener(v -> edit.run());
        com.pharmasiu.mobile.ui.design.UiMotion.press(row);
        return row;
    }

    private static String lotSummary(DemoState state, String productId) {
        int count = 0;
        LocalDate nextExpiry = null;
        LocalDate today = LocalDate.now();
        for (DemoState.Lot lot : state.lots) {
            if (!productId.equals(lot.productId)) continue;
            count++;
            if (!lot.quarantined && lot.quantity > 0 && lot.expiresOn != null && lot.expiresOn.isAfter(today)
                && (nextExpiry == null || lot.expiresOn.isBefore(nextExpiry))) nextExpiry = lot.expiresOn;
        }
        return nextExpiry == null ? (count == 0 ? "Chưa có lô theo dõi" : count + " lô · Không có lô hợp lệ")
            : count + " lô · HSD gần nhất " + nextExpiry.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static void scrollToPanel(Screen screen,View panel) {
        if(screen.scroll!=null)screen.scroll.post(() -> screen.scroll.smoothScrollTo(0,screen.body.getTop()+panel.getTop()));
    }

    private static void hidePanel(Activity activity,View panel) {
        android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(keyboard!=null)keyboard.hideSoftInputFromWindow(panel.getWindowToken(),0);
        panel.setVisibility(View.GONE);
    }

    private static void icon(Button button,int resource,boolean primary,Activity activity) {
        if(button instanceof MaterialButton) {
            MaterialButton material=(MaterialButton)button;
            material.setIconResource(resource);
            material.setIconTint(ColorStateList.valueOf(activity.getColor(primary?R.color.white:R.color.emerald)));
            material.setIconSize(dp(activity,20));
        }
    }

    /**
     * A small UI-owned RAM mutation, because the current DemoState has no add-product
     * method. All validations happen before insertion. A newly counted quantity is
     * only admitted with an eligible dated demo lot.
     */
    public static boolean createDemoProduct(DemoState s, String name, String category,
                                     String unit, long price, int openingQty,
                                     String lotCode, LocalDate expiry) {
        if (s != null && s.isDatabaseBacked()) {
            if (!s.canAdmin()) return false;
            boolean saved=s.repository().createProduct(s.authenticatedAccountId(),
                name,unit,price,openingQty,lotCode,expiry);
            if(saved)s.refreshDatabaseViews();
            return saved;
        }
        if (s == null || !s.canAdmin() || name == null || unit == null ||
            name.trim().length() < 2 || name.trim().length() > 90 ||
            unit.trim().isEmpty() || unit.trim().length() > 24 ||
            price < 1 || price > MAX_PRICE || openingQty < 0 || openingQty > MAX_RECEIVE) return false;
        boolean recognized = false;
        for (String value : NON_RX_CATEGORIES) if (value.equals(category)) recognized = true;
        if (!recognized) return false;
        for (DemoState.Product p : s.products)
            if (p.name.trim().equalsIgnoreCase(name.trim())) return false;
        if (openingQty > 0) {
            if (lotCode == null || lotCode.trim().length() < 2 || lotCode.trim().length() > 40 ||
                expiry == null || !expiry.isAfter(LocalDate.now())) return false;
            for (DemoState.Lot lot : s.lots)
                if (lot.code.equalsIgnoreCase(lotCode.trim())) return false;
        }
        int serial = s.products.size() + 1;
        String id;
        do { id = String.format(Locale.ROOT, "admin-demo-%04d", serial++); }
        while (s.product(id) != null);
        DemoState.Product p = new DemoState.Product(id, name.trim(),
            "Sản phẩm lưu hành nội bộ", price, category, false, unit.trim(),
            "Mặt hàng lưu hành nội bộ theo tiêu chuẩn nhà thuốc PharmaSIU.");
        s.products.add(p);
        s.stock.put(id, 0);
        if (openingQty > 0 && !s.receiveLot(id, lotCode.trim(), openingQty, expiry)) {
            s.products.remove(p);
            s.stock.remove(id);
            return false;
        }
        return true;
    }
}
