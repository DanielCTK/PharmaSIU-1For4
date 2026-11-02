package com.pharmasiu.mobile.data.model;

import java.util.Objects;

/** Immutable cart line. Quantity must be positive; further max-20 rule is in CartRules. */
public final class CartLine {
    public final Medicine medicine;
    public final int quantity;
    public CartLine(Medicine medicine,int quantity){
        if(quantity<=0)throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        this.medicine=medicine;this.quantity=quantity;
    }
    public CartLine copy(Medicine medicine,int quantity){return new CartLine(medicine,quantity);}
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof CartLine))return false;
        CartLine c=(CartLine)o;return quantity==c.quantity && Objects.equals(medicine,c.medicine);}
    @Override public int hashCode(){return Objects.hash(medicine,quantity);}
    @Override public String toString(){return "CartLine(medicine="+medicine+", quantity="+quantity+")";}
}
