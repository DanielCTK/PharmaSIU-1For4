package com.pharmasiu.mobile.ui.staff;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.domain.JavaRules;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/** Store/domain contract checks for the XML POS screen; device rendering needs emulator QA. */
public class StaffPosViewsParityTest {
    @Test public void unauthenticatedOrCustomerCannotOpenCounter() {
        DemoState state = new DemoState();
        assertFalse(state.openShift(0));
        assertFalse(state.addPos("mask-01"));
        assertNull(state.savePosSale("CASH", 100000));
        assertTrue(state.login("0900000000", "demo1234"));
        assertFalse(state.canSell());
        assertFalse(state.openShift(0));
    }

    @Test public void cashierOpensShiftAdjustsCartAndStoresExactlyOneDemoSale() {
        DemoState state = new DemoState();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertTrue(state.openShift(50000));
        assertTrue(state.addPos("mask-01"));
        state.changePosQty("mask-01", 2);
        assertEquals(2, (int) state.posCart.get("mask-01"));
        assertFalse(state.closeShift());
        assertNull(state.savePosSale("CASH", 71999));
        assertEquals(10, (int) state.stock.get("mask-01"));
        DemoState.Sale sale = state.savePosSale("CASH", 100000);
        assertNotNull(sale);
        assertTrue(sale.reference.startsWith("POS-DEMO-"));
        assertEquals(72000L, sale.amountVnd);
        assertEquals(2, (int) sale.lines.get("mask-01"));
        assertEquals(8, (int) state.stock.get("mask-01"));
        assertEquals(72000L, state.cashCollectedVnd);
        assertTrue(state.posCart.isEmpty());
        assertNull(state.savePosSale("CASH", 100000));
        assertEquals(1, state.sales.size());
        assertTrue(state.closeShift());
    }

    @Test public void noRxOrExpiredOrQuarantinedLotCanBeSold() {
        DemoState state = new DemoState();
        assertTrue(state.login("quanly@pharmasiu.demo", "2222"));
        assertTrue(state.openShift(0));
        assertFalse(state.addPos("rx-demo-01"));
        assertFalse(state.canApproveRx());
        DemoState.Lot expired = new DemoState.Lot("EXP", "mask-01", 5,
            LocalDate.now().minusDays(1));
        DemoState.Lot quarantine = new DemoState.Lot("Q", "mask-01", 5,
            LocalDate.now().plusMonths(1));
        quarantine.quarantined = true;
        state.lots.add(expired);
        state.lots.add(quarantine);
        assertEquals(0, state.eligibleLotQty("mask-01", LocalDate.now()));
        assertNull(JavaRules.allocateFefo(state.lots, "mask-01", 1, LocalDate.now()));
        // The stable DemoState may allow draft entry from aggregate stock,
        // but must block final saving when dated lots are all ineligible.
        assertTrue(state.addPos("mask-01"));
        assertNull(state.savePosSale("CASH", 36000));
        assertEquals(10, (int) state.stock.get("mask-01"));
        assertTrue(state.sales.isEmpty());
    }

    @Test public void otherDemoPaymentIsStillOnlyRecordedAsSampleMethod() {
        DemoState state = new DemoState();
        assertTrue(state.login("nhanvien@pharmasiu.demo", "1111"));
        assertTrue(state.openShift(0));
        assertTrue(state.addPos("saline-01"));
        DemoState.Sale sale = state.savePosSale("OTHER_DEMO", 0);
        assertNotNull(sale);
        assertEquals("OTHER_DEMO", sale.method);
        assertEquals(18000L, sale.amountVnd);
        assertEquals(0, state.cashCollectedVnd);
        assertEquals(1, state.sales.size());
    }
}
