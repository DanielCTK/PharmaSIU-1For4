package com.pharmasiu.mobile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.navigation.PharmaBottomBar;
import com.pharmasiu.mobile.ui.AuthScreen;
import com.pharmasiu.mobile.ui.design.UiMotion;
import com.pharmasiu.mobile.ui.staff.StaffPosViews;
import com.pharmasiu.mobile.ui.staff.StaffAdminViews;
import com.pharmasiu.mobile.ui.customer.CustomerAccountViews;
import com.pharmasiu.mobile.ui.customer.CustomerOrderViews;
import com.pharmasiu.mobile.ui.customer.CustomerCatalogViews;
import com.pharmasiu.mobile.ui.customer.account.CustomerAvatarStore;

/** Main application entry point, implemented entirely in Android Java Views/XML. */
public final class MainActivity extends Activity implements UiHost {
    private static final int CHOOSE_LOCAL_PRESCRIPTION = 1020;
    private static final int CHOOSE_STAFF_PHOTO = 1021;
    private static final int CHOOSE_CUSTOMER_AVATAR = 1022;
    private String pendingStaffPhotoProductId;
    private long pendingAvatarAccountId = -1L;
    private DemoState store;
    private FrameLayout content;
    private LinearLayout navigation;
    private String route = "login";
    private final java.util.ArrayDeque<String> routeHistory = new java.util.ArrayDeque<>();

