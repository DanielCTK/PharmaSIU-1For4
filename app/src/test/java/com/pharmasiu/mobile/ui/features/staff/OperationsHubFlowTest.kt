package com.pharmasiu.mobile.ui.features.staff

import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.domain.CounterPayment
import com.pharmasiu.mobile.domain.StaffRole
import com.pharmasiu.mobile.domain.StaffDemoAccess
import org.junit.Assert.*
import org.junit.Test

/**
 * Shared employee/admin state regression tests. No Android device, backend or SQL.
 */
class OperationsHubFlowTest {
    private val products = DemoCatalogRepository.medicines
    private val otc = products.first { !it.requiresPrescription && it.priceVnd > 0 }
    private val rx = products.first { it.requiresPrescription }

    @Test fun cashierAndAdminShareOneCounterSaleStore() {
        val store = StaffDemoStore(products)
        store.openShift()
        val stockBefore = store.stock.getValue(otc.id)
        assertTrue(store.add(otc))
        val sale = store.complete(CounterPayment.CASH, otc.priceVnd + 10000)
        assertNotNull(sale)
        assertEquals(1, store.sales.size)
        assertEquals(sale!!.totalVnd, store.sales.sumOf { it.totalVnd })
        assertEquals(stockBefore - 1, store.stock.getValue(otc.id))
        assertEquals(10000L, sale.changeVnd)
        assertTrue(store.cart.isEmpty())
        // The same store instance is passed to the admin overview.
        assertEquals(sale.reference, store.sales.single().reference)
    }

    @Test fun cannotCloseShiftWithUnfinishedCart() {
        val store = StaffDemoStore(products)
        assertFalse(store.add(otc))
        store.openShift()
        assertTrue(store.add(otc))
        assertFalse(store.closeShift())
        assertTrue(store.shiftOpen)
        store.clearDraft()
        assertTrue(store.closeShift())
        assertFalse(store.shiftOpen)
    }

    @Test fun adminDemoStockAdjustmentsCannotMakeDraftInvalid() {
        val store = StaffDemoStore(products)
        store.openShift()
        assertTrue(store.add(otc))
        val before = store.stock.getValue(otc.id)
        repeat(before - 1) { store.adjustDemoStock(otc.id, -1) }
        assertEquals(1, store.stock.getValue(otc.id))
        store.adjustDemoStock(otc.id, -1)
        assertEquals(1, store.stock.getValue(otc.id))
        store.adjustDemoStock(otc.id, 1)
        assertEquals(2, store.stock.getValue(otc.id))
    }

    @Test fun prescriptionIsNotSoldOrEditedInCounterMock() {
        val store = StaffDemoStore(products)
        store.openShift()
        assertFalse(store.add(rx))
        assertEquals(0, store.stock.getValue(rx.id))
        store.adjustDemoStock(rx.id, 5)
        assertEquals(0, store.stock.getValue(rx.id))
    }

    @Test fun rolePinsRemainSeparateEvenThoughUiShellIsShared() {
        assertTrue(StaffDemoAccess.accepts(StaffRole.CASHIER, "1111"))
        assertTrue(StaffDemoAccess.accepts(StaffRole.ADMIN, "2222"))
        assertFalse(StaffDemoAccess.accepts(StaffRole.ADMIN, "1111"))
        assertFalse(StaffDemoAccess.accepts(StaffRole.CASHIER, "2222"))
    }
}
