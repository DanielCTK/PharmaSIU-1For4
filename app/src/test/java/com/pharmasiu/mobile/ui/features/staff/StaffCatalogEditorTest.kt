package com.pharmasiu.mobile.ui.features.staff

import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.domain.CounterPayment
import org.junit.Assert.*
import org.junit.Test

class StaffCatalogEditorTest {
    @Test fun createGoodsWithOpeningQuantityAndSellAtCounter() {
        val original = DemoCatalogRepository.medicines
        val store = StaffDemoStore(original)
        val newItem = store.createDemoProduct(
            name = "  Bánh mẫu tại quầy  ", category = "Thực phẩm",
            unit = "Hộp", priceVnd = 24000L, initialStock = 8,
        )
        assertNotNull(newItem)
        assertEquals(original.size + 1, store.products.size)
        assertEquals(original.size, DemoCatalogRepository.medicines.size)
        assertEquals(8, store.stock[newItem!!.id])
        store.openShift()
        assertTrue(store.add(newItem))
        val receipt = store.complete(CounterPayment.CASH, 30000L)
        assertNotNull(receipt)
        assertEquals(24000L, receipt!!.totalVnd)
        assertEquals(6000L, receipt.changeVnd)
        assertEquals(7, store.stock[newItem.id])
    }

    @Test fun invalidOrDuplicateCatalogItemsAreRejected() {
        val store = StaffDemoStore(DemoCatalogRepository.medicines)
        val before = store.products.size
        assertNull(store.createDemoProduct("", "Mỹ phẩm", "Chai", 12000, 5))
        assertNull(store.createDemoProduct("Mỹ phẩm demo", "Thuốc kê đơn", "Hộp", 12000, 5))
        assertNull(store.createDemoProduct("Mỹ phẩm demo", "Mỹ phẩm", "Chai", 0, 5))
        assertNull(store.createDemoProduct("Mỹ phẩm demo", "Mỹ phẩm", "Chai", 12000, -1))
        assertNull(store.createDemoProduct("Mỹ phẩm demo", "Mỹ phẩm", "Chai", 12000, 1000))
        assertNotNull(store.createDemoProduct("Mỹ phẩm demo", "Mỹ phẩm", "Chai", 12000, 5))
        assertNull(store.createDemoProduct("mỹ phẩm DEMO", "Mỹ phẩm", "Chai", 12000, 5))
        assertEquals(before + 1, store.products.size)
    }

    @Test fun receivingStockIsBoundedAndRefusesRx() {
        val store = StaffDemoStore(DemoCatalogRepository.medicines)
        val otc = store.products.first { !it.requiresPrescription }
        val rx = store.products.first { it.requiresPrescription }
        val old = store.stock.getValue(otc.id)
        assertTrue(store.receiveDemoStock(otc.id, 5))
        assertEquals(old + 5, store.stock.getValue(otc.id))
        assertFalse(store.receiveDemoStock(otc.id, 0))
        assertFalse(store.receiveDemoStock(otc.id, 999))
        assertFalse(store.receiveDemoStock(rx.id, 1))
        assertEquals(0, store.stock[rx.id])
        assertFalse(store.receiveDemoStock("no-such-id", 1))
    }

    @Test fun photosAreLimitedToProductsAndContentUrisAndSessionState() {
        val store = StaffDemoStore(DemoCatalogRepository.medicines)
        val otc = store.products.first()
        assertFalse(store.changePhoto("does-not-exist", "content://a/b"))
        assertFalse(store.changePhoto(otc.id, "file:///storage/emulated/a.jpg"))
        assertTrue(store.changePhoto(otc.id, "content://media/picked/3"))
        assertEquals("content://media/picked/3", store.photoUris[otc.id])
        assertTrue(store.changePhoto(otc.id, null))
        assertNull(store.photoUris[otc.id])
        assertTrue(StaffDemoStore(DemoCatalogRepository.medicines).photoUris.isEmpty())
    }
}
