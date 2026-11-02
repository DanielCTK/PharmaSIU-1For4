package com.pharmasiu.mobile.core;

import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class OriginalCounterParityContractTest {
    private static DemoState manager(){
        DemoState s=new DemoState();
        assertTrue(s.login("quanly@pharmasiu.demo","2222"));
        return s;
    }
    @Test public void completedSaleRetainsAllHistoricalData(){
        DemoState s=manager();
        assertTrue(s.openShift(100_000L));
        assertTrue(s.addPos("mask-01"));
        DemoState.Sale sale=s.savePosSale("CASH",200_000L);
        assertNotNull(sale);
        assertEquals(1,sale.shiftNumber);
        assertEquals("Quản lý demo",sale.cashierName);
        assertFalse(sale.createdAt.isEmpty());
        assertEquals(200_000L,sale.receivedVnd);
        assertEquals(164_000L,sale.changeVnd);
        assertEquals(1,sale.immutableDetail.lines.size());
        assertEquals("Khẩu trang y tế",sale.immutableDetail.lines.get(0).name);
        assertEquals(36_000L,sale.immutableDetail.lines.get(0).unitPriceVnd);
        assertTrue(s.closeShift());
        assertTrue(s.renameProduct("mask-01","Khẩu trang chỉnh tên",60_000L));
        assertEquals("Khẩu trang y tế",sale.immutableDetail.lines.get(0).name);
        assertEquals(36_000L,sale.immutableDetail.lines.get(0).unitPriceVnd);
    }
    @Test public void quarantinedExpiredLotNeverBecomesSellable(){
        DemoState s=manager();
        int previous=s.stock.get("mask-01");
        LocalDate expired=LocalDate.now().minusDays(2);
        assertFalse(s.receiveLot("mask-01","EXP-01",10,expired));
        assertTrue(s.receiveQuarantinedLot("mask-01","EXP-01",10,expired));
        assertEquals(previous,(int)s.stock.get("mask-01"));
        assertTrue(s.setLotQuarantined("EXP-01",false));
        assertEquals(0,s.eligibleLotQty("mask-01",LocalDate.now()));
        assertEquals(previous,(int)s.stock.get("mask-01"));
    }
    @Test public void unquarantiningValidLotRestoresOnlyDemoEligibleStock(){
        DemoState s=manager();
        int before=s.stock.get("mask-01");
        assertTrue(s.receiveLot("mask-01","AVAILABLE-01",3,LocalDate.now().plusDays(30)));
        assertTrue(s.quarantineLot("AVAILABLE-01"));
        assertEquals(before,(int)s.stock.get("mask-01"));
        assertTrue(s.setLotQuarantined("AVAILABLE-01",false));
        assertEquals(before+3,(int)s.stock.get("mask-01"));
        assertFalse(s.setLotQuarantined("AVAILABLE-01",false));
    }
    @Test public void inventoryAndClosedShiftProvideTypedRamAudit(){
        DemoState s=manager();
        assertTrue(s.countDemoStock("mask-01",5,"Kiểm tra demo"));
        assertEquals(1,s.inventoryCountDetails.size());
        assertEquals(5,s.inventoryCountDetails.get(0).countedUnits);
        assertTrue(s.openShift(5_000L));
        assertTrue(s.addPos("mask-01"));
        assertNotNull(s.savePosSale("OTHER_DEMO",0L));
        assertFalse(s.reconcileDemoShift(5_000L,"Chưa đóng ca"));
        assertTrue(s.closeShift());
        assertTrue(s.reconcileDemoShift(5_000L,"Đối chiếu mẫu"));
        assertEquals(1,s.shiftReconciliationDetails.size());
        DemoState.DemoShiftReconciliation info=s.shiftReconciliationDetails.get(0);
        assertEquals(1,info.invoiceCount);
        assertEquals(36_000L,info.otherDemoVnd);
        assertEquals(0L,info.expectedCashVnd);
        assertEquals(0L,info.differenceVnd);
    }
}
