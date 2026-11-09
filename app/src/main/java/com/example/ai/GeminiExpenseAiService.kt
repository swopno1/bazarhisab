package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class GeminiExpenseAiService : ExpenseAiService {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val primaryModel = "gemini-3.8-flash"

    private val generativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = primaryModel,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    override suspend fun extractFromReceipt(bitmap: Bitmap): Result<ExtractedExpense> = withContext(Dispatchers.IO) {
        // Step 1: Run fast on-device ML Kit OCR directly on the image
        val ocrResult = ReceiptOcrScanner.recognizeText(bitmap)
        val ocrError = ocrResult.exceptionOrNull()
        val ocrText = ocrResult.getOrDefault("")

        if (ocrText.isNotBlank()) {
            val parsedFromOcr = LocalRuleBasedExtractor.parseReceiptText(ocrText)
            if (parsedFromOcr.items.isNotEmpty() && parsedFromOcr.items.any { it.amount != null || it.quantity != null }) {
                Log.d("GeminiExpenseAi", "Extracted ${parsedFromOcr.items.size} items from receipt using on-device OCR")
                return@withContext Result.success(parsedFromOcr)
            }
        }

        // Step 2: Fall back to Firebase AI Logic Gemini vision model
        try {
            val resized = resizeBitmap(bitmap, 1024)

            val prompt = """
                You are an expert Bangladeshi household expense extraction engine.
                Analyze this receipt image (written in Bengali, English, Banglish, or mixed).
                Extract items, quantities, units, prices, and totals.
                Default currency is BDT.
                Categories must be one of: [groceries, vegetables, fish, meat, eggs, rice_grains, oil_spices, fruits, household, food, medicine, transport, bills, other].
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

            val response = withTimeout(60_000L) {
                generativeModel.generateContent(
                    content {
                        image(resized)
                        text(prompt)
                    }
                )
            }

            val text = response.text ?: ""
            val extracted = parseExtractedJson(text)
            Result.success(extracted)
        } catch (e: Exception) {
            Log.e("GeminiExpenseAi", "Firebase AI Logic receipt extraction failed: ${e.message}", e)
            if (ocrText.isNotBlank()) {
                val parsed = LocalRuleBasedExtractor.parseReceiptText(ocrText)
                if (parsed.items.any { it.amount != null } || (parsed.total ?: 0.0) > 0.0) {
                    return@withContext Result.success(parsed)
                }
            }
            val reason = ocrError?.message ?: e.localizedMessage
                ?: "No readable text or prices were found on this receipt."
            Result.failure(
                Exception("Could not read this receipt. $reason Please type the items manually.")
            )
        }
    }

    override suspend fun extractFromVoiceText(text: String): Result<ExtractedExpense> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are an expense extraction assistant for Bangladeshi households.
                The user spoke this expense: "$text".
                Extract items, quantities, units, and amounts.
                Default currency is BDT.
                Categories must be one of: [groceries, vegetables, fish, meat, eggs, rice_grains, oil_spices, fruits, household, food, medicine, transport, bills, other].
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

            val response = withTimeout(30_000L) {
                generativeModel.generateContent(prompt)
            }

            val jsonText = response.text ?: ""
            val extracted = parseExtractedJson(jsonText)
            Result.success(extracted)
        } catch (e: Exception) {
            // Graceful fallback to local rule-based extractor
            Log.w("GeminiExpenseAi", "Voice extraction fallback to rule-based: ${e.message}")
            Result.success(LocalRuleBasedExtractor.parseVoice(text))
        }
    }

    override suspend fun parseShoppingList(text: String): Result<List<ExtractedItem>> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Extract grocery shopping list items from this text:
                "$text"
                For each item, identify name, quantity, unit, and estimated price if mentioned.
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

            val response = withTimeout(30_000L) {
                generativeModel.generateContent(prompt)
            }

            val jsonText = response.text ?: ""
            val adapter = moshi.adapter(Array<ExtractedItem>::class.java)
            val items = adapter.fromJson(jsonText)?.toList() ?: LocalRuleBasedExtractor.parseShoppingList(text)
            Result.success(items)
        } catch (e: Exception) {
            Log.w("GeminiExpenseAi", "Shopping list parsing fallback: ${e.message}")
            Result.success(LocalRuleBasedExtractor.parseShoppingList(text))
        }
    }

    private fun parseExtractedJson(jsonText: String): ExtractedExpense {
        val clean = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val adapter = moshi.adapter(ExtractedExpense::class.java)
        return adapter.fromJson(clean) ?: ExtractedExpense()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
