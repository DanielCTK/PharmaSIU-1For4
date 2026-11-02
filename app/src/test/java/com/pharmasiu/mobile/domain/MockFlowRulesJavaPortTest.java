package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/** Seven direct ports of MockFlowRulesTest; local-order state verified against DemoState. */
public class MockFlowRulesJavaPortTest {
    @Test public void catalogHasMultipleDistinctSectors() {
        List<DemoState.Product> products=new DemoState().products;
        assertTrue(products.size()>=25);
        Set<String> ids=new HashSet<>(),groups=new HashSet<>();
        for(DemoState.Product p:products){assertTrue(ids.add(p.id));groups.add(p.category);}
        assertEquals(products.size(),ids.size());
        for(String group:Arrays.asList("Mỹ phẩm","Thực phẩm","Thực phẩm bổ sung",
            "Thiết bị","Vật tư y tế","Thuốc kê đơn"))
            assertTrue("Missing sector "+group,groups.contains(group));
    }
    @Test public void cosmeticAndFoodAreNotMarkedAsPrescriptionDrugs() {
        for(DemoState.Product p:new DemoState().products)
            if(Arrays.asList("Mỹ phẩm","Thực phẩm","Thực phẩm bổ sung").contains(p.category))
                assertFalse(p.id,p.requiresPrescription);
    }
    @Test public void searchIgnoresAccentsAndCaseAndWhitespace() {
        List<DemoState.Product> items=new DemoState().products;
        List<DemoState.Product> accented=LegacyDomainRules.filterCatalog(items,
            "SỮA RỬA MẶT","Tất cả","Đề xuất");
        List<DemoState.Product> plain=LegacyDomainRules.filterCatalog(items,
            "  sua   rua  mat ","Tất cả","Đề xuất");
        assertFalse(accented.isEmpty());
        assertEquals(accented.size(),plain.size());
        for(int i=0;i<accented.size();i++)assertEquals(accented.get(i).id,plain.get(i).id);
    }
    @Test public void categoryFoodNeverReturnsRxOrCosmetics() {
        List<DemoState.Product> items=LegacyDomainRules.filterCatalog(
            new DemoState().products,"","Thực phẩm","Đề xuất");
        assertFalse(items.isEmpty());
        for(DemoState.Product p:items)
            assertTrue(p.id,Arrays.asList("Thực phẩm","Thực phẩm bổ sung").contains(p.category));
    }
    @Test public void priceSortKeepsUnknownPricesAtEnd() {
        List<DemoState.Product> all=new DemoState().products;
        List<DemoState.Product> asc=LegacyDomainRules.filterCatalog(all,"","Tất cả","Giá thấp");
        List<DemoState.Product> desc=LegacyDomainRules.filterCatalog(all,"","Tất cả","Giá cao");
        assertTrue(asc.get(asc.size()-1).priceVnd<=0L);
        assertTrue(desc.get(desc.size()-1).priceVnd<=0L);
    }
    @Test public void demoOrderIsSuccessfulLocallyWithoutImplyingPaymentOrStockHold() {
        DemoState state=new DemoState();
        assertTrue(state.login("0900000000","demo1234"));
        assertTrue(state.addToCart("mask-01"));
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order request=state.saveRequest("Khách demo","0900000000","Điểm nhận mẫu A");
        assertNotNull(request);
        assertEquals("DEMO-0001",request.reference);
        assertEquals("Đã lưu yêu cầu DEMO",request.status);
        assertFalse(request.status.contains("PAID"));
        assertFalse(request.status.contains("HELD"));
        assertFalse(request.status.contains("RX_APPROVED"));
        assertTrue(request.pricedSubtotalVnd>=0L);
        assertEquals(72000L,request.pricedSubtotalVnd);
    }
    @Test public void cancelledDemoOrderHasSeparateState() {
        DemoState state=new DemoState();
        assertTrue(state.login("0900000000","demo1234"));
        assertTrue(state.addToCart("mask-01"));
        DemoState.Order request=state.saveRequest("Khách demo","0900000000","Điểm nhận mẫu A");
        assertNotNull(request);
        assertEquals("Đã lưu yêu cầu DEMO",request.status);
        assertTrue(state.cancelRequest(request.reference,"Khách thay đổi ý"));
        assertEquals("Đã hủy DEMO",request.status);
    }
}
