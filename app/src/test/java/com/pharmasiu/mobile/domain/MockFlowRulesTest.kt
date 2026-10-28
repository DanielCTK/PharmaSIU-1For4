package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.model.CartLine
import com.pharmasiu.mobile.data.model.MockRequest
import com.pharmasiu.mobile.data.model.MockRequestStatus
import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import org.junit.Assert.*
import org.junit.Test

class MockFlowRulesTest {
    private val products = DemoCatalogRepository.medicines

    @Test fun catalogHasMultipleDistinctSectors() {
        assertTrue(products.size >= 25)
        assertEquals(products.size, products.map { it.id }.distinct().size)
        listOf("Mỹ phẩm", "Thực phẩm", "Thực phẩm bổ sung", "Thiết bị", "Vật tư y tế", "Thuốc kê đơn")
            .forEach { group -> assertTrue("Missing sector $group", products.any { it.category == group }) }
    }

    @Test fun cosmeticAndFoodAreNotMarkedAsPrescriptionDrugs() {
        products.filter { it.category in listOf("Mỹ phẩm", "Thực phẩm", "Thực phẩm bổ sung") }
            .forEach { assertFalse(it.requiresPrescription) }
    }

    @Test fun searchIgnoresAccentsAndCaseAndWhitespace() {
        val a = CatalogSearchRules.filter(products, "SỮA RỬA MẶT", "Tất cả", "Đề xuất")
        val b = CatalogSearchRules.filter(products, "  sua   rua  mat ", "Tất cả", "Đề xuất")
        assertEquals(a.map { it.id }, b.map { it.id })
        assertTrue(a.isNotEmpty())
    }

    @Test fun categoryFoodNeverReturnsRxOrCosmetics() {
        val actual = CatalogSearchRules.filter(products, "", "Thực phẩm", "Đề xuất")
        assertTrue(actual.isNotEmpty())
        assertTrue(actual.all { it.category in listOf("Thực phẩm", "Thực phẩm bổ sung") })
    }

    @Test fun priceSortKeepsUnknownPricesAtEnd() {
        val asc = CatalogSearchRules.filter(products, "", "Tất cả", "Giá thấp")
        val desc = CatalogSearchRules.filter(products, "", "Tất cả", "Giá cao")
        assertTrue(asc.last().priceVnd <= 0L)
        assertTrue(desc.last().priceVnd <= 0L)
    }

    @Test fun demoOrderIsSuccessfulLocallyWithoutImplyingPaymentOrStockHold() {
        val request = MockRequest(
            "DEMO-0001", listOf(CartLine(products.first(), 2)), "Khách demo",
            "0900000000", "Điểm nhận mẫu A", "28/10/2026",
        )
        assertEquals(MockRequestStatus.PLACED_DEMO, request.status)
        assertFalse(request.status.name.contains("PAID"))
        assertFalse(request.status.name.contains("HELD"))
        assertFalse(request.status.name.contains("RX_APPROVED"))
        assertTrue(request.illustrativeTotal >= 0)
    }

    @Test fun cancelledDemoOrderHasSeparateState() {
        val order = MockRequest(
            "DEMO-0002", listOf(CartLine(products.first(), 1)), "Khách demo",
            "0900000000", "Điểm nhận mẫu A", "28/10/2026",
        )
        assertEquals(MockRequestStatus.PLACED_DEMO, order.status)
        assertEquals(MockRequestStatus.CANCELLED_LOCALLY,
            order.copy(status = MockRequestStatus.CANCELLED_LOCALLY).status)
    }
}
