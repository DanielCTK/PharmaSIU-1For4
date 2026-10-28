package com.pharmasiu.mobile.domain

/**
 * Business state vocabulary; only the server is authorized to persist transitions.
 * The mobile demo displays REQUEST_RECEIVED only; it does not call this state machine
 * to claim Rx approval, stock reservation or readiness.
 */
enum class OrderStatus {
    REQUEST_RECEIVED,
    RX_REVIEW_PENDING,
    RX_NEEDS_UPDATE,
    RX_REJECTED,
    STOCK_CONFIRMATION_PENDING,
    HELD,
    PREPARING,
    READY_FOR_PICKUP,
    COMPLETED,
    EXPIRED,
    CANCELLED,
}

object OrderStateMachine {
    private val permitted = mapOf(
        OrderStatus.REQUEST_RECEIVED to setOf(OrderStatus.RX_REVIEW_PENDING, OrderStatus.STOCK_CONFIRMATION_PENDING, OrderStatus.CANCELLED),
        OrderStatus.RX_REVIEW_PENDING to setOf(OrderStatus.RX_NEEDS_UPDATE, OrderStatus.RX_REJECTED, OrderStatus.STOCK_CONFIRMATION_PENDING, OrderStatus.CANCELLED),
        OrderStatus.RX_NEEDS_UPDATE to setOf(OrderStatus.RX_REVIEW_PENDING, OrderStatus.CANCELLED),
        OrderStatus.STOCK_CONFIRMATION_PENDING to setOf(OrderStatus.HELD, OrderStatus.CANCELLED),
        OrderStatus.HELD to setOf(OrderStatus.PREPARING, OrderStatus.EXPIRED, OrderStatus.CANCELLED),
        OrderStatus.PREPARING to setOf(OrderStatus.READY_FOR_PICKUP, OrderStatus.EXPIRED, OrderStatus.CANCELLED),
        OrderStatus.READY_FOR_PICKUP to setOf(OrderStatus.COMPLETED, OrderStatus.EXPIRED, OrderStatus.CANCELLED),
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean =
        permitted[from]?.contains(to) == true
}
