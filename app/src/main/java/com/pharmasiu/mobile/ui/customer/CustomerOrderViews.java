package com.pharmasiu.mobile.ui.customer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.pharmasiu.mobile.ui.customer.orders.OrderScreen;

/** Stable Customer commerce API; feature screen implementations live under ui.customer.orders. */
public final class CustomerOrderViews {
    public static final String PICKUP_A="Điểm nhận mẫu A";
    public static final String PICKUP_B="Điểm nhận mẫu B";
    public static final int MAX_QTY=20;
    private CustomerOrderViews(){}
    public static View create(Activity activity,UiHost host,String route){
        if(activity==null||host==null||host.state()==null)
            throw new IllegalArgumentException("Missing customer UI host");
        if(!DemoState.CUSTOMER.equals(host.state().role))
            return error(activity,"Không đủ quyền","Đăng nhập khách DEMO để mở giỏ và yêu cầu của bạn.");
        if(route==null)return error(activity,"Đường dẫn không hợp lệ","Không xác định màn hình yêu cầu.");
        int layout;
        switch(route){
            case "cart-v2":layout=R.layout.order_v2_cart;break;
            case "rx-v2":layout=R.layout.order_v2_rx;break;
            case "checkout-v2":layout=R.layout.order_v2_checkout;break;
            case "orders-v2":layout=R.layout.order_v2_orders;break;
            case "order-detail-v2":layout=R.layout.order_v2_detail;break;
            case "receipt-v2":layout=R.layout.order_v2_receipt;break;
            default:return error(activity,"Không tìm thấy trang","Đường dẫn DEMO không được hỗ trợ.");
        }
        return new OrderScreen(activity,host,layout).display(route);
    }
    private static View error(Activity activity, String heading, String message) {
        View root = activity.getLayoutInflater().inflate(R.layout.order_v2_error, null, false);
        ((TextView) root.findViewById(R.id.order_v2_error_title)).setText(heading);
        ((TextView) root.findViewById(R.id.order_v2_error_detail)).setText(message);
        return root;
    }
    /** Pure route guard; selection applies only to the current Rx cart fingerprint. */
    public static boolean canAdvance(DemoState state) {
        return state != null && DemoState.CUSTOMER.equals(state.role) &&
            !state.cart.isEmpty() &&
            (!state.hasRx(state.cart) || state.localPrescriptionSelected());
    }

    public static boolean validPickup(String pickup) {
        return PICKUP_A.equals(pickup) || PICKUP_B.equals(pickup);
    }

    /**
     * Button guard + state-level guard: prevents repeated submissions if an
     * event is delivered twice before navigation. Does not create server orders.
     */
    public static DemoState.Order submit(DemoState state, String name, String phone,
                                  String pickup, AtomicBoolean inFlight) {
        if (state == null || inFlight == null || !inFlight.compareAndSet(false, true)) return null;
        DemoState.Order saved = null;
        try {
            if (validPickup(pickup) && canAdvance(state) &&
                name != null && name.trim().length() >= 2 &&
                phone != null && phone.trim().matches("0[0-9]{9,10}")) {
                saved = state.saveRequest(name.trim(), phone.trim(), pickup);
            }
            return saved;
        } finally {
            if (saved == null) inFlight.set(false);
        }
    }

    public static boolean canCancel(DemoState state, DemoState.Order order, String reason) {
        if (state == null || order == null || !DemoState.CUSTOMER.equals(state.role) ||
            !state.customerPhone.equals(order.customerPhone) ||
            !isPending(order) || reason == null) return false;
        String normalized = reason.trim();
        return normalized.length() >= 3 && normalized.length() <= 160;
    }

    public static boolean isPending(DemoState.Order order) {
        return order != null && ("Đã lưu yêu cầu DEMO".equals(order.status)
            || "Đang xử lý".equals(order.status)
            || "Chờ xác minh toa".equals(order.status)
            || "Chờ gửi và xác minh toa".equals(order.status));
    }
    public static DemoState.Order selectedCustomerOrder(DemoState state) {
        if (state == null || !DemoState.CUSTOMER.equals(state.role)) return null;
        for (DemoState.Order order : state.customerOrders()) {
            if (order.reference.equals(state.selectedOrderId)) return order;
        }
        return null;
    }
}
