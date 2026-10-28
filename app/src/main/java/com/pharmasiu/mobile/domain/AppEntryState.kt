package com.pharmasiu.mobile.domain

/** One entry for all roles. Never persist a signed-in state. */
enum class AppEntryScreen { LOGIN, STAFF_APP, CUSTOMER_APP }

data class AppEntryState(
    val screen: AppEntryScreen = AppEntryScreen.LOGIN,
    val staffRole: StaffRole? = null,
) {
    fun staffAuthenticated(role: StaffRole): AppEntryState =
        if (screen == AppEntryScreen.LOGIN) AppEntryState(AppEntryScreen.STAFF_APP, role) else this

    fun customerAuthenticated(): AppEntryState =
        if (screen == AppEntryScreen.LOGIN) AppEntryState(AppEntryScreen.CUSTOMER_APP) else this

    fun returnToLogin(): AppEntryState = AppEntryState()
}
