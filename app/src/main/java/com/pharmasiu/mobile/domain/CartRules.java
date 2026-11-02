package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.data.model.CartLine;
import java.util.List;

/** Local demo cart guard, not an actual prescription upload/approval. */
public final class CartRules {
    private CartRules(){}
    public static final int MAX_DEMO_QUANTITY=20;
    public static boolean requiresPrescription(List<CartLine> lines){
        for(CartLine l:lines)if(l.medicine.requiresPrescription)return true;
        return false;
    }
    public static boolean canProceedToRequest(List<CartLine> lines,boolean prescriptionAttached){
        if(lines.isEmpty())return false;
        for(CartLine l:lines)if(l.quantity<1||l.quantity>MAX_DEMO_QUANTITY)return false;
        return !requiresPrescription(lines)||prescriptionAttached;
    }
    public static long totalIllustrativeVnd(List<CartLine> lines){
        long total=0;for(CartLine l:lines)total+=l.medicine.priceVnd*l.quantity;return total;
    }
}
