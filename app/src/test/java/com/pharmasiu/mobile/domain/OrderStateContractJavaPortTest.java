package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.domain.JavaRules;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.pharmasiu.mobile.domain.JavaRules.OrderStatus.*;

/** Canonical-stage and pharmacist-gate assertions retained from OrderStateContractTest. */
public class OrderStateContractJavaPortTest {
    @Test public void everyInternalStateHasExactlyOnePublicStage() {
        assertEquals(11,JavaRules.OrderStatus.values().length);
        for(JavaRules.OrderStatus status:JavaRules.OrderStatus.values())
            assertNotNull("Unmapped "+status,LegacyDomainRules.canonicalStage(status));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.PENDING,
            LegacyDomainRules.canonicalStage(REQUEST_RECEIVED));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.RX_PENDING,
            LegacyDomainRules.canonicalStage(RX_NEEDS_UPDATE));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.HELD,
            LegacyDomainRules.canonicalStage(PREPARING));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.READY,
            LegacyDomainRules.canonicalStage(READY_FOR_PICKUP));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.COMPLETED,
            LegacyDomainRules.canonicalStage(COMPLETED));
        assertEquals(LegacyDomainRules.CanonicalOrderStage.CANCELLED,
            LegacyDomainRules.canonicalStage(EXPIRED));
        assertEquals("Quá hạn giữ hàng",LegacyDomainRules.terminationReasonLabel(EXPIRED));
    }
    @Test public void prescriptionCannotSkipReviewOrStockGateWithoutApproval() {
        assertFalse(LegacyDomainRules.canTransitionForOrder(
            REQUEST_RECEIVED,STOCK_CONFIRMATION_PENDING,true,true));
        assertFalse(LegacyDomainRules.canTransitionForOrder(
            RX_REVIEW_PENDING,STOCK_CONFIRMATION_PENDING,true,false));
        assertTrue(LegacyDomainRules.canTransitionForOrder(
            RX_REVIEW_PENDING,STOCK_CONFIRMATION_PENDING,true,true));
        assertTrue(LegacyDomainRules.canTransitionForOrder(
            REQUEST_RECEIVED,STOCK_CONFIRMATION_PENDING,false,false));
    }
    @Test public void completedAndCancelledAreTerminalAndPaymentCannotSkipStages() {
        for(JavaRules.OrderStatus terminal:new JavaRules.OrderStatus[]{
            COMPLETED,CANCELLED,EXPIRED,RX_REJECTED})
            for(JavaRules.OrderStatus target:JavaRules.OrderStatus.values())
                assertFalse(terminal+" -> "+target,LegacyDomainRules.canTransition(terminal,target));
        assertFalse(LegacyDomainRules.canTransition(REQUEST_RECEIVED,COMPLETED));
        assertFalse(LegacyDomainRules.canTransition(READY_FOR_PICKUP,HELD));
    }
}
