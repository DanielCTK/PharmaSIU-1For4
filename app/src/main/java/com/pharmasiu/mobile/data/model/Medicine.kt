package com.pharmasiu.mobile.data.model

/**
 * Public-facing catalog model. These are demo-only values until connected to a backend.
 * The client must never infer confirmed stock or Rx approval from this record.
 */
data class Medicine(
    val id: String,
    val name: String,
    val subtitle: String,
    val priceVnd: Long,
    val category: String,
    val requiresPrescription: Boolean,
    val unit: String,
    val description: String,
    /**
     * Optional HTTPS URL supplied by the official catalog service.
     * The offline preview/demo uses in-app artwork and never fetches an image.
     */
    val imageUrl: String? = null,
)

data class CartLine(
    val medicine: Medicine,
    val quantity: Int,
) {
    init { require(quantity > 0) { "Số lượng phải lớn hơn 0" } }
}
