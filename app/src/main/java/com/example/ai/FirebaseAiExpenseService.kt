package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GenerativeModel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Thrown when no Firebase project is configured in the build
 * (no `google-services.json` — see `app/FIREBASE_SETUP.md`). AI features are
 * unavailable in that state; the app must keep working offline.
 */
class FirebaseAiNotConfiguredException : Exception(
    "Firebase is not configured in this build (no google-services.json). " +
        "See app/FIREBASE_SETUP.md. AI features are unavailable; " +
        "on-device parsing is used where possible."
)

/**
 * The remote Gemini call, separated from [FirebaseAiExpenseService] so tests
 * can inject a fake without a Firebase project or network.
 */
interface FirebaseAiRemote {
    suspend fun generateText(prompt: String): String
    suspend fun generateFromImage(bitmap: Bitmap, prompt: String): String
}

/** [FirebaseAiRemote] backed by a real Firebase AI Logic [GenerativeModel]. */
internal class FirebaseAiRemoteClient(private val model: GenerativeModel) : FirebaseAiRemote {

    override suspend fun generateText(prompt: String): String {
        return model.generateContent(prompt).text
            ?: throw IllegalStateException("Gemini returned an empty response.")
    }

    override suspend fun generateFromImage(bitmap: Bitmap, prompt: String): String {
        val response = model.generateContent(
            content {
                image(bitmap)
                text(prompt)
            },
        )
        return response.text
            ?: throw IllegalStateException("Gemini returned an empty response.")
    }
}

/**
 * [ExpenseAiService] backed by Firebase AI Logic (Gemini 2.5 Flash) with
 * App Check attestation. No API key is embedded in the app, in BuildConfig,
 * or in any request URL — the Firebase SDK authenticates the app itself.
 *
 * Behaviour contract (never fabricate data):
 * - Receipt: fast on-device ML Kit OCR is only a pre-pass (Latin script only,
 *   so Bengali receipts normally yield nothing here). The receipt image is
 *   then sent to Gemini Vision, which can read Bengali. If Gemini fails or
 *   returns no items and no total, this returns [Result.failure] with a
 *   user-facing message — never a sample / invented expense.
 * - Voice / shopping list: if Gemini fails or returns nothing, the local
 *   rule-based parser runs as an *explicitly labelled* offline fallback
 *   ([ExtractedExpense.aiOfflineFallback] / [ExtractedItem.aiOfflineFallback]);
 *   the ViewModel surfaces that label instead of pretending AI answered.
 *
 * This class deliberately does NOT touch [GeminiExpenseAiService]: that file
 * is being fixed on branch `fix/p0-entry-and-save-integrity` (fake-receipt
 * removal). Once both branches land, `GeminiExpenseAiService` and the
 * `.env` / `GEMINI_API_KEY` plumbing should be deleted.
 */
class FirebaseAiExpenseService(
    private val context: Context,
    private val remote: FirebaseAiRemote? = null,
) : ExpenseAiService {

    private val realRemote: FirebaseAiRemote by lazy { FirebaseAiRemoteClient(createModel()) }

    private fun activeRemote(): FirebaseAiRemote = remote ?: realRemote

    override suspend fun extractFromReceipt(bitmap: Bitmap): Result<ExtractedExpense> {
        // Step 1: fast on-device OCR pre-pass (Latin only — Bengali receipts
        // normally produce no usable text here, which is expected).
        // Note: ReceiptOcrScanner.recognizeText returns Result<String>
        // (P0 change); a failure simply skips the pre-pass. The pre-pass is
        // time-boxed so a stuck recognizer can never block the Gemini path.
        val ocrText = withTimeoutOrNull(OCR_TIMEOUT_MS) {
            runCatching { ReceiptOcrScanner.recognizeText(bitmap) }.getOrNull()?.getOrNull()
        }?.takeIf { it.isNotBlank() }
        ocrText?.let {
            val parsed = LocalRuleBasedExtractor.parseReceiptText(it)
            if (parsed.items.any { item -> item.amount != null }) {
                Log.d(TAG, "Receipt parsed from on-device OCR: ${parsed.items.size} items")
                return Result.success(parsed)
            }
        }

        // Step 2: Firebase AI Logic vision — the only path that reads Bengali receipts.
        return try {
            val json = activeRemote().generateFromImage(
                resizeBitmap(bitmap, MAX_IMAGE_DIMENSION),
                FirebaseAiPrompts.receiptPrompt(),
            )
            val extracted = FirebaseAiResponseParser.parseExpense(json)
            if (extracted.items.isEmpty() && extracted.total == null) {
                Result.failure(
                    IllegalStateException("Gemini could not read any items from this receipt."),
                )
            } else {
                Result.success(extracted)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Receipt AI failed: ${FirebaseAiErrors.userMessage(e)}", e)
            Result.failure(FirebaseAiErrors.toException(e))
        }
    }

    override suspend fun extractFromVoiceText(text: String): Result<ExtractedExpense> {
        return try {
            val json = activeRemote().generateText(FirebaseAiPrompts.voicePrompt(text))
            val extracted = FirebaseAiResponseParser.parseExpense(json)
            if (extracted.items.isEmpty()) {
                Log.w(TAG, "Gemini returned no items for voice text; using labelled on-device fallback")
                Result.success(localVoiceFallback(text))
            } else {
                Result.success(extracted)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Voice AI failed, using labelled on-device fallback: ${e.message}", e)
            Result.success(localVoiceFallback(text))
        }
    }

    override suspend fun parseShoppingList(text: String): Result<List<ExtractedItem>> {
        return try {
            val json = activeRemote().generateText(FirebaseAiPrompts.shoppingListPrompt(text))
            val items = FirebaseAiResponseParser.parseItems(json)
            if (items.isEmpty()) {
                Log.w(TAG, "Gemini returned no items for shopping list; using labelled on-device fallback")
                Result.success(localShoppingFallback(text))
            } else {
                Result.success(items)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Shopping-list AI failed, using labelled on-device fallback: ${e.message}", e)
            Result.success(localShoppingFallback(text))
        }
    }

    private fun localVoiceFallback(text: String): ExtractedExpense =
        LocalRuleBasedExtractor.parseVoice(text).copy(aiOfflineFallback = true)

    private fun localShoppingFallback(text: String): List<ExtractedItem> =
        LocalRuleBasedExtractor.parseShoppingList(text)
            .map { it.copy(aiOfflineFallback = true) }

    /**
     * Creates the Gemini model through Firebase AI Logic. Throws
     * [FirebaseAiNotConfiguredException] when the build has no Firebase
     * configuration, so callers can show "AI isn't set up yet" instead of
     * crashing or silently degrading.
     */
    private fun createModel(): GenerativeModel {
        val apps = runCatching { FirebaseApp.getApps(context) }.getOrNull().orEmpty()
        if (apps.isEmpty()) throw FirebaseAiNotConfiguredException()
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    temperature = 0.1f
                },
            )
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

    companion object {
        private const val TAG = "FirebaseAi"
        const val MODEL_NAME = "gemini-2.5-flash"
        private const val MAX_IMAGE_DIMENSION = 1024
        private const val OCR_TIMEOUT_MS = 10_000L
    }
}
