package com.pharmasiu.mobile.domain;

import java.util.Objects;

/** Subtotal of priced demo lines only; not a final payable quote. */
public final class MockPriceSummary {
    public final long pricedSubtotalVnd;
    public final int unpricedLineCount;
    public final boolean includesUnpricedItems;
    public MockPriceSummary(long pricedSubtotalVnd,int unpricedLineCount){
        this.pricedSubtotalVnd=pricedSubtotalVnd;this.unpricedLineCount=unpricedLineCount;
        this.includesUnpricedItems=unpricedLineCount>0;
    }
    public boolean includesUnpricedItems(){return includesUnpricedItems;}
    @Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof MockPriceSummary))return false;
        MockPriceSummary m=(MockPriceSummary)o;return pricedSubtotalVnd==m.pricedSubtotalVnd&&unpricedLineCount==m.unpricedLineCount;}
    @Override public int hashCode(){return Objects.hash(pricedSubtotalVnd,unpricedLineCount);}
}
