package com.pharmasiu.mobile.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class OrderSnapshotParityTest {
    @Test public void historicalRequestRetainsSavedNamePriceWhenCatalogChanges() {
        DemoState state=new DemoState();
        assertTrue(state.login("0900000000","demo1234"));
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order request=state.saveRequest("Khách hàng mẫu","0900000000","Điểm nhận mẫu A");
        assertNotNull(request);
        assertEquals("Khẩu trang y tế",request.productNamesSnapshot.get("mask-01"));
        assertEquals(Long.valueOf(36000L),request.unitPricesSnapshot.get("mask-01"));
        state.logout();
        assertTrue(state.login("quanly@pharmasiu.demo","2222"));
        assertTrue(state.renameProduct("mask-01","Tên mới trong danh mục DEMO",99000L));
        assertEquals("Khẩu trang y tế",request.productNamesSnapshot.get("mask-01"));
        assertEquals(Long.valueOf(36000L),request.unitPricesSnapshot.get("mask-01"));
        assertEquals(36000L,request.pricedSubtotalVnd);
    }
}
