package com.pharmasiu.mobile.ui.features.staff;

import com.pharmasiu.mobile.core.DemoState;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Contract port of StaffCatalogEditorTest. The Java DemoState intentionally exposes
 * no createDemoProduct API: existing product edit + POS is tested instead. The unported
 * create/duplicate-name rules are identified explicitly in the final handoff.
 */
public class StaffCatalogEditorJavaPortTest {
    private DemoState admin() {
        DemoState store=new DemoState();
        assertTrue(store.login("quanly@pharmasiu.demo","2222"));
        return store;
    }
    @Test public void createGoodsWithOpeningQuantityAndSellAtCounter() {
        DemoState store=admin();
        int fixtureCount=store.products.size();
        assertTrue(store.renameProduct("mask-01","Bánh mẫu tại quầy",24000L));
        assertTrue(store.receiveLot("mask-01","LOT-STOCK",8,LocalDate.now().plusMonths(5)));
        assertEquals(fixtureCount,store.products.size()); // Unlike Kotlin: edit existing; no create API.
        assertEquals(18,(int)store.stock.get("mask-01"));
        assertTrue(store.openShift(0));
        assertTrue(store.addPos("mask-01"));
        DemoState.Sale receipt=store.savePosSale("CASH",30000L);
        assertNotNull(receipt);
        assertEquals(24000L,receipt.amountVnd);
        assertEquals(6000L,Math.max(0,30000L-receipt.amountVnd));
        assertEquals(17,(int)store.stock.get("mask-01"));
        assertEquals("Bánh mẫu tại quầy",store.product("mask-01").name);
        assertEquals(36000L,new DemoState().product("mask-01").priceVnd); // catalog isolation
    }
    @Test public void invalidOrDuplicateCatalogItemsAreRejected() {
        DemoState store=admin();
        int count=store.products.size();
        assertFalse(store.renameProduct("mask-01","",12000L));
        assertFalse(store.renameProduct("rx-demo-01","Thuốc mẫu",12000L));
        assertFalse(store.renameProduct("mask-01","Mỹ phẩm DEMO",0L));
        assertFalse(store.renameProduct("does-not-exist","Mỹ phẩm DEMO",12000L));
        assertTrue(store.renameProduct("mask-01","Mỹ phẩm DEMO",12000L));
        assertEquals(count,store.products.size());
        // Java has no createDemoProduct and thus no duplicate-name creation pathway to assert.
    }
    @Test public void receivingStockIsBoundedAndRefusesRx() {
        DemoState store=admin();
        int old=store.stock.get("mask-01");
        LocalDate expires=LocalDate.now().plusMonths(5);
        assertTrue(store.receiveLot("mask-01","LOT-DEMO-01",5,expires));
        assertEquals(old+5,(int)store.stock.get("mask-01"));
        assertFalse(store.receiveLot("mask-01","LOT-2",0,expires));
        assertFalse(store.receiveLot("mask-01","LOT-3",1000,expires));
        assertFalse(store.receiveLot("rx-demo-01","LOT-RX",1,expires));
        assertEquals(10,(int)store.stock.get("rx-demo-01")); // Java seed count; Rx cannot be sold.
        assertFalse(store.receiveLot("no-such-id","LOT-404",1,expires));
        assertFalse(store.receiveLot("mask-01","LOT-DEMO-01",1,expires)); // Duplicate lot code rejected.
    }
    @Test public void photosAreLimitedToProductsAndContentUrisAndSessionState() {
        DemoState store=admin();
        assertFalse(store.changePhoto("does-not-exist","content://a/b"));
        assertFalse(store.changePhoto("mask-01","file:///storage/emulated/a.jpg"));
        assertTrue(store.changePhoto("mask-01","content://media/picked/3"));
        assertEquals("content://media/picked/3",store.staffPhotoUris.get("mask-01"));
        assertTrue(store.changePhoto("mask-01",null));
        assertNull(store.staffPhotoUris.get("mask-01"));
        assertTrue(new DemoState().staffPhotoUris.isEmpty());
    }
}
