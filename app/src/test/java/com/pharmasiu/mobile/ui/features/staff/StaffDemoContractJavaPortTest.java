package com.pharmasiu.mobile.ui.features.staff;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.domain.JavaRules;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Port of five StaffDemoContractTest cases using real DemoState mutations.
 * Java differs intentionally from Kotlin StaffDemoStore: no createDemoProduct,
 * no unquarantine API, no expiry receipt of an expired lot, reconciliation is
 * a boolean with an audit string instead of the Kotlin report object.
 */
public class StaffDemoContractJavaPortTest {
    private DemoState admin() {
        DemoState state=new DemoState();
        assertTrue(state.login("quanly@pharmasiu.demo","2222"));
        return state;
    }
    private DemoState cashier() {
        DemoState state=new DemoState();
        assertTrue(state.login("nhanvien@pharmasiu.demo","1111"));
        return state;
    }
    @Test public void cashierCannotMutateAdminDataEvenWhenCallingStoreDirectly() {
        DemoState state=cashier();
        long originalPrice=state.product("mask-01").priceVnd;
        int originalStock=state.stock.get("mask-01");
        assertFalse(LegacyDomainRules.mayApprovePrescription(LegacyDomainRules.StaffRole.ADMIN));
        assertFalse(LegacyDomainRules.mayApprovePrescription(LegacyDomainRules.StaffRole.CASHIER));
        assertFalse(state.renameProduct("mask-01","Tên thử",12345L));
        assertFalse(state.changePhoto("mask-01","content://media/sample"));
        assertFalse(state.receiveLot("mask-01","LOT-C",10,LocalDate.now().plusDays(1)));
        assertFalse(state.quarantineLot("LOT-C"));
        assertFalse(state.countDemoStock("mask-01",99,"no permission"));
        assertFalse(state.reconcileDemoShift(0,"no permission"));
        assertEquals(originalPrice,state.product("mask-01").priceVnd);
        assertEquals(originalStock,(int)state.stock.get("mask-01"));
    }
    @Test public void managerUpdatesNameAndPriceButCannotSilentlyRepriceDraft() {
        DemoState state=admin();
        long untouched=new DemoState().product("mask-01").priceVnd;
        assertTrue(state.renameProduct("mask-01","Mặt hàng thử",45678L));
        assertEquals("Mặt hàng thử",state.product("mask-01").name);
        assertEquals(45678L,state.product("mask-01").priceVnd);
        assertTrue(state.openShift(0));
        assertTrue(state.addPos("mask-01"));
        assertFalse(state.renameProduct("mask-01","Lần nữa",88888L));
        assertEquals(45678L,state.posTotal()); // Unchanged amount for an active draft.
        state.changePosQty("mask-01",0);
        assertTrue(state.renameProduct("mask-01","Lần nữa",88888L));
        assertEquals(88888L,state.product("mask-01").priceVnd);
        assertEquals(untouched,new DemoState().product("mask-01").priceVnd);
    }
    @Test public void qualityAndExpiryGatesApplyBeforeDemoSale() {
        DemoState state=admin();
        LocalDate today=LocalDate.now();
        assertTrue(state.openShift(0));
        // Unlike Kotlin, Java receiveLot disallows expired dates. Exercise that guard.
        assertFalse(state.receiveLot("mask-01","EXPIRED",5,today.minusDays(1)));
        // Java lot constructor is used to model imported ineligible data.
        DemoState.Lot expired=new DemoState.Lot("EXPIRED","mask-01",5,today.minusDays(1));
        DemoState.Lot quarantine=new DemoState.Lot("QUARANTINE","mask-01",2,today.plusDays(1));
        quarantine.quarantined=true;
        state.lots.add(expired);
        state.lots.add(quarantine);
        assertEquals(0,state.eligibleLotQty("mask-01",today));
        assertTrue(state.addPos("mask-01")); // Draft may exist; final save must enforce lotted eligibility.
        assertNull(state.savePosSale("CASH",36000L));
        assertEquals(5,expired.quantity);
        assertEquals(2,quarantine.quantity);
        assertTrue(state.receiveLot("mask-01","GOOD",2,today.plusMonths(1)));
        state.changePosQty("mask-01",2);
        assertNotNull(JavaRules.allocateFefo(state.lots,"mask-01",2,today));
        DemoState.Sale receipt=state.savePosSale("CASH",72000L);
        assertNotNull(receipt);
        assertEquals(72000L,receipt.amountVnd);
        assertEquals(5,expired.quantity);
        assertEquals(2,quarantine.quantity);
        assertEquals(0,state.lots.stream().filter(l -> "GOOD".equals(l.code))
            .findFirst().get().quantity);
        assertNull(state.savePosSale("CASH",72000L)); // Empty draft cannot be saved twice.
    }
    @Test public void inventoryCountRecordsDiscrepancyWithoutInventingLotAdjustment() {
        DemoState state=admin();
        int beforeStock=state.stock.get("mask-01");
        int beforeLots=state.lots.size();
        assertTrue(state.countDemoStock("mask-01",beforeStock-2,"Đếm mẫu"));
        assertEquals(1,state.inventoryChecks.size());
        assertTrue(state.inventoryChecks.get(0).contains("lệch -2"));
        assertFalse(state.countDemoStock("mask-01",beforeStock-1,""));
        assertEquals(beforeStock,(int)state.stock.get("mask-01"));
        assertEquals(beforeLots,state.lots.size());
        assertFalse(state.countDemoStock("mask-01",-1,"x"));
    }
    @Test public void reconciliationOccursAfterShiftAndIsOncePerShift() {
        DemoState state=admin();
        assertTrue(state.openShift(0L));
        assertTrue(state.addPos("mask-01"));
        DemoState.Sale first=state.savePosSale("CASH",36000L);
        assertNotNull(first);
        assertTrue(state.addPos("mask-01"));
        DemoState.Sale second=state.savePosSale("OTHER_DEMO",0);
        assertNotNull(second);
        assertEquals("OTHER_DEMO",second.method);
        assertEquals(first.amountVnd,state.cashCollectedVnd); // Other payment not added to cash.
        assertFalse(state.reconcileDemoShift(first.amountVnd,"Trước đóng ca"));
        assertTrue(state.closeShift());
        assertTrue(state.reconcileDemoShift(first.amountVnd-1000,"Kiểm tiền"));
        assertEquals(first.amountVnd-1000,state.cashCountedVnd);
        assertEquals(1,state.shiftReconciliations.size());
        assertTrue(state.shiftReconciliations.get(0).contains("lệch -1000"));
        assertFalse(state.reconcileDemoShift(first.amountVnd,"Đối soát lần nữa"));
        assertTrue(state.openShift(0));
        assertEquals(2,state.shiftNumber);
        assertEquals(0L,state.cashCollectedVnd);
        assertEquals(0L,state.openingCashVnd);
        assertEquals(2,state.sales.size()); // Previous demo receipts remain in shared RAM store.
    }
}
