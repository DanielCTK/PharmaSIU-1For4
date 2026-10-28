package com.pharmasiu.mobile.ui.features.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pharmasiu.mobile.data.model.Medicine
import com.pharmasiu.mobile.domain.CounterLine
import com.pharmasiu.mobile.domain.CounterPayment
import com.pharmasiu.mobile.domain.CounterRules
import com.pharmasiu.mobile.domain.CounterSale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * In-memory, counter-only sandbox shared by cashier and admin.
 * NEVER use these numbers to represent real pharmacy or customer inventory.
 */
class StaffDemoStore(private val products: List<Medicine>) {
    val stock = mutableStateMapOf<String, Int>().also { map ->
        products.forEachIndexed { index, p ->
            map[p.id] = if (p.requiresPrescription) 0 else 6 + (index % 9)
        }
    }
    val cart = mutableStateMapOf<String, Int>()
    val sales = mutableStateListOf<CounterSale>()

    var shiftOpen by mutableStateOf(false)
        private set
    var completedInShift by mutableStateOf(0)
        private set
    var shiftRevenueVnd by mutableStateOf(0L)
        private set
    var lastReceipt by mutableStateOf<CounterSale?>(null)
        private set
    private var sequence = 0

    val cartLines: List<CounterLine>
        get() = cart.mapNotNull { (id, qty) ->
            products.firstOrNull { it.id == id }?.let {
                CounterLine(it.id, it.name, it.unit, it.priceVnd, qty, it.requiresPrescription)
            }
        }

    fun openShift() {
        if (!shiftOpen) {
            completedInShift = 0
            shiftRevenueVnd = 0L
            shiftOpen = true
        }
    }

    fun closeShift(): Boolean {
        if (!shiftOpen || cart.isNotEmpty()) return false
        shiftOpen = false
        return true
    }

    fun add(product: Medicine): Boolean {
        val current = cart[product.id] ?: 0
        if (!shiftOpen || !CounterRules.availableToAdd(product, current, stock[product.id] ?: 0)) return false
        cart[product.id] = current + 1
        return true
    }

    fun reduce(productId: String) {
        val qty = (cart[productId] ?: 0) - 1
        if (qty > 0) cart[productId] = qty else cart.remove(productId)
    }

    fun clearDraft() = cart.clear()

    fun adjustDemoStock(productId: String, delta: Int) {
        val product = products.firstOrNull { it.id == productId } ?: return
        if (product.requiresPrescription) return
        val next = (stock[productId] ?: 0).toLong() + delta
        val reservedInDraft = cart[productId] ?: 0
        if (next in reservedInDraft.toLong()..999L) stock[productId] = next.toInt()
    }

    fun complete(payment: CounterPayment, received: Long): CounterSale? {
        val snapshot = cartLines
        if (!CounterRules.canConfirm(snapshot, stock, shiftOpen, payment, received)) return null
        val sale = CounterSale(
            reference = "POS-DEMO-" + (++sequence).toString().padStart(4, '0'),
            cashierName = "Nhân viên demo",
            createdAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date()),
            lines = snapshot,
            payment = payment,
            receivedVnd = if (payment == CounterPayment.CASH) received else 0L,
        )
        // No partial sale: validate all stock first, then decrement as one local action.
        snapshot.forEach { line -> stock[line.productId] = stock.getValue(line.productId) - line.quantity }
        sales.add(sale)
        completedInShift++
        shiftRevenueVnd += sale.totalVnd
        cart.clear()
        lastReceipt = sale
        return sale
    }
}
