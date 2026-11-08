package com.example.ai

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Parses the JSON returned by Gemini through Firebase AI Logic.
 *
 * Pure Moshi parsing (no Android/Firebase dependencies) so it can be
 * unit-tested without a Firebase project. Throws on malformed JSON — callers
 * translate that into a user-facing failure, never into fabricated items.
 */
object FirebaseAiResponseParser {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val expenseAdapter = moshi.adapter(ExtractedExpense::class.java)
    private val itemsAdapter = moshi.adapter(Array<ExtractedItem>::class.java)

    fun parseExpense(jsonText: String): ExtractedExpense {
        val clean = stripCodeFences(jsonText)
        return expenseAdapter.fromJson(clean) ?: ExtractedExpense()
    }

    fun parseItems(jsonText: String): List<ExtractedItem> {
        val clean = stripCodeFences(jsonText)
        return itemsAdapter.fromJson(clean)?.toList() ?: emptyList()
    }

    internal fun stripCodeFences(jsonText: String): String =
        jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
