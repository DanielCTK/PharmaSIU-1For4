package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.data.model.Medicine;
import java.util.List;
import java.util.Map;

/** POS demo guards, not pharmacist, stock allocation, or server sale approval. */
public final class CounterRules {
    private CounterRules(){}
    public static final int MAX_PER_LINE=30;
    public static boolean sellable(Medicine product){
        return !product.requiresPrescription&&product.priceVnd>0;
    }
    public static boolean availableToAdd(Medicine product,int alreadyInCart,int demoAvailable){
        return sellable(product)&&alreadyInCart>=0&&demoAvailable>alreadyInCart&&alreadyInCart<MAX_PER_LINE;
    }
    public static long total(List<CounterLine> lines){
        long sum=0;for(CounterLine l:lines)sum+=l.unitPriceVnd*l.quantity;return sum;
    }
    public static boolean canConfirm(List<CounterLine> lines,Map<String,Integer> stockById,
                                     boolean shiftOpen,CounterPayment method,long amountTendered){
        if(!shiftOpen||lines.isEmpty())return false;
        for(CounterLine l:lines){
            if(l.quantity<1||l.quantity>MAX_PER_LINE||l.unitPriceVnd<=0||
                l.requiresPrescription||stockById.getOrDefault(l.productId,0)<l.quantity)return false;
        }
        long total=total(lines);
        return total>0&&(method!=CounterPayment.CASH||amountTendered>=total);
    }
    public static long changeDue(long total,long tendered){return Math.max(tendered-total,0);}
}
