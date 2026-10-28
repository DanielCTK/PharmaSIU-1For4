package com.pharmasiu.mobile.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStateMachineTest {
    @Test fun prescriptionMustBeReviewedBeforeHolding() {
        assertTrue(OrderStateMachine.canTransition(OrderStatus.REQUEST_RECEIVED, OrderStatus.RX_REVIEW_PENDING))
        assertFalse(OrderStateMachine.canTransition(OrderStatus.RX_REVIEW_PENDING, OrderStatus.HELD))
        assertTrue(OrderStateMachine.canTransition(OrderStatus.RX_REVIEW_PENDING, OrderStatus.STOCK_CONFIRMATION_PENDING))
    }

    @Test fun cannotCompleteAnUnconfirmedOrder() {
        assertFalse(OrderStateMachine.canTransition(OrderStatus.REQUEST_RECEIVED, OrderStatus.COMPLETED))
        assertFalse(OrderStateMachine.canTransition(OrderStatus.HELD, OrderStatus.COMPLETED))
        assertTrue(OrderStateMachine.canTransition(OrderStatus.READY_FOR_PICKUP, OrderStatus.COMPLETED))
    }

    @Test fun terminalStatusesCannotBeReopened() {
        listOf(OrderStatus.COMPLETED, OrderStatus.EXPIRED, OrderStatus.CANCELLED, OrderStatus.RX_REJECTED)
            .forEach { terminal ->
                assertFalse(OrderStateMachine.canTransition(terminal, OrderStatus.HELD))
            }
    }
}
