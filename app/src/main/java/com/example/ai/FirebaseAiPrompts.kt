package com.example.ai

import android.graphics.Bitmap

/**
 * Prompts sent to Gemini through Firebase AI Logic.
 *
 * Kept in a separate object (no Android/Firebase dependencies) so the prompt
 * text can be unit-tested without a Firebase project. The JSON schemas here
 * must stay in sync with [ExtractedExpense] / [ExtractedItem].
 */
object FirebaseAiPrompts {

    const val CATEGORIES =
        "groceries, vegetables, fish, meat, eggs, rice_grains, oil_spices, " +
            "fruits, household, food, medicine, transport, bills, other"

    fun receiptPrompt(): String = """
        You are an expert Bangladeshi household expense extraction engine.
        Analyze this receipt image (written in Bengali, English, Banglish, or mixed).
        Extract items, quantities, units, prices, and totals.
        Default currency is BDT.
        Categories must be one of: [$CATEGORIES].
        Never invent missing prices. If price or quantity is missing, use null.
        Return ONLY valid JSON matching this schema:
        {
          "merchant": "Store name or null",
          "date": "YYYY-MM-DD or null",
          "currency": "BDT",
          "subtotal": 0.0,
          "discount": 0.0,
          "tax": 0.0,
          "total": 0.0,
          "items": [
            {
              "name": "Item name",
              "quantity": 1.0,
              "unit": "kg",
              "unitPrice": 100.0,
              "amount": 100.0,
              "category": "vegetables",
              "confidence": 0.95
            }
          ],
          "category": "groceries",
          "paymentMethod": "cash",
          "notes": null,
          "confidence": 0.9
        }
    """.trimIndent()

    fun voicePrompt(text: String): String = """
        You are an expense extraction assistant for Bangladeshi households.
        The user spoke this expense: "$text".
        Extract items, quantities, units, and amounts.
        Default currency is BDT.
        Categories must be one of: [$CATEGORIES].
        Never invent missing prices. If price or quantity is missing, use null.
        Return ONLY valid JSON matching this schema:
        {
          "merchant": null,
          "total": 0.0,
          "items": [
            {
              "name": "Item name",
              "quantity": 1.0,
              "unit": "kg",
              "amount": 100.0,
              "category": "vegetables",
              "confidence": 0.95
            }
          ],
          "category": "groceries",
          "paymentMethod": "cash",
          "confidence": 0.9
        }
    """.trimIndent()

    fun shoppingListPrompt(text: String): String = """
        Extract grocery shopping list items from this text:
        "$text"
        For each item, identify name, quantity, unit, and estimated price if mentioned.
        Never invent missing prices. If price is missing, use null.
        Return ONLY a JSON array of items:
        [
          {
            "name": "Item name",
            "quantity": 1.0,
            "unit": "kg",
            "amount": 100.0,
            "category": "vegetables",
            "confidence": 0.95
          }
        ]
    """.trimIndent()
}
