package com.pharmasiu.mobile.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Immutable RAM sale snapshot, not a settled real-world payment. */
public final class CounterSale {
    public final String reference,cashierName,createdAt;
    public final List<CounterLine> lines;
    public final CounterPayment payment;
    public final long receivedVnd;
    public final long totalVnd,changeVnd;
    public CounterSale(String reference,String cashierName,String createdAt,List<CounterLine> lines,
                       CounterPayment payment,long receivedVnd){
        this.reference=reference;this.cashierName=cashierName;this.createdAt=createdAt;
        this.lines=Collections.unmodifiableList(new ArrayList<>(lines));this.payment=payment;this.receivedVnd=receivedVnd;
        this.totalVnd=CounterRules.total(this.lines);
        this.changeVnd=payment==CounterPayment.CASH?CounterRules.changeDue(totalVnd,receivedVnd):0L;
    }
    public long totalVnd(){return totalVnd;}
    public long changeVnd(){return changeVnd;}
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof CounterSale))return false;
        CounterSale s=(CounterSale)o;return receivedVnd==s.receivedVnd&&Objects.equals(reference,s.reference)&&
            Objects.equals(cashierName,s.cashierName)&&Objects.equals(createdAt,s.createdAt)&&
            Objects.equals(lines,s.lines)&&payment==s.payment;}
    @Override public int hashCode(){return Objects.hash(reference,cashierName,createdAt,lines,payment,receivedVnd);}
}
