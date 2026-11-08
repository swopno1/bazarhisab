package com.example

import com.example.ai.BengaliNumberUtils
import com.example.ai.LocalRuleBasedExtractor
import com.example.data.model.ExpenseCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BazarHisabTest {

    @Test
    fun testBengaliDigitConversion() {
        val bengali = "১২৩৪৫৬৭৮৯০"
        val converted = BengaliNumberUtils.toEnglishDigits(bengali)
        assertEquals("1234567890", converted)

        val eng = "1300"
        val backToBn = BengaliNumberUtils.toBengaliDigits(eng)
        assertEquals("১৩০০", backToBn)
    }

    @Test
    fun testCurrencyFormatting() {
        val formattedEn = BengaliNumberUtils.formatCurrency(1250.0, isBengali = false)
        assertEquals("৳ 1,250", formattedEn)

        val formattedBn = BengaliNumberUtils.formatCurrency(1250.0, isBengali = true)
        assertEquals("৳ ১,২৫০", formattedBn)
    }

    @Test
    fun testCategoryMatching() {
        assertEquals("rice_grains", ExpenseCategories.matchCategoryByItemName("মিনিকেট চাল"))
        assertEquals("fish", ExpenseCategories.matchCategoryByItemName("রুই মাছ"))
        assertEquals("meat", ExpenseCategories.matchCategoryByItemName("গরুর মাংস"))
        assertEquals("vegetables", ExpenseCategories.matchCategoryByItemName("গোল আলু"))
        assertEquals("oil_spices", ExpenseCategories.matchCategoryByItemName("সয়াবিন তেল"))
        assertEquals("eggs", ExpenseCategories.matchCategoryByItemName("ফার্মের ডিম"))
    }

    @Test
    fun testVoiceExtractionMultiItem() {
        val speech = "আজ আলু ৮০ টাকা, পেঁয়াজ ১০০ টাকা, ডিম ১৪০ টাকা"
        val result = LocalRuleBasedExtractor.parseVoice(speech)

        assertEquals(3, result.items.size)
        assertEquals(320.0, result.total ?: 0.0, 0.01)

        val potato = result.items.find { it.name.contains("আলু") }
        assertNotNull(potato)
        assertEquals(80.0, potato?.amount ?: 0.0, 0.01)
    }

    @Test
    fun testVoiceExtractionSingleTotal() {
        val speech = "আজ বাজারে এক হাজার দুইশ টাকা খরচ হয়েছে"
        val result = LocalRuleBasedExtractor.parseVoice(speech)

        assertEquals(1200.0, result.total ?: 0.0, 0.01)
    }

    @Test
    fun testShoppingListParsing() {
        val listText = """
            চাল ৫ কেজি ৪৫০
            আলু ২ কেজি ১০০
            ডিম ১২টা ১৫০
            সয়াবিন তেল ২ লিটার ৩৬০
        """.trimIndent()

        val items = LocalRuleBasedExtractor.parseShoppingList(listText)
        assertEquals(4, items.size)

        val rice = items[0]
        assertTrue(rice.name.contains("চাল"))
        assertEquals(5.0, rice.quantity ?: 0.0, 0.01)
        assertEquals(450.0, rice.amount ?: 0.0, 0.01)

        val egg = items[2]
        assertTrue(egg.name.contains("ডিম"))
        assertEquals(12.0, egg.quantity ?: 0.0, 0.01)
        assertEquals(150.0, egg.amount ?: 0.0, 0.01)
    }

    @Test
    fun testVoiceExtractionWithQuantity() {
        // Regression: quantity must not be read as the price.
        // "চাল ৫ কেজি ৪৫০ টাকা" used to save as ৳5; it is ৳450, qty 5.
        val speech = "চাল ৫ কেজি ৪৫০ টাকা, ডিম ১২টা ১৫০ টাকা, আলু ২ কেজি ৮০ টাকা"
        val result = LocalRuleBasedExtractor.parseVoice(speech)

        assertEquals(3, result.items.size)
        assertEquals(680.0, result.total ?: 0.0, 0.01)

        val rice = result.items.find { it.name.contains("চাল") }
        assertNotNull(rice)
        assertEquals(450.0, rice?.amount ?: 0.0, 0.01)
        assertEquals(5.0, rice?.quantity ?: 0.0, 0.01)

        val egg = result.items.find { it.name.contains("ডিম") }
        assertNotNull(egg)
        assertEquals(150.0, egg?.amount ?: 0.0, 0.01)
        assertEquals(12.0, egg?.quantity ?: 0.0, 0.01)

        val potato = result.items.find { it.name.contains("আলু") }
        assertNotNull(potato)
        assertEquals(80.0, potato?.amount ?: 0.0, 0.01)
        assertEquals(2.0, potato?.quantity ?: 0.0, 0.01)
    }

    @Test
    fun testCommaAmounts() {
        // Regression: "১,১৬০" used to parse as 1 (receipt total) / 160 (item).
        val receipt = """
            স্বপ্ন সুপার শপ
            চাল ৫ কেজি ১,১৬০ টাকা
            মোট: ১,১৬০ টাকা
        """.trimIndent()
        val extracted = LocalRuleBasedExtractor.parseReceiptText(receipt)
        assertEquals(1160.0, extracted.total ?: 0.0, 0.01)
        val rice = extracted.items.find { it.name.contains("চাল") }
        assertNotNull(rice)
        assertEquals(1160.0, rice?.amount ?: 0.0, 0.01)

        val shopping = LocalRuleBasedExtractor.parseShoppingList("চাল ৫ কেজি ১,১৬০")
        assertEquals(1, shopping.size)
        assertEquals(1160.0, shopping[0].amount ?: 0.0, 0.01)
        assertEquals(5.0, shopping[0].quantity ?: 0.0, 0.01)

        val voice = LocalRuleBasedExtractor.parseVoice("চাল ৫ কেজি ১,২৫০ টাকা")
        assertEquals(1250.0, voice.total ?: 0.0, 0.01)
        assertEquals(5.0, voice.items.firstOrNull()?.quantity ?: 0.0, 0.01)
    }

    @Test
    fun testReceiptTextParsing() {
        val receipt = """
            স্বপ্ন সুপার শপ
            চাল ৫ কেজি ৪৫০ টাকা
            সয়াবিন তেল ২ লিটার ৩৮০ টাকা
            মোট: ৮৩০ টাকা
        """.trimIndent()

        val extracted = LocalRuleBasedExtractor.parseReceiptText(receipt)
        assertNotNull(extracted)
        assertTrue(extracted.items.isNotEmpty())
        assertEquals(830.0, extracted.total ?: 0.0, 0.01)
    }
}
