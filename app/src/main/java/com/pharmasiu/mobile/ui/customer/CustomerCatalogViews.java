package com.pharmasiu.mobile.ui.customer;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.customer.catalog.*;
import java.util.List;

/** Stable customer catalog API; feature screens and shared cards live in ui.customer.catalog. */
public final class CustomerCatalogViews {
    private CustomerCatalogViews() {}
    public static View create(Activity activity,UiHost host,String route) {
        if(activity==null || host==null || host.state()==null)
            throw new IllegalArgumentException("Missing customer view host");
        DemoState state=host.state();
        if(!DemoState.CUSTOMER.equals(state.role)){
            TextView denied=new TextView(activity);
            denied.setText("Chỉ tài khoản khách hàng mới có thể mở danh mục và thêm vào giỏ.");
            denied.setPadding(24,24,24,24);
            return denied;
        }
        CatalogSupport.NavFilter filter=CatalogSupport.filterFor(state);
        if("catalog".equals(route)||"catalog-v2".equals(route))
            return new CustomerBrowseScreen(activity,host,filter).catalog();
        if("product".equals(route)||"product-v2".equals(route))
            return new CustomerProductScreen(activity,host,filter).product();
        return new CustomerHomeScreen(activity,host,filter).home();
    }
    public static String normalize(String value){return CatalogSupport.normalize(value);}
    public static List<DemoState.Product> filterProducts(List<DemoState.Product> products,
                                       String query,String category,String sort){
        return CatalogSupport.filterProducts(products,query,category,sort);
    }
}
