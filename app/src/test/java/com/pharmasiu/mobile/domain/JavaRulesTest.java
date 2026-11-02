package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class JavaRulesTest {
    @Test public void prescriptionReviewCannotBeSkipped() {
        assertFalse(JavaRules.canTransitionForOrder(
            JavaRules.OrderStatus.REQUEST_RECEIVED,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING,true,true));
        assertFalse(JavaRules.canTransitionForOrder(
            JavaRules.OrderStatus.RX_REVIEW_PENDING,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING,true,false));
        assertTrue(JavaRules.canTransitionForOrder(
            JavaRules.OrderStatus.RX_REVIEW_PENDING,
            JavaRules.OrderStatus.STOCK_CONFIRMATION_PENDING,true,true));
    }
    @Test public void expiryAndQuarantineAreNeverAllocated() {
        LocalDate today=LocalDate.of(2026,10,29);
        DemoState.Lot expired=new DemoState.Lot("OLD","a",10,today.minusDays(1));
        DemoState.Lot valid=new DemoState.Lot("OK","a",5,today.plusDays(20));
        DemoState.Lot held=new DemoState.Lot("BLOCK","a",30,today.plusDays(2));
        held.quarantined=true;
        assertFalse(JavaRules.eligibleLot(expired,today));
        assertFalse(JavaRules.eligibleLot(held,today));
        List<JavaRules.Allocation> allocation=JavaRules.allocateFefo(
            Arrays.asList(expired,valid,held),"a",4,today);
        assertNotNull(allocation);assertEquals(1,allocation.size());
        assertEquals("OK",allocation.get(0).code);
        assertNull(JavaRules.allocateFefo(Arrays.asList(expired,valid,held),"a",6,today));
    }
    @Test public void posAndRolesAlwaysBlockRx() {
        DemoState demo=new DemoState();
        assertFalse(JavaRules.canSellDemo(demo.product("rx-demo-01"),1,10,true));
        assertFalse(JavaRules.canSellDemo(demo.product("mask-01"),1,10,false));
        assertTrue(JavaRules.canSellDemo(demo.product("mask-01"),1,10,true));
        assertFalse(JavaRules.canEditCatalog(DemoState.CASHIER));
        assertTrue(JavaRules.canEditCatalog(DemoState.ADMIN));
        assertFalse(JavaRules.canApproveRx(DemoState.ADMIN));
    }
    @Test public void demoCheckoutNeedsPrescribedFileWhenRxInCart() {
        DemoState demo=new DemoState();
        assertTrue(demo.login("0900000000","demo1234"));
        assertTrue(demo.addToCart("rx-demo-01"));
        assertFalse(demo.canSaveRequest("Nguyen Van A","0900000000","Điểm nhận mẫu A"));
        demo.attachLocalPrescription("toa.pdf");
        assertTrue(demo.canSaveRequest("Nguyen Van A","0900000000","Điểm nhận mẫu A"));
        demo.changeCartQty("rx-demo-01",2);
        assertFalse(demo.canSaveRequest("Nguyen Van A","0900000000","Điểm nhận mẫu A"));
    }
    @Test public void saleIsOnlySimulatedAndRolesAreEnforced() {
        DemoState demo=new DemoState();
        assertFalse(demo.openShift(0));
        assertTrue(demo.login("nhanvien@pharmasiu.demo","1111"));
        assertFalse(demo.renameProduct("mask-01","Khau trang",1000));
        assertTrue(demo.openShift(10000));
        assertTrue(demo.addPos("mask-01"));
        assertNull(demo.savePosSale("CASH",1000));
        assertNotNull(demo.savePosSale("CASH",36000));
        assertEquals(36000,demo.cashCollectedVnd);
        assertTrue(demo.closeShift());
    }

    @Test public void posDemoMustUseEligibleLotsWhenBatchRecordsExist() {
        DemoState demo=new DemoState();
        assertTrue(demo.login("quanly@pharmasiu.demo","2222"));
        assertTrue(demo.receiveLot("mask-01","LOT-LIMIT",1,LocalDate.now().plusDays(4)));
        assertTrue(demo.openShift(0));
        assertTrue(demo.addPos("mask-01"));
        assertTrue(demo.addPos("mask-01"));
        assertNull("Cannot sell two units when only one eligible dated lot unit exists",
            demo.savePosSale("CASH",72000));
        demo.changePosQty("mask-01",1);
        assertNotNull(demo.savePosSale("CASH",36000));
        assertEquals(0,demo.lots.get(0).quantity);
        assertTrue(demo.addPos("mask-01"));
        assertNull("Cannot reuse exhausted lot even if legacy seed stock remains",
            demo.savePosSale("CASH",36000));
    }
}
