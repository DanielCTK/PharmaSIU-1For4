package com.pharmasiu.mobile.ui.staff;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;
import java.time.LocalDate;

import static org.junit.Assert.*;

/** JVM tests of the screen's RAM-only product creation and permission guards. */
public final class StaffAdminViewsTest {
    private static DemoState admin() {
        DemoState s = new DemoState();
        assertTrue(s.login("quanly@pharmasiu.demo", "2222"));
        return s;
    }

    @Test public void adminCreatesStockOnlyWithFutureDatedLot() {
        DemoState s = admin();
        int products = s.products.size();
        LocalDate expiry = LocalDate.now().plusMonths(6);
        assertTrue(StaffAdminViews.createDemoProduct(s, "Bộ dụng cụ sơ cứu",
            "Vật tư y tế", "Bộ", 56000, 7, "NEW-L1", expiry));
        assertEquals(products + 1, s.products.size());
        DemoState.Product created = s.products.get(products);
        assertFalse(created.requiresPrescription);
        assertEquals(7, (int) s.stock.get(created.id));
        assertEquals(7, s.eligibleLotQty(created.id, LocalDate.now()));
        assertEquals("NEW-L1", s.lots.get(s.lots.size() - 1).code);
    }

    @Test public void cashierCannotCreateOrAlterAdminGoods() {
        DemoState s = new DemoState();
        assertTrue(s.login("nhanvien@pharmasiu.demo", "1111"));
        int before = s.products.size();
        assertFalse(StaffAdminViews.createDemoProduct(s, "Bông băng mới",
            "Vật tư y tế", "Gói", 30000, 0, "", null));
        assertEquals(before, s.products.size());
        assertFalse(s.renameProduct("mask-01", "Không hợp lệ", 15000));
        assertFalse(s.receiveLot("mask-01", "UNAUTHORIZED", 2, LocalDate.now().plusDays(7)));
    }

    @Test public void rejectExpiredOrUndatedOpeningStock() {
        DemoState s = admin();
        int before = s.products.size();
        assertFalse(StaffAdminViews.createDemoProduct(s, "Gạc vô trùng mẫu mới",
            "Vật tư y tế", "Gói", 20000, 5, "", LocalDate.now().plusDays(7)));
        assertFalse(StaffAdminViews.createDemoProduct(s, "Gạc vô trùng mẫu mới",
            "Vật tư y tế", "Gói", 20000, 5, "L-2", LocalDate.now()));
        assertFalse(StaffAdminViews.createDemoProduct(s, "Gạc vô trùng mẫu mới",
            "Vật tư y tế", "Gói", 20000, 5, "L-3", null));
        assertEquals(before, s.products.size());
    }

    @Test public void zeroStockGoodsAreAllowedButNotAnRxOrDuplicateName() {
        DemoState s = admin();
        assertTrue(StaffAdminViews.createDemoProduct(s, "Khẩu trang màu", 
            "Vật tư y tế", "Hộp", 22000, 0, "", null));
        assertFalse(StaffAdminViews.createDemoProduct(s, "KHẨU TRANG MÀU", 
            "Vật tư y tế", "Hộp", 22000, 0, "", null));
        assertFalse(StaffAdminViews.createDemoProduct(s, "Thuốc mẫu", 
            "Thuốc kê đơn", "Hộp", 22000, 0, "", null));
        assertFalse(StaffAdminViews.createDemoProduct(s, "Thực phẩm mẫu", 
            "Thực phẩm", "Hộp", 0, 0, "", null));
        DemoState.Product created = s.products.get(s.products.size() - 1);
        assertEquals(0, (int) s.stock.get(created.id));
    }

    @Test public void quarantineAndCountingRemainLocal() {
        DemoState s = admin();
        LocalDate date = LocalDate.now().plusMonths(2);
        assertTrue(s.receiveLot("mask-01", "Q-DEMO", 4, date));
        assertTrue(StaffAdminViews.duplicateLotCode(s, "q-demo"));
        assertTrue(s.quarantineLot("Q-DEMO"));
        assertFalse(s.quarantineLot("Q-DEMO"));
        assertEquals(0, s.eligibleLotQty("mask-01", LocalDate.now()));
        int recordedStock = s.stock.get("mask-01");
        assertTrue(s.countDemoStock("mask-01", 3, "Kiểm thử lệch"));
        assertEquals(recordedStock, (int) s.stock.get("mask-01"));
        assertEquals(1, s.inventoryChecks.size());
    }

    @Test public void reconciliationDoesNotSettlePayment() {
        DemoState s = admin();
        assertTrue(s.openShift(100000));
        assertFalse("Ca đang mở không thể đối soát",s.reconcileDemoShift(95000, "Đối chiếu mẫu"));
        assertTrue(s.closeShift());
        assertTrue(s.reconcileDemoShift(95000, "Đối chiếu mẫu"));
        assertFalse("Một ca chỉ đối soát một lần",s.reconcileDemoShift(96000, "Trùng biên bản"));
        assertEquals(95000L, s.cashCountedVnd);
        assertEquals(1, s.shiftReconciliations.size());
        assertEquals(100000L, s.openingCashVnd);
        assertFalse(s.canApproveRx());
    }
}
