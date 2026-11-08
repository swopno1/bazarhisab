package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.UnknownHostException

/**
 * Tests for the Firebase AI Logic integration that need no Firebase project
 * and no network: prompt construction, JSON parsing, error mapping, and the
 * labelled on-device fallback (via a fake [FirebaseAiRemote]).
 *
 * Not covered here (needs a real Firebase project + device): the live
 * Gemini call, App Check attestation, and the google-services.json build
 * check — see the verification checklist in `app/FIREBASE_SETUP.md`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirebaseAiLogicTest {

    // --- Prompts ---

    @Test
    fun receiptPrompt_forbidsInventionAndRequiresBdtJson() {
        val prompt = FirebaseAiPrompts.receiptPrompt()
        assertTrue(prompt.contains("Never invent"))
        assertTrue(prompt.contains("BDT"))
        assertTrue(prompt.contains("\"merchant\""))
        assertTrue(prompt.contains("\"items\""))
        assertTrue(prompt.contains("rice_grains"))
    }

    @Test
    fun voicePrompt_embedsSpokenTextAndSchema() {
        val prompt = FirebaseAiPrompts.voicePrompt("চাল ৫ কেজি ৪৫০ টাকা")
        assertTrue(prompt.contains("চাল ৫ কেজি ৪৫০ টাকা"))
        assertTrue(prompt.contains("\"total\""))
        assertTrue(prompt.contains("Never invent"))
    }

    @Test
    fun shoppingListPrompt_returnsJsonArraySchema() {
        val prompt = FirebaseAiPrompts.shoppingListPrompt("চাল ৫ কেজি")
        assertTrue(prompt.contains("চাল ৫ কেজি"))
        assertTrue(prompt.contains("\"name\""))
    }

    // --- JSON parsing ---

    @Test
    fun parseExpense_validJson() {
        val json = """
            {
              "merchant": "স্বপ্ন",
              "total": 1160.0,
              "items": [
                {"name": "চাল", "quantity": 5.0, "unit": "kg", "amount": 450.0, "category": "rice_grains", "confidence": 0.95}
              ],
              "category": "groceries",
              "confidence": 0.9
            }
        """.trimIndent()
        val parsed = FirebaseAiResponseParser.parseExpense(json)
        assertEquals("স্বপ্ন", parsed.merchant)
        assertEquals(1160.0, parsed.total ?: 0.0, 0.01)
        assertEquals(1, parsed.items.size)
        assertEquals(450.0, parsed.items[0].amount ?: 0.0, 0.01)
        assertFalse(parsed.aiOfflineFallback)
    }

    @Test
    fun parseExpense_stripsMarkdownFences() {
        val json = "```json\n{\"merchant\": null, \"total\": 80.0, " +
            "\"items\": [{\"name\": \"আলু\", \"amount\": 80.0}]}\n```"
        val parsed = FirebaseAiResponseParser.parseExpense(json)
        assertEquals(80.0, parsed.total ?: 0.0, 0.01)
        assertEquals("আলু", parsed.items[0].name)
    }

    @Test
    fun parseItems_validJsonArray() {
        val json = "[{\"name\": \"ডিম\", \"quantity\": 12.0, \"unit\": \"pcs\", \"amount\": 150.0}]"
        val items = FirebaseAiResponseParser.parseItems(json)
        assertEquals(1, items.size)
        assertEquals(150.0, items[0].amount ?: 0.0, 0.01)
    }

    @Test
    fun parseExpense_throwsOnGarbage() {
        var threw = false
        try {
            FirebaseAiResponseParser.parseExpense("not json at all {{{")
        } catch (e: Exception) {
            threw = true
        }
        assertTrue(threw)
    }

    // --- Error mapping ---

    @Test
    fun classify_quota429() {
        assertEquals(
            AiFailureKind.QUOTA,
            FirebaseAiErrors.classify(RuntimeException("429 RESOURCE_EXHAUSTED: quota exceeded")),
        )
    }

    @Test
    fun classify_appCheck403() {
        assertEquals(
            AiFailureKind.APP_CHECK,
            FirebaseAiErrors.classify(RuntimeException("403 PERMISSION_DENIED: App Check failed")),
        )
    }

    @Test
    fun classify_network() {
        assertEquals(
            AiFailureKind.NETWORK,
            FirebaseAiErrors.classify(UnknownHostException("Unable to resolve host")),
        )
    }

    @Test
    fun classify_notConfigured() {
        assertEquals(
            AiFailureKind.NOT_CONFIGURED,
            FirebaseAiErrors.classify(FirebaseAiNotConfiguredException()),
        )
    }

    @Test
    fun userMessage_isHumanReadableForEveryKind() {
        for (kind in AiFailureKind.entries) {
            val error = when (kind) {
                AiFailureKind.NOT_CONFIGURED -> FirebaseAiNotConfiguredException()
                AiFailureKind.APP_CHECK -> RuntimeException("App Check failed")
                AiFailureKind.QUOTA -> RuntimeException("429 quota")
                AiFailureKind.NETWORK -> UnknownHostException("host")
                AiFailureKind.EMPTY_RESPONSE -> IllegalStateException("Gemini returned an empty response.")
                AiFailureKind.UNKNOWN -> RuntimeException("something odd")
            }
            val message = FirebaseAiErrors.userMessage(error)
            assertTrue("empty message for $kind", message.isNotBlank())
            assertFalse("message leaks raw status for $kind", message.contains("429"))
        }
    }

    // --- Service fallback behaviour (fake remote, no Firebase) ---

    private class ThrowingRemote(private val message: String) : FirebaseAiRemote {
        override suspend fun generateText(prompt: String): String = throw RuntimeException(message)
        override suspend fun generateFromImage(bitmap: Bitmap, prompt: String): String =
            throw RuntimeException(message)
    }

    private class JsonRemote(private val json: String) : FirebaseAiRemote {
        override suspend fun generateText(prompt: String): String = json
        override suspend fun generateFromImage(bitmap: Bitmap, prompt: String): String = json
    }

    private fun serviceWith(remote: FirebaseAiRemote): FirebaseAiExpenseService {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return FirebaseAiExpenseService(context, remote)
    }

    @Test
    @Config(sdk = [34])
    fun voiceFallsBackToLabelledLocalParseWhenAiFails() = runBlocking {
        val service = serviceWith(ThrowingRemote("429 RESOURCE_EXHAUSTED"))
        val result = service.extractFromVoiceText("চাল ৫ কেজি ৪৫০ টাকা")

        assertTrue(result.isSuccess)
        val extracted = result.getOrThrow()
        assertTrue(extracted.aiOfflineFallback)
        val rice = extracted.items.find { it.name.contains("চাল") }
        assertEquals(450.0, rice?.amount ?: 0.0, 0.01)
    }

    @Test
    @Config(sdk = [34])
    fun voiceUsesGeminiResultWhenAiSucceeds() = runBlocking {
        val json = """{"total": 80.0, "items": [{"name": "আলু", "amount": 80.0, "category": "vegetables"}]}"""
        val service = serviceWith(JsonRemote(json))
        val result = service.extractFromVoiceText("আলু ৮০ টাকা")

        assertTrue(result.isSuccess)
        val extracted = result.getOrThrow()
        assertFalse(extracted.aiOfflineFallback)
        assertEquals(80.0, extracted.total ?: 0.0, 0.01)
    }

    @Test
    @Config(sdk = [34])
    fun receiptFailsWithUserMessageWhenAiFails() = runBlocking {
        val service = serviceWith(ThrowingRemote("429 RESOURCE_EXHAUSTED: quota exceeded"))
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val result = service.extractFromReceipt(bitmap)

        assertTrue(result.isFailure)
        val message = result.exceptionOrNull()?.message.orEmpty()
        assertTrue(message.contains("usage limit"))
        assertFalse(message.contains("429"))
    }

    @Test
    @Config(sdk = [34])
    fun shoppingListFallsBackToLabelledLocalParseWhenAiFails() = runBlocking {
        val service = serviceWith(ThrowingRemote("network down"))
        val result = service.parseShoppingList("চাল ৫ কেজি ৪৫০")

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.aiOfflineFallback })
    }
}
