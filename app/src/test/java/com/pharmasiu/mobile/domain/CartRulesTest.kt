package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.repository.DemoCatalogRepository
import com.pharmasiu.mobile.data.model.CartLine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartRulesTest {
    @Test fun rxRequestsRequireAttachment() {
        val rx = requireNotNull(DemoCatalogRepository.byId("rx-demo-01"))
        val lines = listOf(CartLine(rx, 1))
        assertFalse(CartRules.canProceedToRequest(lines, prescriptionAttached = false))
        assertTrue(CartRules.canProceedToRequest(lines, prescriptionAttached = true))
    }

    @Test fun otcDoesNotRequireAttachment() {
        val otc = requireNotNull(DemoCatalogRepository.byId("mask-01"))
        assertTrue(CartRules.canProceedToRequest(listOf(CartLine(otc, 1)), prescriptionAttached = false))
    }

    @Test fun emptyRequestIsBlocked() {
        assertFalse(CartRules.canProceedToRequest(emptyList(), prescriptionAttached = true))
    }
}
