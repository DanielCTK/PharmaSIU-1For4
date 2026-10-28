package com.pharmasiu.mobile.domain

import java.util.Locale

/** Only for the offline demonstration; never use client credentials as production auth. */
data class DemoCustomerIdentity(val phone: String, val email: String?, val password: String)

sealed interface DemoLoginIdentity {
    data class Staff(val role: StaffRole) : DemoLoginIdentity
    data class Customer(val phone: String) : DemoLoginIdentity
}

object UnifiedDemoAuth {
    fun authenticate(
        identifier: String,
        password: String,
        customers: Collection<DemoCustomerIdentity>,
    ): DemoLoginIdentity? {
        val normalized = identifier.trim().lowercase(Locale.ROOT)
        if (normalized.isBlank() || password.isEmpty()) return null
        StaffDemoAccess.authenticate(normalized, password)?.let {
            return DemoLoginIdentity.Staff(it)
        }
        return customers.firstOrNull {
            (it.phone == normalized || it.email?.trim()?.lowercase(Locale.ROOT) == normalized) &&
                it.password == password
        }?.let { DemoLoginIdentity.Customer(it.phone) }
    }
}
