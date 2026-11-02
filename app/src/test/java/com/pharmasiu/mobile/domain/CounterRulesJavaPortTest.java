package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of CounterRulesTest; uses immutable Java counter-line snapshot contract. */
public class CounterRulesJavaPortTest {
    private final DemoState fixture=new DemoState();
    private final DemoState.Product otc=fixture.product("mask-01");
    private final DemoState.Product rx=fixture.product("rx-demo-01");
    private LegacyDomainRules.CounterLine line(int quantity,boolean prescribed) {
        return new LegacyDomainRules.CounterLine(otc.id,otc.priceVnd,quantity,prescribed);
    }
    @Test public void cannotSellPrescriptionInCashierMock() {
        assertFalse(LegacyDomainRules.counterSellable(rx));
        assertFalse(LegacyDomainRules.availableToAddAtCounter(rx,0,20));
        assertFalse(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(2,true)),
            Collections.singletonMap(otc.id,20),true,LegacyDomainRules.CounterPayment.CASH,1000000));
    }
    @Test public void enforcesStockAndQuantity() {
        assertTrue(LegacyDomainRules.availableToAddAtCounter(otc,1,2));
        assertFalse(LegacyDomainRules.availableToAddAtCounter(otc,2,2));
        assertFalse(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(3,false)),
            Collections.singletonMap(otc.id,2),true,LegacyDomainRules.CounterPayment.OTHER_DEMO,0));
        assertFalse(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(31,false)),
            Collections.singletonMap(otc.id,100),true,LegacyDomainRules.CounterPayment.OTHER_DEMO,0));
    }
    @Test public void paymentRequiresOpenShiftAndSufficientCash() {
        long total=LegacyDomainRules.counterTotal(Arrays.asList(line(2,false)));
        assertFalse(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(2,false)),
            Collections.singletonMap(otc.id,5),false,LegacyDomainRules.CounterPayment.CASH,total));
        assertFalse(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(2,false)),
            Collections.singletonMap(otc.id,5),true,LegacyDomainRules.CounterPayment.CASH,total-1));
        assertTrue(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(2,false)),
            Collections.singletonMap(otc.id,5),true,LegacyDomainRules.CounterPayment.CASH,total));
        assertTrue(LegacyDomainRules.canConfirmCounter(Arrays.asList(line(2,false)),
            Collections.singletonMap(otc.id,5),true,LegacyDomainRules.CounterPayment.OTHER_DEMO,0));
        assertEquals(5000L,LegacyDomainRules.changeDue(total,total+5000));
    }
    @Test public void saleIsImmutableSnapshot() {
        DemoState state = new DemoState();
        assertTrue(state.login("nhanvien@pharmasiu.demo","1111"));
        assertTrue(state.openShift(0));
        assertTrue(state.addPos(otc.id));
        assertTrue(state.addPos(otc.id));
        DemoState.Sale sale=state.savePosSale("CASH",otc.priceVnd*2+10000);
        assertNotNull(sale);
        assertEquals(otc.priceVnd*2,sale.amountVnd);
        assertEquals(10000L,LegacyDomainRules.changeDue(sale.amountVnd,sale.amountVnd+10000));
        assertEquals(2,(int)sale.lines.get(otc.id));
        assertTrue(state.posCart.isEmpty());
        assertEquals(2,(int)sale.lines.get(otc.id)); // Snapshot independent of the now-empty draft.
    }
    @Test public void demoRolesNotInterchangeable() {
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.CASHIER,"1111"));
        assertTrue(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN,"2222"));
        assertFalse(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.ADMIN,"1111"));
        assertFalse(LegacyDomainRules.acceptsPin(LegacyDomainRules.StaffRole.CASHIER,"2222"));
    }
}
