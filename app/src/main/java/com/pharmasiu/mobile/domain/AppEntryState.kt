package com.pharmasiu.mobile.domain

/** App entry routing. A new activity always starts unauthenticated. */
enum class AppEntryScreen { STAFF_LOGIN, CUSTOMER_LOGIN, CUSTOMER_APP }

data class AppEntryState(val screen: AppEntryScreen = AppEntryScreen.STAFF_LOGIN) {
    fun openCustomerLogin(): AppEntryState =
        copy(screen = AppEntryScreen.CUSTOMER_LOGIN)

    /** Customer shop is only reachable after a successful customer login/register. */
    fun customerAuthenticated(): AppEntryState =
        if (screen == AppEntryScreen.CUSTOMER_LOGIN)
            copy(screen = AppEntryScreen.CUSTOMER_APP)
        else this

    /** Both logout and back from the customer login reset the entry to sign in. */
    fun returnToLogin(): AppEntryState =
        AppEntryState(AppEntryScreen.STAFF_LOGIN)
}
