package com.pharmasiu.mobile.data.model

/** Local, in-memory identity. No real server authentication is performed. */
data class MockProfile(
    val displayName: String,
    val phone: String,
    val email: String = "",
    val birthday: String = "",
)

/** Contact address mock; pickup orders always use a separate pickup point. */
data class MockAddress(
    val id: String,
    val label: String,
    val receiver: String,
    val phone: String,
    val detail: String,
    val isDefault: Boolean = false,
)

enum class MockRequestStatus { CREATED_LOCALLY, CANCELLED_LOCALLY }

/**
 * Local requests cannot become stock holds, paid orders or approved prescriptions.
 */
data class MockRequest(
    val reference: String,
    val lines: List<CartLine>,
    val customerName: String,
    val contactPhone: String,
    val pickupPoint: String,
    val displayTime: String,
    val status: MockRequestStatus = MockRequestStatus.CREATED_LOCALLY,
) {
    val hasPrescription: Boolean get() = lines.any { it.medicine.requiresPrescription }
    val illustrativeTotal: Long get() = lines.sumOf { it.medicine.priceVnd * it.quantity }
}
