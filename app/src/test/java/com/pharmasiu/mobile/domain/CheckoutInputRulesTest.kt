package com.pharmasiu.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutInputRulesTest {
    @Test fun validatesNameAndPhoneWithoutChangingExistingRules() {
        assertTrue(CheckoutInputRules.canSubmit("  Nguyễn An  ", "0900000000", true))
        assertFalse(CheckoutInputRules.canSubmit(" A ", "0900000000", true))
        assertFalse(CheckoutInputRules.canSubmit("Nguyễn An", "12345", true))
        assertFalse(CheckoutInputRules.canSubmit("Nguyễn An", "0900000000", false))
    }

    @Test fun rejectsInternationalAndNonnumericInputAsBefore() {
        assertFalse(CheckoutInputRules.isValidPhone("+84900000000"))
        assertFalse(CheckoutInputRules.isValidPhone("09000abc00"))
        assertFalse(CheckoutInputRules.isValidPhone("090000000000"))
        assertEquals("0900000000", CheckoutInputRules.normalizePhoneInput("09 00-000000"))
    }

    @Test fun trimsNameWithoutChangingItsContent() {
        assertEquals("Nguyễn Văn An", CheckoutInputRules.normalizeName("  Nguyễn Văn An  "))
    }
}
