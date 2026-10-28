package com.pharmasiu.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppEntryStateTest {
    @Test fun freshLaunchAlwaysShowsSignIn() {
        assertEquals(AppEntryScreen.LOGIN, AppEntryState().screen)
        assertEquals(AppEntryScreen.LOGIN, AppEntryState().returnToLogin().screen)
    }

    @Test fun customerNeedsSignInBeforeEnteringShop() {
        val initial = AppEntryState()
        assertEquals(AppEntryScreen.CUSTOMER_APP, initial.customerAuthenticated().screen)
    }

    @Test fun logoutAndBackDoNotRestorePriorAppScreen() {
        val loggedIn = AppEntryState().customerAuthenticated()
        assertEquals(AppEntryScreen.LOGIN, loggedIn.returnToLogin().screen)
        val manager = AppEntryState().staffAuthenticated(StaffRole.ADMIN)
        assertEquals(AppEntryScreen.STAFF_APP, manager.screen)
        assertEquals(StaffRole.ADMIN, manager.staffRole)
        assertEquals(AppEntryScreen.LOGIN, manager.returnToLogin().screen)
        assertEquals(AppEntryScreen.STAFF_APP, manager.customerAuthenticated().screen)
    }
}
