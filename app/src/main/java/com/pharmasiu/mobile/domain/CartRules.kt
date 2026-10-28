package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.model.CartLine

object CartRules {
    const val MAX_DEMO_QUANTITY = 20

    fun requiresPrescription(lines: List<CartLine>): Boolean =
        lines.any { it.medicine.requiresPrescription }

    fun canProceedToRequest(lines: List<CartLine>, prescriptionAttached: Boolean): Boolean =
        lines.isNotEmpty() &&
            lines.all { it.quantity in 1..MAX_DEMO_QUANTITY } &&
            (!requiresPrescription(lines) || prescriptionAttached)

    fun totalIllustrativeVnd(lines: List<CartLine>): Long =
        lines.sumOf { it.medicine.priceVnd * it.quantity }
}
