package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import com.pharmasiu.mobile.legacy.LegacyDomainRules;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of CartRulesTest; domain contract and live DemoState Rx picker both exercised. */
public class CartRulesJavaPortTest {
    @Test public void rxRequestsRequireAttachment() {
        DemoState state = new DemoState();
        LegacyDomainRules.CartLine rx = new LegacyDomainRules.CartLine(state.product("rx-demo-01"),1);
        assertFalse(LegacyDomainRules.canProceedToRequest(Arrays.asList(rx),false));
        assertTrue(LegacyDomainRules.canProceedToRequest(Arrays.asList(rx),true));
        assertTrue(state.login("0900000000","demo1234"));
        assertTrue(state.addToCart("rx-demo-01"));
        assertFalse(state.canSaveRequest("Khách demo","0900000000","Điểm nhận mẫu A"));
        state.attachLocalPrescription("toa-tai-may.pdf");
        assertTrue(state.localPrescriptionSelected());
        assertTrue(state.canSaveRequest("Khách demo","0900000000","Điểm nhận mẫu A"));
    }
    @Test public void otcDoesNotRequireAttachment() {
        DemoState state = new DemoState();
        assertTrue(LegacyDomainRules.canProceedToRequest(
            Arrays.asList(new LegacyDomainRules.CartLine(state.product("mask-01"),1)), false));
        assertTrue(state.login("0900000000","demo1234"));
        assertTrue(state.addToCart("mask-01"));
        assertTrue(state.canSaveRequest("Khách demo","0900000000","Điểm nhận mẫu A"));
    }
    @Test public void emptyRequestIsBlocked() {
        assertFalse(LegacyDomainRules.canProceedToRequest(Collections.emptyList(),true));
        DemoState state=new DemoState();
        assertTrue(state.login("0900000000","demo1234"));
        assertFalse(state.canSaveRequest("Khách demo","0900000000","Điểm nhận mẫu A"));
    }
}
