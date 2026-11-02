package com.pharmasiu.mobile.ui.customer;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/** Pure-model parity checks for the three XML-backed customer catalog screens. */
public class CustomerCatalogViewsTest {
    @Test public void exactlyThirtyOriginalKotlinDemoFixturesWithNoDuplicateId() {
        DemoState state = new DemoState();
        assertEquals(30, state.products.size());
        Set<String> ids = new HashSet<>();
        for (DemoState.Product product : state.products) assertTrue("duplicate id " + product.id, ids.add(product.id));
        assertNotNull(state.product("mask-01"));
        assertNotNull(state.product("beauty-06"));
        assertNotNull(state.product("food-06"));
        assertNotNull(state.product("supplies-02"));
        assertNotNull(state.product("rx-demo-02"));
        assertNull(state.product("gauze-01"));
        assertEquals(0, state.product("rx-demo-01").priceVnd);
    }

    @Test public void accentInsensitiveVietnameseCaseAndSpaces() {
        assertEquals("sua rua mat diu nhe",CustomerCatalogViews.normalize("Sữa rửa mặt dịu nhẹ"));
        assertEquals("kem duong am", CustomerCatalogViews.normalize("  KEM DƯỠNG ẨM "));
        DemoState state = new DemoState();
        List<DemoState.Product> match = CustomerCatalogViews.filterProducts(state.products,
            "sua rua mat", "Tất cả", "Đề xuất");
        assertEquals(1,match.size());
        assertEquals("beauty-01",match.get(0).id);
        assertEquals(1,CustomerCatalogViews.filterProducts(state.products,
            "dien tu", "Thiết bị", "Đề xuất").size());
    }

    @Test public void foodFilterIncludesSupplementsAndRxFilterRequiresRx() {
        DemoState state = new DemoState();
        List<DemoState.Product> foods = CustomerCatalogViews.filterProducts(state.products,
            "", "Thực phẩm", "Đề xuất");
        assertEquals(8,foods.size());
        List<DemoState.Product> prescription = CustomerCatalogViews.filterProducts(state.products,
            "", "Kê đơn", "Đề xuất");
        assertEquals(2,prescription.size());
        for(DemoState.Product product:prescription)assertTrue(product.requiresPrescription);
        assertEquals(0,CustomerCatalogViews.filterProducts(state.products,
            "khong-co-san-pham", "Tất cả", "Đề xuất").size());
    }

    @Test public void stableDefaultOrderingNameAndPriceTreatUnpricedLast() {
        DemoState state = new DemoState();
        List<DemoState.Product> baseline = CustomerCatalogViews.filterProducts(state.products,
            "", "Tất cả", "Đề xuất");
        assertEquals("mask-01",baseline.get(0).id);
        List<DemoState.Product> cheap = CustomerCatalogViews.filterProducts(state.products,
            "", "Tất cả", "Giá thấp");
        assertEquals("saline-01",cheap.get(0).id);
        assertTrue(cheap.get(29).requiresPrescription);
        List<DemoState.Product> costly = CustomerCatalogViews.filterProducts(state.products,
            "", "Tất cả", "Giá cao");
        assertEquals("device-02",costly.get(0).id);
        assertTrue(costly.get(29).requiresPrescription);
        List<DemoState.Product> names = CustomerCatalogViews.filterProducts(state.products,
            "", "Tất cả", "Tên A–Z");
        assertEquals(30,names.size());
        for(int i=1;i<names.size();i++)
            assertTrue(String.CASE_INSENSITIVE_ORDER.compare(names.get(i-1).name,names.get(i).name)<=0);
    }

    @Test public void customerActionsRemainRamAndNeverApproveRx() {
        DemoState state = new DemoState();
        assertTrue(state.login("0900000000","demo1234"));
        int originalCount=state.products.size();
        state.toggleFavorite("beauty-02");
        assertTrue(state.favorites.contains("beauty-02"));
        assertTrue(state.addToCart("beauty-02"));
        assertTrue(state.addToCart("rx-demo-02"));
        assertTrue(state.hasRx(state.cart));
        assertFalse(state.canApproveRx());
        assertEquals(originalCount,state.products.size());
        assertFalse(state.canSaveRequest("Khách thử","0900000000","Điểm nhận mẫu A"));
    }
}
