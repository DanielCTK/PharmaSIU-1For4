package com.pharmasiu.mobile.ui.customer.orders;

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
import com.pharmasiu.mobile.ui.design.PharmaUi;
import static com.pharmasiu.mobile.ui.customer.CustomerOrderViews.*;

public final class OrderScreen extends OrderHistoryScreens {
    public OrderScreen(Activity activity,UiHost host,int layout){super(activity,host,layout);}
    public View display(String route){
        switch(route){
            case "cart-v2":cart();break;
            case "rx-v2":rx();break;
            case "checkout-v2":checkout();break;
            case "orders-v2":orders();break;
            case "order-detail-v2":detail();break;
            case "receipt-v2":receipt();break;
            default:throw new IllegalArgumentException("Unsupported customer order route");
        }
        return root;
    }
}
