package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.pharmasiu.mobile.domain.JavaRules.OrderStatus.*;

/** Direct port of OrderStateMachineTest. */
public class OrderStateMachineJavaPortTest {
    @Test public void prescriptionMustBeReviewedBeforeHolding() {
        assertTrue(LegacyDomainRules.canTransition(REQUEST_RECEIVED,RX_REVIEW_PENDING));
        assertFalse(LegacyDomainRules.canTransition(RX_REVIEW_PENDING,HELD));
        assertTrue(LegacyDomainRules.canTransition(RX_REVIEW_PENDING,STOCK_CONFIRMATION_PENDING));
    }
    @Test public void cannotCompleteAnUnconfirmedOrder() {
        assertFalse(LegacyDomainRules.canTransition(REQUEST_RECEIVED,COMPLETED));
        assertFalse(LegacyDomainRules.canTransition(HELD,COMPLETED));
        assertTrue(LegacyDomainRules.canTransition(READY_FOR_PICKUP,COMPLETED));
    }
    @Test public void terminalStatusesCannotBeReopened() {
        for(com.pharmasiu.mobile.domain.JavaRules.OrderStatus terminal:
            new com.pharmasiu.mobile.domain.JavaRules.OrderStatus[]{COMPLETED,EXPIRED,CANCELLED,RX_REJECTED})
            assertFalse(LegacyDomainRules.canTransition(terminal,HELD));
    }
}
