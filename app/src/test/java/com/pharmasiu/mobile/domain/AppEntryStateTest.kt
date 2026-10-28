package com.pharmasiu.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppEntryStateTest {
    @Test fun freshLaunchAlwaysShowsSignIn() {
        assertEquals(AppEntryScreen.STAFF_LOGIN, AppEntryState().screen)
        assertEquals(AppEntryScreen.STAFF_LOGIN, AppEntryState().returnToLogin().screen)
    }

    @Test fun customerNeedsSignInBeforeEnteringShop() {
        val initial = AppEntryState()
        assertEquals(AppEntryScreen.STAFF_LOGIN, initial.customerAuthenticated().screen)
        val customerAuth = initial.openCustomerLogin()
        assertEquals(AppEntryScreen.CUSTOMER_LOGIN, customerAuth.screen)
        assertEquals(AppEntryScreen.CUSTOMER_APP, customerAuth.customerAuthenticated().screen)
    }

    @Test fun logoutAndBackDoNotRestorePriorAppScreen() {
        val loggedIn = AppEntryState().openCustomerLogin().customerAuthenticated()
        assertEquals(AppEntryScreen.STAFF_LOGIN, loggedIn.returnToLogin().screen)
        val cancelled = AppEntryState().openCustomerLogin().returnToLogin()
        assertEquals(AppEntryScreen.STAFF_LOGIN, cancelled.screen)
        assertEquals(AppEntryScreen.STAFF_LOGIN, cancelled.customerAuthenticated().screen)
    }
}
