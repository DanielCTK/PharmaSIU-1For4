package com.pharmasiu.mobile.ui.staff;

import android.app.Activity;
import android.view.View;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import com.pharmasiu.mobile.ui.staff.admin.*;
import java.time.LocalDate;

/**
 * Stable public router for staff feature packages. Only role and route dispatch belong here.
 * Staff screens themselves live under ui.staff.admin.
 */
public final class StaffAdminViews {
    private StaffAdminViews() {}
    public static View create(Activity activity, UiHost host, String route) {
        if (activity == null || host == null || host.state() == null)
            throw new IllegalArgumentException("Missing Java Views host");
        boolean isRequestRoute = "requests-admin".equals(route) || "requests-admin-v2".equals(route);
        if (!host.state().canAdmin() && !(isRequestRoute && DemoState.CASHIER.equals(host.state().role)))
            return AdminUi.access(activity, "Không đủ quyền",
                "Khu quản trị chỉ dành cho tài khoản Quản lý. Phiên hiện tại không có quyền chỉnh dữ liệu.");
        if (route == null)
            return AdminUi.access(activity, "Đường dẫn không hợp lệ",
                "Không tìm thấy màn quản trị yêu cầu.");
        switch (route) {
            case "admin": case "admin-v2": return AdminDashboard.dashboard(activity,host);
            case "catalog-admin": case "catalog-admin-v2": return AdminCatalog.catalog(activity,host);
            case "lots": case "lots-v2": return AdminLots.lots(activity,host);
            case "audit": case "audit-v2": return AdminAudit.audit(activity,host);
            case "reports": case "reports-v2": return AdminReports.reports(activity,host);
            case "requests-admin": case "requests-admin-v2": return AdminRequests.requests(activity,host);
            default: return AdminUi.access(activity, "Không tìm thấy trang",
                "Đường dẫn không thuộc khu quản trị hệ thống.");
        }
    }
    static boolean createDemoProduct(DemoState s,String name,String category,
                                     String unit,long price,int openingQty,String lotCode,LocalDate expiry) {
        return AdminCatalog.createDemoProduct(s,name,category,unit,price,openingQty,lotCode,expiry);
    }
    static boolean duplicateLotCode(DemoState s,String code) {
        return AdminLots.duplicateLotCode(s,code);
    }
    static boolean restoreDemoLot(DemoState s,DemoState.Lot lot){
        return AdminLots.restoreDemoLot(s,lot);
    }
}
