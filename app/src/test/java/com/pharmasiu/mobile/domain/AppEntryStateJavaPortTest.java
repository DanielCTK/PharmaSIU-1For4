package com.pharmasiu.mobile.domain;

import com.pharmasiu.mobile.core.DemoState;
import org.junit.Test;
import static org.junit.Assert.*;

/** Port of AppEntryStateTest: the Java shell derives entry state from DemoState.role. */
public class AppEntryStateJavaPortTest {
    @Test public void freshLaunchAlwaysShowsSignIn() {
        DemoState initial = new DemoState();
        assertEquals(DemoState.NONE, initial.role);
        initial.logout();
        assertEquals(DemoState.NONE, initial.role);
    }
    @Test public void customerNeedsSignInBeforeEnteringShop() {
        DemoState state = new DemoState();
        assertFalse(state.addToCart("mask-01"));
        assertTrue(state.login("0900000000", "demo1234"));
        assertEquals(DemoState.CUSTOMER, state.role);
    }
    @Test public void logoutAndBackDoNotRestorePriorAppScreen() {
        DemoState state = new DemoState();
        assertTrue(state.login("0900000000", "demo1234"));
        state.logout();
        assertEquals(DemoState.NONE, state.role);
        assertTrue(state.login("quanly@pharmasiu.demo", "2222"));
        assertEquals(DemoState.ADMIN, state.role);
        // The Java shell only presents customer pages when CUSTOMER role is active.
        assertFalse(state.addToCart("mask-01"));
        assertEquals(DemoState.ADMIN, state.role);
        state.logout();
        assertEquals(DemoState.NONE, state.role);
    }
}
