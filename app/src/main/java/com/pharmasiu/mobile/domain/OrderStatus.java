package com.pharmasiu.mobile.domain;

/** SRS vocabulary for eventual server workflow. Device demo does NOT advance these states. */
public enum OrderStatus {
    REQUEST_RECEIVED, RX_REVIEW_PENDING, RX_NEEDS_UPDATE, RX_REJECTED,
    STOCK_CONFIRMATION_PENDING, HELD, PREPARING, READY_FOR_PICKUP, COMPLETED, EXPIRED, CANCELLED;
    public CanonicalOrderStage toCanonicalStage(){return OrderStateMachine.toCanonicalStage(this);}
    public String terminationReasonLabel(){return OrderStateMachine.terminationReasonLabel(this);}
}
