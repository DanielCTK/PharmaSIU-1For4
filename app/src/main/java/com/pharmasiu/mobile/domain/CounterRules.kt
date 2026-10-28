package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.model.Medicine

/** POS demo policy. No clinical approval, batch assignment or server inventory. */
object CounterRules {
    const val MAX_PER_LINE = 30

    fun sellable(product: Medicine): Boolean =
        !product.requiresPrescription && product.priceVnd > 0L

    fun availableToAdd(
        product: Medicine,
        alreadyInCart: Int,
        demoAvailable: Int,
    ): Boolean = sellable(product) &&
        alreadyInCart >= 0 &&
        demoAvailable > alreadyInCart &&
        alreadyInCart < MAX_PER_LINE

    fun total(lines: List<CounterLine>): Long =
        lines.sumOf { it.unitPriceVnd * it.quantity }

    fun canConfirm(
        lines: List<CounterLine>,
        stockById: Map<String, Int>,
        shiftOpen: Boolean,
        method: CounterPayment,
        amountTendered: Long,
    ): Boolean {
        if (!shiftOpen || lines.isEmpty() || lines.any {
                it.quantity !in 1..MAX_PER_LINE ||
                    it.unitPriceVnd <= 0L ||
                    it.requiresPrescription ||
                    (stockById[it.productId] ?: 0) < it.quantity
            }) return false
        val total = total(lines)
        return total > 0L && (method != CounterPayment.CASH || amountTendered >= total)
    }

    fun changeDue(total: Long, tendered: Long): Long = (tendered - total).coerceAtLeast(0)
}

enum class CounterPayment { CASH, OTHER_DEMO }

/** Immutable snapshot of products sold at a demo counter. */
data class CounterLine(
    val productId: String,
    val name: String,
    val unit: String,
    val unitPriceVnd: Long,
    val quantity: Int,
    val requiresPrescription: Boolean = false,
)

data class CounterSale(
    val reference: String,
    val cashierName: String,
    val createdAt: String,
    val lines: List<CounterLine>,
    val payment: CounterPayment,
    val receivedVnd: Long,
) {
    val totalVnd: Long get() = CounterRules.total(lines)
    val changeVnd: Long get() =
        if (payment == CounterPayment.CASH) CounterRules.changeDue(totalVnd, receivedVnd) else 0L
}

enum class StaffRole(val label: String) { CASHIER("Nhân viên bán hàng"), ADMIN("Quản trị") }

/** The demo PINs are publicly displayed, not real credentials or security. */
object StaffDemoAccess {
    fun accepts(role: StaffRole, pin: String): Boolean = when (role) {
        StaffRole.CASHIER -> pin == "1111"
        StaffRole.ADMIN -> pin == "2222"
    }
}
