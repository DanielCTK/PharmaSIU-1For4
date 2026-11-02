package com.pharmasiu.mobile.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** All-or-nothing FEFO over RAM demo lots; zero/expired/quarantined lots rejected. */
public final class MockLotRules {
    private MockLotRules(){}
    public static boolean isEligible(DemoLot lot){return isEligible(lot,LocalDate.now());}
    public static boolean isEligible(DemoLot lot,LocalDate today){
        return lot.quantity>0&&lot.condition==DemoLotCondition.AVAILABLE_DEMO&&
            (lot.expiresOn==null||lot.expiresOn.isAfter(today));
    }
    public static int eligibleQuantity(List<DemoLot> lots,String productId){
        return eligibleQuantity(lots,productId,LocalDate.now());
    }
    public static int eligibleQuantity(List<DemoLot> lots,String productId,LocalDate today){
        int total=0;for(DemoLot l:lots)if(l.productId.equals(productId)&&isEligible(l,today))total+=l.quantity;
        return total;
    }
    public static List<DemoLotAllocation> allocate(List<DemoLot> lots,String productId,int requested){
        return allocate(lots,productId,requested,LocalDate.now());
    }
    public static List<DemoLotAllocation> allocate(List<DemoLot> lots,String productId,int requested,LocalDate today){
        if(requested<=0)return null;
        List<DemoLot> candidates=new ArrayList<>();
        for(DemoLot l:lots)if(l.productId.equals(productId)&&isEligible(l,today))candidates.add(l);
        candidates.sort(Comparator.comparing((DemoLot l)->l.expiresOn==null)
            .thenComparing(l->l.expiresOn,Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(l->l.id));
        int remaining=requested;List<DemoLotAllocation> picked=new ArrayList<>();
        for(DemoLot l:candidates){
            if(remaining==0)break;
            int take=Math.min(remaining,l.quantity);
            picked.add(new DemoLotAllocation(l.id,take));remaining-=take;
        }
        return remaining==0?picked:null;
    }
}
