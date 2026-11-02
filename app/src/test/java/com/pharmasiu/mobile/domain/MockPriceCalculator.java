package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.data.model.CartLine;
import java.util.List;

/** Excludes Rx/unpriced items from priced subtotal and counts them separately. */
public final class MockPriceCalculator {
    private MockPriceCalculator(){}
    public static MockPriceSummary summarize(List<CartLine> lines){
        long subtotal=0;int unpriced=0;
        for(CartLine l:lines){
            if(l.medicine.priceVnd>0)subtotal+=l.medicine.priceVnd*l.quantity;
            else unpriced++;
        }
        return new MockPriceSummary(subtotal,unpriced);
    }
}
