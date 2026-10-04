package com.example.ai

import android.graphics.Bitmap

interface ExpenseAiService {
    suspend fun extractFromReceipt(bitmap: Bitmap): Result<ExtractedExpense>
    suspend fun extractFromVoiceText(text: String): Result<ExtractedExpense>
    suspend fun parseShoppingList(text: String): Result<List<ExtractedItem>>
}
