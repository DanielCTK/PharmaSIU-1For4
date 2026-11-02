package com.pharmasiu.mobile.domain;

import java.util.Objects;

public final class DemoLotAllocation {
    public final String lotId;
    public final int quantity;
    public DemoLotAllocation(String lotId,int quantity){this.lotId=lotId;this.quantity=quantity;}
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof DemoLotAllocation))return false;
        DemoLotAllocation a=(DemoLotAllocation)o;return quantity==a.quantity&&Objects.equals(lotId,a.lotId);}
    @Override public int hashCode(){return Objects.hash(lotId,quantity);}
}
