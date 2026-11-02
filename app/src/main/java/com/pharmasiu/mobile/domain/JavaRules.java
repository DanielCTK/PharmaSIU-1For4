package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/** Pure-Java rules for the Android Views prototype; not a backend authorization layer. */
public final class JavaRules {
    private JavaRules() {}

    public enum OrderStatus {
        REQUEST_RECEIVED, RX_REVIEW_PENDING, RX_NEEDS_UPDATE, RX_REJECTED,
        STOCK_CONFIRMATION_PENDING, HELD, PREPARING, READY_FOR_PICKUP,
        COMPLETED, EXPIRED, CANCELLED
    }
    private static final Map<OrderStatus,EnumSet<OrderStatus>> TRANSITIONS =
        new EnumMap<>(OrderStatus.class);
    static {
        TRANSITIONS.put(OrderStatus.REQUEST_RECEIVED,
            EnumSet.of(OrderStatus.RX_REVIEW_PENDING,OrderStatus.STOCK_CONFIRMATION_PENDING,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.RX_REVIEW_PENDING,
            EnumSet.of(OrderStatus.RX_NEEDS_UPDATE,OrderStatus.RX_REJECTED,
                OrderStatus.STOCK_CONFIRMATION_PENDING,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.RX_NEEDS_UPDATE,
            EnumSet.of(OrderStatus.RX_REVIEW_PENDING,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.STOCK_CONFIRMATION_PENDING,
            EnumSet.of(OrderStatus.HELD,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.HELD,
            EnumSet.of(OrderStatus.PREPARING,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.PREPARING,
            EnumSet.of(OrderStatus.READY_FOR_PICKUP,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
        TRANSITIONS.put(OrderStatus.READY_FOR_PICKUP,
            EnumSet.of(OrderStatus.COMPLETED,OrderStatus.EXPIRED,OrderStatus.CANCELLED));
    }
    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        EnumSet<OrderStatus> permitted=TRANSITIONS.get(from);
        return permitted!=null && permitted.contains(to);
    }
    public static boolean canTransitionForOrder(OrderStatus from,OrderStatus to,
                                                  boolean needsRx,boolean pharmacistApproved) {
        if(!canTransition(from,to))return false;
        if(!needsRx)return true;
        if(from==OrderStatus.REQUEST_RECEIVED && to==OrderStatus.STOCK_CONFIRMATION_PENDING)return false;
        if(to==OrderStatus.STOCK_CONFIRMATION_PENDING||to==OrderStatus.HELD
             ||to==OrderStatus.PREPARING||to==OrderStatus.READY_FOR_PICKUP
             ||to==OrderStatus.COMPLETED)return pharmacistApproved;
        return true;
    }
    public static boolean eligibleLot(DemoState.Lot lot,LocalDate today) {
        return lot!=null && lot.quantity>0 && !lot.quarantined
            && lot.expiresOn!=null && lot.expiresOn.isAfter(today);
    }
    public static final class Allocation {
        public final String code; public final int quantity;
        public Allocation(String code,int quantity){this.code=code;this.quantity=quantity;}
    }
    /** All-or-nothing allocation over mock dated lots only, ordered FEFO. */
    public static List<Allocation> allocateFefo(List<DemoState.Lot> lots, String productId,
                                                int qty, LocalDate today) {
        if(qty<1||productId==null)return null;
        List<DemoState.Lot> filtered=new ArrayList<>();
        for(DemoState.Lot lot:lots)
            if(lot.productId.equals(productId)&&eligibleLot(lot,today))filtered.add(lot);
        filtered.sort(Comparator.comparing((DemoState.Lot l)->l.expiresOn).thenComparing(l->l.code));
        int remaining=qty;
        List<Allocation> result=new ArrayList<>();
        for(DemoState.Lot lot:filtered) {
            int take=Math.min(remaining,lot.quantity);
            if(take>0)result.add(new Allocation(lot.code,take));
            remaining-=take;if(remaining==0)return result;
        }
        return null;
    }
    public static boolean canSellDemo(DemoState.Product product,int qty,int available,
                                      boolean shiftOpen) {
        return shiftOpen && product!=null && !product.requiresPrescription &&
            product.priceVnd>0 && qty>0 && qty<=30 && qty<=available;
    }
    public static boolean canEditCatalog(String role) {
        return DemoState.ADMIN.equals(role);
    }
    public static boolean canApproveRx(String role) { return false; }
    public static String publicStage(OrderStatus s) {
        switch(s) {
            case REQUEST_RECEIVED: case STOCK_CONFIRMATION_PENDING:return "CHỜ XỬ LÝ";
            case RX_REVIEW_PENDING: case RX_NEEDS_UPDATE:return "CHỜ DUYỆT TOA";
            case RX_REJECTED:return "TỪ CHỐI";
            case HELD:case PREPARING:return "ĐÃ GIỮ HÀNG";
            case READY_FOR_PICKUP:return "SẴN SÀNG NHẬN";
            case COMPLETED:return "HOÀN TẤT";
            case CANCELLED:case EXPIRED:return "ĐÃ HỦY";
            default:throw new IllegalArgumentException();
        }
    }
}
