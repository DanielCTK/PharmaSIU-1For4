package com.pharmasiu.mobile.ui.customer.catalog;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Customer-facing merchandising groups. Never changes stored categories or Rx status.
 * Audited VTYT IDs come from the bundled 99-item catalog; the VTYT prefix denotes
 * supplies in general, not a claim that every item is a medical device.
 */
public final class CatalogCategories {
    public static final String ALL="Tất cả";
    public static final String DEVICES="Thiết bị";
    public static final String SUPPLIES="Vật tư y tế";
    public static final String CARE="Chăm sóc";
    private CatalogCategories() { }

    public static String categoryOf(DemoState.Product product) {
        if(product==null)return "Sản phẩm khác";
        if(product.requiresPrescription)return "Kê đơn";
        String raw=product.category==null?"":product.category.trim();
        // Drug classification remains the repository's declaration, never guessed from a name.
        if("khong ke don".equals(CatalogSupport.normalize(raw)))return "Không kê đơn";
        String code=product.id==null?"":product.id.trim().toUpperCase(Locale.ROOT);
        if("VTYT-012".equals(code)||"TIEUDUNG-008".equals(code))return DEVICES;
        if("VTYT-006".equals(code)||"VTYT-009".equals(code)||"VTYT-010".equals(code))return CARE;
        if(code.startsWith("VTYT-"))return SUPPLIES;
        if(code.startsWith("TPCN-"))return "Thực phẩm bổ sung";
        if(code.startsWith("TIEUDUNG-"))return "Tiêu dùng";
        if("thiet bi".equals(CatalogSupport.normalize(raw)))return DEVICES;
        return raw.isEmpty()?"Sản phẩm khác":raw;
    }

    public static boolean matches(DemoState.Product product,String selected) {
        if(product==null)return false;
        String selection=CatalogSupport.normalize(selected);
        if(selection.isEmpty()||"tat ca".equals(selection)||"danh cho ban".equals(selection))return true;
        if("ke don".equals(selection))return product.requiresPrescription;
        String group=CatalogSupport.normalize(categoryOf(product));
        if("thuc pham".equals(selection))return "thuc pham".equals(group)||"thuc pham bo sung".equals(group);
        return selection.equals(group);
    }

    public static List<String> available(List<DemoState.Product> products) {
        Set<String> present=new LinkedHashSet<>();
        for(DemoState.Product product:products)present.add(categoryOf(product));
        List<String> groups=new ArrayList<>();
        groups.add(ALL);
        for(String group:Arrays.asList("Mỹ phẩm",CARE,SUPPLIES,DEVICES,"Thực phẩm",
                "Thực phẩm bổ sung","Không kê đơn","Kê đơn","Tiêu dùng")) {
            if(present.remove(group))groups.add(group);
        }
        groups.addAll(present);
        return groups;
    }

    /** Removes only the duplicated legacy category suffix, retaining real pack details. */
    public static String detailOf(DemoState.Product product) {
        if(product==null)return "";
        String detail=product.subtitle==null?"":product.subtitle.trim();
        int divider=detail.lastIndexOf(" · ");
        if(divider>=0 && CatalogSupport.normalize(detail.substring(divider+3))
                .equals(CatalogSupport.normalize(product.category)))return detail.substring(0,divider).trim();
        return detail;
    }

    public static int iconRes(String group) {
        String category=CatalogSupport.normalize(group);
        if("my pham".equals(category))return R.drawable.catalog_redesign_cosmetic;
        if("thuc pham".equals(category))return R.drawable.catalog_redesign_food;
        if("thuc pham bo sung".equals(category))return R.drawable.catalog_polish_supplement;
        if("thiet bi".equals(category))return R.drawable.catalog_redesign_device;
        if("vat tu y te".equals(category))return R.drawable.catalog_polish_supplies;
        if("cham soc".equals(category)||"danh cho ban".equals(category))return R.drawable.catalog_redesign_care;
        if("khong ke don".equals(category)||"ke don".equals(category))return R.drawable.catalog_polish_medicine;
        if("tieu dung".equals(category))return R.drawable.nav_cart;
        return R.drawable.nav_catalog;
    }
}
