package com.pharmasiu.mobile.domain;

import java.util.Objects;

/** Snapshot of counter line, never an authorized pharmacy sale. */
public final class CounterLine {
    public final String productId,name,unit;
    public final long unitPriceVnd;
    public final int quantity;
    public final boolean requiresPrescription;
    public CounterLine(String productId,String name,String unit,long unitPriceVnd,int quantity){
        this(productId,name,unit,unitPriceVnd,quantity,false);
    }
    public CounterLine(String productId,String name,String unit,long unitPriceVnd,int quantity,boolean requiresPrescription){
        this.productId=productId;this.name=name;this.unit=unit;this.unitPriceVnd=unitPriceVnd;
        this.quantity=quantity;this.requiresPrescription=requiresPrescription;
    }
    public CounterLine copy(String productId,String name,String unit,long unitPriceVnd,int quantity,boolean requiresPrescription){
        return new CounterLine(productId,name,unit,unitPriceVnd,quantity,requiresPrescription);
    }
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof CounterLine))return false;
        CounterLine l=(CounterLine)o;return unitPriceVnd==l.unitPriceVnd&&quantity==l.quantity&&
            requiresPrescription==l.requiresPrescription&&Objects.equals(productId,l.productId)&&
            Objects.equals(name,l.name)&&Objects.equals(unit,l.unit);}
    @Override public int hashCode(){return Objects.hash(productId,name,unit,unitPriceVnd,quantity,requiresPrescription);}
}
