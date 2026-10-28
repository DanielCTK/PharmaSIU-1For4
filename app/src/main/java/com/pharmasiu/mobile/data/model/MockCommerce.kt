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

/**
 * A checkout success is simulated on this device.
 * It must never be confused with pharmacist approval, a stock hold or payment.
 */
enum class MockRequestStatus { PLACED_DEMO, CANCELLED_LOCALLY }

/**
 * Local demo orders cannot become stock holds, paid orders or approved prescriptions.
 */
data class MockRequest(
    val reference: String,
    val lines: List<CartLine>,
    val customerName: String,
    val contactPhone: String,
    val pickupPoint: String,
    val displayTime: String,
    val status: MockRequestStatus = MockRequestStatus.PLACED_DEMO,
) {
    val hasPrescription: Boolean get() = lines.any { it.medicine.requiresPrescription }
    val illustrativeTotal: Long get() = lines.sumOf { it.medicine.priceVnd * it.quantity }
}
