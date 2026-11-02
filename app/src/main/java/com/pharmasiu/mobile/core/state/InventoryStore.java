package com.pharmasiu.mobile.core.state;

import com.pharmasiu.mobile.core.DemoState;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** RAM inventory, dated lots, audit and closed-shift reconciliation; never a real stock ledger. */
public final class InventoryStore {
    private final DemoState state;
    private final Set<Integer> reconciledShifts = new HashSet<>();
    public InventoryStore(DemoState state){this.state=state;}
    public boolean renameProduct(String id,String name,long price){
        if(state.isDatabaseBacked()){
            if(!state.canAdmin())return false;
            boolean changed=state.repository().updateProduct(
                state.authenticatedAccountId(),id,name,price);
            if(changed)state.refreshDatabaseViews();
            return changed;
        }
        DemoState.Product p=state.product(id);
        if(!state.canAdmin()||p==null||p.requiresPrescription||state.posCart.containsKey(id)||
            name==null||name.trim().length()<2||price<=0)return false;
        p.name=name.trim();p.priceVnd=price;return true;
    }
    public boolean receiveLot(String productId,String code,int qty,LocalDate expires){
        if(state.isDatabaseBacked()){
            if(!state.canAdmin())return false;
            boolean stored=state.repository().receiveLot(
                state.authenticatedAccountId(),productId,code,qty,expires,false);
            if(stored)state.refreshDatabaseViews();
            return stored;
        }
        DemoState.Product p=state.product(productId);
        if(!state.canAdmin()||p==null||p.requiresPrescription||code==null
            ||code.trim().length()<2||code.trim().length()>40
            ||qty<=0||qty>999||expires==null||!expires.isAfter(LocalDate.now()))return false;
        long existingTotal=0L;
        for(DemoState.Lot lot:state.lots){
            if(lot.productId.equals(productId))existingTotal+=lot.quantity;
            // A globally unique code is required because the Java demo quarantine UI selects by code.
            if(lot.code.equalsIgnoreCase(code.trim()))return false;
        }
        if(existingTotal+qty>999L)return false;
        state.lots.add(new DemoState.Lot(code.trim(),productId,qty,expires));
        state.stock.put(productId,state.stock.getOrDefault(productId,0)+qty);return true;
    }
    public boolean quarantineLot(String code){
        if(state.isDatabaseBacked()){
            boolean changed=state.repository().quarantineLot(
                state.authenticatedAccountId(),code,true);
            if(changed)state.refreshDatabaseViews();
            return changed;
        }
        if(!state.canAdmin())return false;
        for(DemoState.Lot lot:state.lots)if(lot.code.equals(code)){
            if(lot.quarantined)return false;
            int remaining=Math.max(0,state.stock.getOrDefault(lot.productId,0)-lot.quantity);
            if(state.posCart.getOrDefault(lot.productId,0)>remaining)return false;
            boolean hasDatedLots=false;
            int remainingEligible=0;
            for(DemoState.Lot candidate:state.lots)if(candidate.productId.equals(lot.productId)){
                hasDatedLots=true;
                if(candidate!=lot && !candidate.quarantined && candidate.quantity>0
                    && candidate.expiresOn!=null && candidate.expiresOn.isAfter(LocalDate.now()))
                    remainingEligible+=candidate.quantity;
            }
            if(hasDatedLots && state.posCart.getOrDefault(lot.productId,0)>remainingEligible)return false;
            lot.quarantined=true;
            state.stock.put(lot.productId,remaining);
            return true;
        }
        return false;
    }
    /** Receive an explicitly quarantined sample lot, including an already expired batch.
     * Such a lot never enters sellable demo state.stock or FEFO allocation. */
    public boolean receiveQuarantinedLot(String productId,String code,int qty,LocalDate expires){
        if(state.isDatabaseBacked()){
            boolean changed=state.repository().receiveLot(
                state.authenticatedAccountId(),productId,code,qty,expires,true);
            if(changed)state.refreshDatabaseViews();
            return changed;
        }
        DemoState.Product p=state.product(productId);
        if(!state.canAdmin()||p==null||p.requiresPrescription||code==null
            ||code.trim().length()<2||code.trim().length()>40||qty<1||qty>999
            ||expires==null)return false;
        long existing=0;
        for(DemoState.Lot lot:state.lots){
            if(lot.productId.equals(productId))existing+=lot.quantity;
            if(lot.code.equalsIgnoreCase(code.trim()))return false;
        }
        if(existing+qty>999L)return false;
        DemoState.Lot lot=new DemoState.Lot(code.trim(),productId,qty,expires);
        lot.quarantined=true;
        state.lots.add(lot);
        return true;
    }
    /** Reversible local lot condition. Expired batches remain ineligible after unquarantine. */
    public boolean setLotQuarantined(String code,boolean quarantined){
        if(state.isDatabaseBacked()){
            boolean changed=state.repository().quarantineLot(
                state.authenticatedAccountId(),code,quarantined);
            if(changed)state.refreshDatabaseViews();
            return changed;
        }
        if(!state.canAdmin()||code==null)return false;
        if(quarantined)return quarantineLot(code);
        for(DemoState.Lot lot:state.lots)if(lot.code.equals(code)){
            if(!lot.quarantined)return false;
            lot.quarantined=false;
            if(lot.expiresOn!=null&&lot.expiresOn.isAfter(LocalDate.now()))
                state.stock.put(lot.productId,state.stock.getOrDefault(lot.productId,0)+lot.quantity);
            return true;
        }
        return false;
    }
    public int eligibleLotQty(String productId,LocalDate today){
        int sum=0;for(DemoState.Lot lot:state.lots)if(lot.productId.equals(productId)&&!lot.quarantined
           &&lot.expiresOn!=null&&lot.expiresOn.isAfter(today))sum+=lot.quantity;
        return sum;
    }
    public boolean countDemoStock(String productId,int counted,String reason) {
        if(!state.canAdmin()||state.product(productId)==null||counted<0||
            reason==null||reason.trim().length()<3)return false;
        int system=state.stock.getOrDefault(productId,0);
        state.inventoryCountDetails.add(0,new DemoState.DemoInventoryCount(
            productId,state.product(productId).name,system,counted,reason.trim()));
        state.inventoryChecks.add(0,productId+": tồn DEMO "+system+
            " · đếm "+counted+" · lệch "+(counted-system)+" · lý do: "+reason.trim());
        return true; // Record only; never modify authoritative state.stock.
    }
    public boolean reconcileDemoShift(long counted,String reason) {
        if(!state.canAdmin()||state.shiftOpen||state.shiftNumber<=0||reconciledShifts.contains(state.shiftNumber)||
            counted<0||counted>1_000_000_000_000L||reason==null||reason.trim().length()<3)return false;
        long expected=state.openingCashVnd+state.cashCollectedVnd;
        int invoices=0;
        long cashSales=0,otherDemo=0;
        for(DemoState.Sale sale:state.sales)if(sale.shiftNumber==state.shiftNumber){
            invoices++;
            if("CASH".equals(sale.method))cashSales+=sale.amountVnd;
            else otherDemo+=sale.amountVnd;
        }
        state.shiftReconciliationDetails.add(0,new DemoState.DemoShiftReconciliation(
            state.shiftNumber,invoices,state.openingCashVnd,cashSales,otherDemo,counted));
        state.shiftReconciliations.add(0,"Ca #"+state.shiftNumber+": kỳ vọng "+expected+
            " VND · kiểm đếm "+counted+" VND · lệch "+(counted-expected)+
            " VND · ghi chú: "+reason.trim());
        state.cashCountedVnd=counted;
        reconciledShifts.add(state.shiftNumber);
        return true; // Only RAM sample comparison, not a settled payment.
    }

}
