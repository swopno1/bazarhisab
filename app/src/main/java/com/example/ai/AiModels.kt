package com.example.ai

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExtractedExpense(
    val date: String? = null,
    val merchant: String? = null,
    val currency: String = "BDT",
    val subtotal: Double? = null,
    val discount: Double? = null,
    val tax: Double? = null,
    val total: Double? = null,
    val items: List<ExtractedItem> = emptyList(),
    val category: String? = null,
    val paymentMethod: String? = "cash",
    val notes: String? = null,
    val confidence: Float = 0.9f,
    /**
     * True when this result came from the labelled on-device fallback
     * (AI unavailable) rather than Gemini. The UI surfaces this instead
     * of pretending AI answered. Never set by the Gemini path.
     */
    val aiOfflineFallback: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class ExtractedItem(
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val unitPrice: Double? = null,
    val amount: Double? = null,
    val category: String? = null,
    val confidence: Float = 0.9f,
    /** True when this item came from the labelled on-device fallback (AI unavailable). */
    val aiOfflineFallback: Boolean = false,
)
