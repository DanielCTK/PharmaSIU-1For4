package com.pharmasiu.mobile.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** Pure validator only, never persists a stock hold, Rx approval or payment status. */
public final class OrderStateMachine {
    private OrderStateMachine(){}
    private static final Map<OrderStatus,EnumSet<OrderStatus>> ALLOWED=new EnumMap<>(OrderStatus.class);
    static{
        ALLOWED.put(OrderStatus.REQUEST_RECEIVED,EnumSet.of(OrderStatus.RX_REVIEW_PENDING,OrderStatus.STOCK_CONFIRMATION_PENDING,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.RX_REVIEW_PENDING,EnumSet.of(OrderStatus.RX_NEEDS_UPDATE,OrderStatus.RX_REJECTED,OrderStatus.STOCK_CONFIRMATION_PENDING,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.RX_NEEDS_UPDATE,EnumSet.of(OrderStatus.RX_REVIEW_PENDING,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.STOCK_CONFIRMATION_PENDING,EnumSet.of(OrderStatus.HELD,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.HELD,EnumSet.of(OrderStatus.PREPARING,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PREPARING,EnumSet.of(OrderStatus.READY_FOR_PICKUP,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.READY_FOR_PICKUP,EnumSet.of(OrderStatus.COMPLETED,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
    }
    public static boolean canTransition(OrderStatus from,OrderStatus to){
        EnumSet<OrderStatus> destinations=ALLOWED.get(from);
        return destinations!=null&&destinations.contains(to);
    }
    public static boolean canTransitionForOrder(OrderStatus from,OrderStatus to,
                                               boolean requiresPrescription,boolean pharmacistApproved){
        if(!canTransition(from,to))return false;
        if(!requiresPrescription)return true;
        if(from==OrderStatus.REQUEST_RECEIVED && to==OrderStatus.STOCK_CONFIRMATION_PENDING)return false;
        switch(to){
            case STOCK_CONFIRMATION_PENDING:case HELD:case PREPARING:
            case READY_FOR_PICKUP:case COMPLETED:return pharmacistApproved;
            default:return true;
        }
    }
    public static CanonicalOrderStage toCanonicalStage(OrderStatus status){
        switch(status){
            case REQUEST_RECEIVED:case STOCK_CONFIRMATION_PENDING:return CanonicalOrderStage.PENDING;
            case RX_REVIEW_PENDING:case RX_NEEDS_UPDATE:return CanonicalOrderStage.RX_PENDING;
            case RX_REJECTED:return CanonicalOrderStage.REJECTED;
            case HELD:case PREPARING:return CanonicalOrderStage.HELD;
            case READY_FOR_PICKUP:return CanonicalOrderStage.READY;
            case COMPLETED:return CanonicalOrderStage.COMPLETED;
            case EXPIRED:case CANCELLED:return CanonicalOrderStage.CANCELLED;
            default:throw new IllegalArgumentException("Unknown status: "+status);
        }
    }
    public static String terminationReasonLabel(OrderStatus status){
        if(status==OrderStatus.EXPIRED)return "Quá hạn giữ hàng";
        if(status==OrderStatus.CANCELLED)return "Đã hủy";
        if(status==OrderStatus.RX_REJECTED)return "Toa bị từ chối";
        return null;
    }
}
