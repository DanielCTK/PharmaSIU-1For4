package com.pharmasiu.mobile.domain

import com.pharmasiu.mobile.data.model.Medicine
import java.text.Normalizer
import java.util.Locale

/** Pure Kotlin search/filter/sort logic for the deterministic offline catalog. */
object CatalogSearchRules {
    fun normalized(text: String): String =
        Normalizer.normalize(text.lowercase(Locale("vi", "VN")), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .trim()

    fun filter(
        products: List<Medicine>,
        query: String,
        category: String,
        sort: String,
    ): List<Medicine> {
        val words = normalized(query).split(Regex("\\s+")).filter { it.isNotBlank() }
        val result = products.filter { item ->
            val searchable = normalized(item.name + " " + item.subtitle + " " + item.category)
            val queryMatches = words.all { searchable.contains(it) }
            val categoryMatches = when (category) {
                "Tất cả" -> true
                "Kê đơn" -> item.requiresPrescription
                "Thực phẩm" -> item.category == "Thực phẩm" || item.category == "Thực phẩm bổ sung"
                else -> item.category == category
            }
            queryMatches && categoryMatches
        }
        return when (sort) {
            "Tên A–Z" -> result.sortedWith(compareBy<Medicine, String>(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.id })
            "Giá thấp" -> result.sortedWith(compareBy<Medicine> { it.priceVnd <= 0L }.thenBy { it.priceVnd }.thenBy { it.id })
            "Giá cao" -> result.sortedWith(compareBy<Medicine> { it.priceVnd <= 0L }.thenByDescending { it.priceVnd }.thenBy { it.id })
            else -> result
        }
    }
}
