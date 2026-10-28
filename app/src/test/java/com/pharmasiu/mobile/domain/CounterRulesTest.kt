package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import org.junit.Assert.*
import org.junit.Test

class CounterRulesTest {
    private val nonRx = DemoCatalogRepository.medicines.first {
        !it.requiresPrescription && it.priceVnd > 0
    }
    private val rx = DemoCatalogRepository.medicines.first { it.requiresPrescription }

    private fun line(id: String = nonRx.id, qty: Int = 2) =
        CounterLine(id, nonRx.name, nonRx.unit, nonRx.priceVnd, qty)

    @Test fun cannotSellPrescriptionInCashierMock() {
        assertFalse(CounterRules.sellable(rx))
        assertFalse(CounterRules.availableToAdd(rx, 0, 20))
        assertFalse(CounterRules.canConfirm(
            listOf(line().copy(requiresPrescription = true)), mapOf(nonRx.id to 20),
            shiftOpen = true, method = CounterPayment.CASH, amountTendered = 1000000,
        ))
    }

    @Test fun enforcesStockAndQuantity() {
        assertTrue(CounterRules.availableToAdd(nonRx, 1, 2))
        assertFalse(CounterRules.availableToAdd(nonRx, 2, 2))
        assertFalse(CounterRules.canConfirm(
            listOf(line(qty = 3)), mapOf(nonRx.id to 2), true, CounterPayment.OTHER_DEMO, 0,
        ))
        assertFalse(CounterRules.canConfirm(
            listOf(line(qty = 31)), mapOf(nonRx.id to 100), true, CounterPayment.OTHER_DEMO, 0,
        ))
    }

    @Test fun paymentRequiresOpenShiftAndSufficientCash() {
        val lines = listOf(line())
        val total = CounterRules.total(lines)
        val stock = mapOf(nonRx.id to 5)
        assertFalse(CounterRules.canConfirm(lines, stock, false, CounterPayment.CASH, total))
        assertFalse(CounterRules.canConfirm(lines, stock, true, CounterPayment.CASH, total - 1))
        assertTrue(CounterRules.canConfirm(lines, stock, true, CounterPayment.CASH, total))
        assertTrue(CounterRules.canConfirm(lines, stock, true, CounterPayment.OTHER_DEMO, 0))
        assertEquals(5000L, CounterRules.changeDue(total, total + 5000))
    }

    @Test fun saleIsImmutableSnapshot() {
        val sale = CounterSale("POS-DEMO-0001", "Thu ngân demo", "28/10/2026",
            listOf(line()), CounterPayment.CASH, nonRx.priceVnd * 2 + 10000)
        assertEquals(nonRx.priceVnd * 2, sale.totalVnd)
        assertEquals(10000L, sale.changeVnd)
    }

    @Test fun demoRolesNotInterchangeable() {
        assertTrue(StaffDemoAccess.accepts(StaffRole.CASHIER, "1111"))
        assertTrue(StaffDemoAccess.accepts(StaffRole.ADMIN, "2222"))
        assertFalse(StaffDemoAccess.accepts(StaffRole.ADMIN, "1111"))
        assertFalse(StaffDemoAccess.accepts(StaffRole.CASHIER, "2222"))
    }
}