    /** Retain navigation and database-backed UI state across configuration changes. */
    private static final class RetainedSession {
        final DemoState store;
        final String route;
        final java.util.ArrayDeque<String> history;
        final String pendingPhoto;
        final long pendingAvatarAccountId;
        RetainedSession(DemoState store, String route, java.util.ArrayDeque<String> history,
                        String pendingPhoto, long pendingAvatarAccountId) {
            this.store=store;
            this.route=route;
            this.history=new java.util.ArrayDeque<>(history);
            this.pendingPhoto=pendingPhoto;
            this.pendingAvatarAccountId=pendingAvatarAccountId;
        }
    }
    @Override public Object onRetainNonConfigurationInstance() {
        return new RetainedSession(store,route,routeHistory,pendingStaffPhotoProductId,pendingAvatarAccountId);
    }
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Object retained=getLastNonConfigurationInstance();
        if(retained instanceof RetainedSession){
            RetainedSession last=(RetainedSession)retained;
            store=last.store;
            route=last.route;
            routeHistory.addAll(last.history);
            pendingStaffPhotoProductId=last.pendingPhoto;
            pendingAvatarAccountId=last.pendingAvatarAccountId;
        }else{
            store=new DemoState(getApplicationContext());
            route="login";
        }
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.main_root);
        content = findViewById(R.id.content_area);
        navigation = findViewById(R.id.navigation_bar);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets sysBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            boolean isKeyboardOpen = ime.bottom > 0 || windowInsets.isVisible(WindowInsetsCompat.Type.ime());

            if (navigation != null) {
                navigation.setVisibility(showsBottomNavigation() && !isKeyboardOpen ? View.VISIBLE : View.GONE);
            }

            int bottomInset = isKeyboardOpen ? ime.bottom : sysBars.bottom;
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, bottomInset);
            return WindowInsetsCompat.CONSUMED;
        });

        render();
    }
    @Override public DemoState state() { return store; }
    @Override public void message(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override public void logout() {
        store.logout();
        pendingAvatarAccountId=-1L;
        routeHistory.clear();
        route="login";
        render();
    }
    @Override public void navigate(String next) {
        String previous = route;
        if (DemoState.NONE.equals(store.role)) { route=("register".equals(next)||"login".equals(next)||"reset".equals(next))?next:"login"; render(); return; }
        if (DemoState.CUSTOMER.equals(store.role)) {
            if("home".equals(next) && "onboarding".equals(route)){
                getSharedPreferences("pharmasiu_local_settings",MODE_PRIVATE)
                    .edit().putBoolean("onboarding_complete",true).apply();
            }else if("home".equals(next)
                && !getSharedPreferences("pharmasiu_local_settings",MODE_PRIVATE)
                    .getBoolean("onboarding_complete",false)){
                next="onboarding";
            }
            switch(next) {
                case "home":case "catalog":case "product":case "home-v2":
                case "catalog-v2":case "product-v2":case "cart":case "prescription":
                case "checkout":case "orders":case "order-detail":case "account":
                case "favorites":case "edit-profile":case "profile":case "addresses":case "address-book":
                case "change-password":case "onboarding":case "receipt":
                case "cart-v2":case "rx-v2":case "checkout-v2":case "orders-v2":
                case "order-detail-v2":case "receipt-v2":route=next;break;
                default: route="home";
            }
        } else if(DemoState.CASHIER.equals(store.role)) {
            if ("requests".equals(next) || "requests-admin-v2".equals(next) || "requests-admin".equals(next)) {
                route = "requests-admin-v2";
            } else {
                route = "pos";
            }
        } else if(DemoState.ADMIN.equals(store.role)) {
            switch(next) {
                case "admin":case "catalog-admin":case "lots":case "audit":case "reports":
                case "pos":case "admin-v2":case "catalog-admin-v2":case "lots-v2":
                case "audit-v2":case "requests-admin-v2":case "reports-v2":route=next;break;
                default:route="admin";
            }
        } else route="login";
        if(!previous.equals(route) && !"login".equals(previous)
            && !"register".equals(previous) && !"reset".equals(previous)
            && !"onboarding".equals(previous)) {
            if(routeHistory.size()>=24)routeHistory.removeFirst();
            routeHistory.addLast(previous);
        }
        render(previous, false);
    }
    @Override public void choosePrescriptionFile() {
        if(!DemoState.CUSTOMER.equals(store.role)){message("Không đủ quyền");return;}
        Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("*/*");
        picker.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp","application/pdf"});
        startActivityForResult(picker,CHOOSE_LOCAL_PRESCRIPTION);
    }
    @Override public void chooseStaffProductPhoto(String productId) {
        if(!store.canAdmin()||store.product(productId)==null){
            message("Chỉ Quản trị viên mới có quyền cập nhật ảnh sản phẩm.");
            return;
        }
        pendingStaffPhotoProductId=productId;
        Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("image/*");
        picker.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpeg","image/png","image/webp"});
        startActivityForResult(picker,CHOOSE_STAFF_PHOTO);
    }
    @Override public void chooseCustomerAvatar() {
        long accountId=store.authenticatedAccountId();
        if (!DemoState.CUSTOMER.equals(store.role) || accountId <= 0) return;
        Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("image/*");
        pendingAvatarAccountId=accountId;
        try {
            startActivityForResult(picker,CHOOSE_CUSTOMER_AVATAR);
        } catch (ActivityNotFoundException unavailable) {
            pendingAvatarAccountId=-1L;
            message("Thiết bị chưa có trình chọn ảnh.");
        }
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode==CHOOSE_CUSTOMER_AVATAR) {
            long accountId=pendingAvatarAccountId;
            pendingAvatarAccountId=-1L;
            if (resultCode==RESULT_OK && accountId > 0 && accountId==store.authenticatedAccountId()
                && DemoState.CUSTOMER.equals(store.role) && data!=null && data.getData()!=null) {
                CustomerAvatarStore.saveSelected(this,this,data.getData());
            }
            return;
        }
        if(requestCode==CHOOSE_STAFF_PHOTO) {
            String productId=pendingStaffPhotoProductId;
            pendingStaffPhotoProductId=null;
            if(resultCode==RESULT_OK&&store.canAdmin()&&productId!=null
                &&data!=null&&data.getData()!=null){
                Uri uri=data.getData();
                String mime=getContentResolver().getType(uri);
                if(!"content".equalsIgnoreCase(uri.getScheme())
                    ||!("image/jpeg".equalsIgnoreCase(mime)
                        ||"image/png".equalsIgnoreCase(mime)
                        ||"image/webp".equalsIgnoreCase(mime))){
                    message("Chỉ nhận ảnh JPG, PNG hoặc WebP được chọn tại máy.");
                    return;
                }
                if(store.changePhoto(productId,uri.toString())){
                    message("Đã cập nhật ảnh sản phẩm thành công.");
                    navigate("catalog-admin");
                }
            }
        }

        if(requestCode==CHOOSE_LOCAL_PRESCRIPTION && resultCode==RESULT_OK
            && data!=null && data.getData()!=null && DemoState.CUSTOMER.equals(store.role)) {
            Uri uri=data.getData();
            String mime=getContentResolver().getType(uri);
            boolean accepted="application/pdf".equalsIgnoreCase(mime)
                ||"image/jpeg".equalsIgnoreCase(mime)
                ||"image/png".equalsIgnoreCase(mime)
                ||"image/webp".equalsIgnoreCase(mime);
            if(!"content".equalsIgnoreCase(uri.getScheme())||!accepted){
                message("Tệp không hợp lệ. Chỉ chọn ảnh JPG, PNG, WebP hoặc PDF tại máy.");
                return;
            }
            String name="Tệp toa đã chọn tại máy";
            try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},
                null,null,null)) {
                if(c!=null&&c.moveToFirst())name=c.getString(0);
            }catch(Exception ignored) { /* local picker display metadata is optional */ }
            store.attachLocalPrescription(name);
            message("Chỉ chọn tệp tại máy; chưa gửi đến nhà thuốc");
            navigate("rx-v2");
        }
    }
    private void render() {
        render(null, false);
    }

    private void render(String previous, boolean returning) {
        if(content==null)return;
        View outgoing = content.getChildAt(0);
        if (outgoing != null) outgoing.animate().cancel();
        content.removeAllViews();
        View screen;
        if(DemoState.NONE.equals(store.role)) {
            if(!"register".equals(route)&&!"reset".equals(route))route="login";
            screen=AuthScreen.create(this,this,route);
        } else if(DemoState.CUSTOMER.equals(store.role)) {
            String orderRoute=null;
            switch(route) {
                case "cart": case "cart-v2": orderRoute="cart-v2";break;
                case "prescription": case "rx-v2": orderRoute="rx-v2";break;
                case "checkout": case "checkout-v2": orderRoute="checkout-v2";break;
                case "orders": case "orders-v2": orderRoute="orders-v2";break;
                case "order-detail": case "order-detail-v2": orderRoute="order-detail-v2";break;
                case "receipt": case "receipt-v2": orderRoute="receipt-v2";break;
            }
            if(orderRoute!=null)screen=CustomerOrderViews.create(this,this,orderRoute);
            else if (route.equals("account")||route.equals("favorites")||route.equals("edit-profile")
                ||route.equals("profile")||route.equals("addresses")||route.equals("address-book")
                ||route.equals("change-password")||route.equals("onboarding"))
                screen=CustomerAccountViews.create(this,this,route);
            else if(route.equals("home")||route.equals("catalog")||route.equals("product")
                ||route.equals("home-v2")||route.equals("catalog-v2")||route.equals("product-v2"))
                screen=CustomerCatalogViews.create(this,this,route);
            else { route="home"; screen=CustomerCatalogViews.create(this,this,"home"); }
        } else if(DemoState.CASHIER.equals(store.role)||"pos".equals(route)) {
            if ("requests".equals(route) || "requests-admin-v2".equals(route) || "requests-admin".equals(route)) {
                screen = StaffAdminViews.create(this, this, "requests-admin-v2");
            } else {
                screen = StaffPosViews.create(this, this, "pos-v2");
            }
        } else if(DemoState.ADMIN.equals(store.role)) {
            screen=StaffAdminViews.create(this,this,route);
        } else {
            route="login";screen=AuthScreen.create(this,this,route);
        }
        findViewById(R.id.main_root).setBackgroundResource(
            DemoState.NONE.equals(store.role) ? R.drawable.auth_glass_background : R.color.ui_workspace_canvas);
        content.addView(screen,new FrameLayout.LayoutParams(-1,-1));
        if (previous != null && !route.equals(previous) && !DemoState.NONE.equals(store.role)) {
            int direction = PharmaBottomBar.transitionDirection(content, store.role, previous, route);
            UiMotion.enterPage(screen, direction, returning);
        }
        PharmaBottomBar.bind(this, navigation, store.role, route, target -> {
            if("logout".equals(target)) {
                new android.app.AlertDialog.Builder(this)
                    .setTitle("Đăng xuất tài khoản?")
                    .setMessage("Bạn sẽ quay về màn hình đăng nhập.")
                    .setNegativeButton("Ở lại",null)
                    .setPositiveButton("Đăng xuất",(dialog,which)->logout())
                    .show();
            } else {
                navigate(target);
                // Switching a main tab starts a fresh path instead of stacking every tab visit.
                routeHistory.clear();
            }
        });
        if (!showsBottomNavigation()) navigation.setVisibility(View.GONE);
    }

    private boolean showsBottomNavigation() {
        if (DemoState.NONE.equals(store.role)) return false;
        if (!DemoState.CUSTOMER.equals(store.role)) return true;
        return !"product".equals(route) && !"product-v2".equals(route)
            && !"edit-profile".equals(route) && !"profile".equals(route);
    }
    @Override public void onBackPressed() {
        if(DemoState.NONE.equals(store.role)){
            if(!"login".equals(route)){route="login";render();}
            else super.onBackPressed();
            return;
        }
        String previous = route;
        if(!routeHistory.isEmpty()){
            route=routeHistory.removeLast();
            render(previous, true);
            return;
        }
        if(DemoState.CUSTOMER.equals(store.role) && !"home".equals(route)){
            route="home";render(previous, true);
        }else if(DemoState.ADMIN.equals(store.role) && !"admin".equals(route)){
            route="admin";render(previous, true);
        }else if(DemoState.CASHIER.equals(store.role) && !"pos".equals(route)){
            route="pos";render(previous, true);
        }else{
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
