package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of CheckoutInputRulesTest. */
public class CheckoutInputRulesJavaPortTest {
    @Test public void validatesNameAndPhoneWithoutChangingExistingRules() {
        assertTrue(LegacyDomainRules.canSubmitCheckout("  Nguyễn An  ","0900000000",true));
        assertFalse(LegacyDomainRules.canSubmitCheckout(" A ","0900000000",true));
        assertFalse(LegacyDomainRules.canSubmitCheckout("Nguyễn An","12345",true));
        assertFalse(LegacyDomainRules.canSubmitCheckout("Nguyễn An","0900000000",false));
    }
    @Test public void rejectsInternationalAndNonnumericInputAsBefore() {
        assertFalse(LegacyDomainRules.isValidPhone("+84900000000"));
        assertFalse(LegacyDomainRules.isValidPhone("09000abc00"));
        assertFalse(LegacyDomainRules.isValidPhone("090000000000"));
        assertEquals("0900000000",LegacyDomainRules.normalizePhoneInput("09 00-000000"));
    }
    @Test public void trimsNameWithoutChangingItsContent() {
        assertEquals("Nguyễn Văn An",LegacyDomainRules.normalizeName("  Nguyễn Văn An  "));
    }
}
