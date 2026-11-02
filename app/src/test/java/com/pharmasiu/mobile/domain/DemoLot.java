package com.pharmasiu.mobile.domain;

import java.time.LocalDate;
import java.util.Objects;

/** Immutable RAM demo lot; undated fixtures only eligible as last-resort fallback. */
public final class DemoLot {
    public final String id,productId,code;
    public final int quantity;
    public final LocalDate expiresOn;
    public final DemoLotCondition condition;
    public DemoLot(String id,String productId,String code,int quantity,LocalDate expiresOn){
        this(id,productId,code,quantity,expiresOn,DemoLotCondition.AVAILABLE_DEMO);
    }
    public DemoLot(String id,String productId,String code,int quantity,LocalDate expiresOn,DemoLotCondition condition){
        this.id=id;this.productId=productId;this.code=code;this.quantity=quantity;this.expiresOn=expiresOn;this.condition=condition;
    }
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof DemoLot))return false;
        DemoLot l=(DemoLot)o;return quantity==l.quantity&&Objects.equals(id,l.id)&&Objects.equals(productId,l.productId)&&
            Objects.equals(code,l.code)&&Objects.equals(expiresOn,l.expiresOn)&&condition==l.condition;}
    @Override public int hashCode(){return Objects.hash(id,productId,code,quantity,expiresOn,condition);}
}
