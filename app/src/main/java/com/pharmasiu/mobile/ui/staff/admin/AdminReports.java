package com.pharmasiu.mobile.ui.staff.admin;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.pharmasiu.mobile.R;
import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.core.UiHost;
import java.time.LocalDate;
import static com.pharmasiu.mobile.ui.staff.admin.AdminUi.*;

/** Compact, explicit reporting of the existing local demo records. */
public final class AdminReports {
    private AdminReports() {}

    public static View reports(Activity activity, UiHost host) {
        DemoState state = host.state();
        Screen screen = new Screen(activity, R.layout.staff_admin_reports, host);
        screen.context(state.sales.size() + " phiếu POS · " + state.orders.size() + " yêu cầu");
        long gross = 0;
        int blocked = 0, low = 0, rx = 0;
        LocalDate today = LocalDate.now();
        for (DemoState.Sale sale : state.sales) gross += sale.amountVnd;
        for (DemoState.Lot lot : state.lots)
            if (lot.quarantined || lot.expiresOn == null || !lot.expiresOn.isAfter(today)) blocked++;
        for (DemoState.Product product : state.products) {
            if (product.requiresPrescription) rx++;
            if (state.stock.getOrDefault(product.id, 0) <= 4) low++;
        }

        LinearLayout sales = card(screen);
        title(sales, "Doanh số POS demo");
        line(sales, vnd(gross), 28, R.color.emerald, true);
        line(sales, "Tổng giá trị các phiếu đã lưu trên thiết bị", 14, R.color.muted, false);
        divider(sales);
        dataRow(sales, "Phiếu đã lưu", String.valueOf(state.sales.size()), R.color.forest);
        dataRow(sales, "Tiền mặt ca hiện tại", vnd(state.cashCollectedVnd), R.color.emerald);
        warning(sales, "Số liệu cục bộ; chưa xác minh doanh thu hoặc thanh toán thật.");

        LinearLayout inventory = card(screen);
        title(inventory, "Tình trạng hàng hóa");
        dataRow(inventory, "Mặt hàng", String.valueOf(state.products.size()), R.color.forest);
        dataRow(inventory, "Tồn từ 4 trở xuống", String.valueOf(low), low > 0 ? R.color.warning : R.color.forest);
        divider(inventory);
        dataRow(inventory, "Lô hàng", String.valueOf(state.lots.size()), R.color.forest);
        dataRow(inventory, "Lô hết hạn / cách ly / thiếu hạn", String.valueOf(blocked),
            blocked > 0 ? R.color.warning : R.color.forest);
        dataRow(inventory, "Hàng Rx bị khóa", String.valueOf(rx), R.color.forest);

        LinearLayout records = card(screen);
        title(records, "Hồ sơ trên thiết bị");
        dataRow(records, "Yêu cầu khách hàng", String.valueOf(state.orders.size()), R.color.forest);
        dataRow(records, "Biên bản kiểm kê", String.valueOf(state.inventoryChecks.size()), R.color.forest);
        dataRow(records, "Biên bản đối soát", String.valueOf(state.shiftReconciliations.size()), R.color.forest);
        button(records, "Kiểm kê và đối soát ca", false, () -> host.navigate("audit-v2"));
        button(records, "Xem lô hàng", false, () -> host.navigate("lots-v2"));
        return screen.root;
    }
}
