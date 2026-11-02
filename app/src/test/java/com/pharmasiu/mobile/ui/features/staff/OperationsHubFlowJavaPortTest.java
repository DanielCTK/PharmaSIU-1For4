package com.pharmasiu.mobile.ui.features.staff;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of OperationsHubFlowTest against the live Java shared-RAM store. */
public class OperationsHubFlowJavaPortTest {
    private DemoState cashier() {
        DemoState store=new DemoState();
        assertTrue(store.login("nhanvien@pharmasiu.demo","1111"));
        return store;
    }
    @Test public void cashierAndAdminShareOneCounterSaleStore() {
        DemoState store=cashier();
        assertTrue(store.openShift(0));
        int stockBefore=store.stock.get("mask-01");
        assertTrue(store.addPos("mask-01"));
        DemoState.Sale sale=store.savePosSale("CASH",46000L);
        assertNotNull(sale);
        assertEquals(1,store.sales.size());
        assertEquals(sale.amountVnd,store.sales.get(0).amountVnd);
        assertEquals(stockBefore-1,(int)store.stock.get("mask-01"));
        assertEquals(10000L,LegacyDomainRules.changeDue(sale.amountVnd,46000L));
        assertTrue(store.posCart.isEmpty());
        store.logout();
        assertTrue(store.login("quanly@pharmasiu.demo","2222"));
        assertEquals(sale.reference,store.sales.get(0).reference);
    }
    @Test public void cannotCloseShiftWithUnfinishedCart() {
        DemoState store=cashier();
        assertFalse(store.addPos("mask-01"));
        assertTrue(store.openShift(0));
        assertTrue(store.addPos("mask-01"));
        assertFalse(store.closeShift());
        assertTrue(store.shiftOpen);
        store.changePosQty("mask-01",0);
        assertTrue(store.closeShift());
        assertFalse(store.shiftOpen);
    }
    @Test public void adminDemoStockAdjustmentsCannotMakeDraftInvalid() {
        DemoState store=new DemoState();
        assertTrue(store.login("quanly@pharmasiu.demo","2222"));
        assertTrue(store.receiveLot("mask-01","LOT-1",5,LocalDate.now().plusDays(15)));
        assertEquals(15,(int)store.stock.get("mask-01"));
        assertTrue(store.openShift(0));
        store.changePosQty("mask-01",11);
        assertEquals(11,(int)store.posCart.get("mask-01"));
        assertFalse(store.quarantineLot("LOT-1")); // Remaining aggregate 10 < selected 11.
        assertEquals(15,(int)store.stock.get("mask-01"));
        store.changePosQty("mask-01",0); // Draft must be cleared before removing its only eligible lot.
        assertTrue(store.quarantineLot("LOT-1"));
        assertEquals(10,(int)store.stock.get("mask-01"));
    }
    @Test public void prescriptionIsNotSoldOrEditedInCounterMock() {
        DemoState store=new DemoState();
        assertTrue(store.login("quanly@pharmasiu.demo","2222"));
        assertTrue(store.openShift(0));
        assertFalse(store.addPos("rx-demo-01"));
        assertFalse(store.renameProduct("rx-demo-01","Giá mẫu",12000));
        assertFalse(store.receiveLot("rx-demo-01","LOT-RX",1,LocalDate.now().plusMonths(1)));
        assertTrue(store.product("rx-demo-01").requiresPrescription);
    }
    @Test public void rolePinsRemainSeparateEvenThoughUiShellIsShared() {
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.CASHIER,"1111"));
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN,"2222"));
        assertFalse(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN,"1111"));
        assertFalse(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.CASHIER,"2222"));
    }
}
