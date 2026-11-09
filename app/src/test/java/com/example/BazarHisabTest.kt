package com.example

import com.example.ai.BengaliNumberUtils
import com.example.ai.LocalRuleBasedExtractor
import com.example.data.model.ExpenseCategories
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertEquals("৳ 1,250.00", formattedEn)

        val formattedBn = BengaliNumberUtils.formatCurrency(1250.0, isBengali = true)
        assertEquals("৳ ১,২৫০.০০", formattedBn)
    }

    @Test
    fun testCurrencyPreservesPaisa() {
        // Paisa must never be silently dropped from displayed amounts.
        assertEquals("৳ 1,250.50", BengaliNumberUtils.formatCurrency(1250.5, isBengali = false))
        assertEquals("৳ ১,২৫০.৫০", BengaliNumberUtils.formatCurrency(1250.5, isBengali = true))
        // Lakh/crore (Indian-style) grouping with decimals.
        assertEquals("৳ 1,00,000.25", BengaliNumberUtils.formatCurrency(100000.25, isBengali = false))
        assertEquals("৳ 1,00,00,000.00", BengaliNumberUtils.formatCurrency(10000000.0, isBengali = false))
        assertEquals("৳ 999.00", BengaliNumberUtils.formatCurrency(999.0, isBengali = false))
        assertEquals("৳ 0.00", BengaliNumberUtils.formatCurrency(0.0, isBengali = false))
        assertEquals("৳ -5.50", BengaliNumberUtils.formatCurrency(-5.5, isBengali = false))
        // HALF_UP rounding on the paisa boundary.
        assertEquals("৳ 10.13", BengaliNumberUtils.formatCurrency(10.125, isBengali = false))
        assertEquals("৳ 10.12", BengaliNumberUtils.formatCurrency(10.124, isBengali = false))
    }

    @Test
    fun testParseAmountStrict() {
        assertEquals(1250.0, BengaliNumberUtils.parseAmountStrict("1,250")!!, 0.0)
        assertEquals(1250.5, BengaliNumberUtils.parseAmountStrict("৳ ১,২৫০.৫০")!!, 0.0)
        assertEquals(100.0, BengaliNumberUtils.parseAmountStrict("100 টাকা")!!, 0.0)
        // Signs preserved so the >0 gate can reject negatives.
        assertEquals(-5.0, BengaliNumberUtils.parseAmountStrict("-5")!!, 0.0)
        // Trailing garbage rejected instead of truncated.
        assertNull(BengaliNumberUtils.parseAmountStrict("12ab"))
        assertNull(BengaliNumberUtils.parseAmountStrict("1-2"))
        assertNull(BengaliNumberUtils.parseAmountStrict("abc"))
        assertNull(BengaliNumberUtils.parseAmountStrict(""))
        assertNull(BengaliNumberUtils.parseAmountStrict(null))
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
    fun testBengaliNumberWords() {
        // Thousands + hundreds + tens/ones compose; larger scales first.
        assertEquals(
            1250.0,
            LocalRuleBasedExtractor.parseBengaliNumberWords("এক হাজার দুইশ পঞ্চাশ") ?: 0.0,
            0.0
        )
        assertEquals(
            1200.0,
            LocalRuleBasedExtractor.parseBengaliNumberWords("আজ বাজারে এক হাজার দুইশ টাকা খরচ হয়েছে") ?: 0.0,
            0.0
        )
        // General thousand multipliers and fractional forms.
        assertEquals(10000.0, LocalRuleBasedExtractor.parseBengaliNumberWords("দশ হাজার") ?: 0.0, 0.0)
        assertEquals(1500.0, LocalRuleBasedExtractor.parseBengaliNumberWords("দেড় হাজার") ?: 0.0, 0.0)
        assertEquals(2500.0, LocalRuleBasedExtractor.parseBengaliNumberWords("আড়াই হাজার") ?: 0.0, 0.0)
        // Hundreds incl. fractional forms.
        assertEquals(700.0, LocalRuleBasedExtractor.parseBengaliNumberWords("সাতশ টাকা") ?: 0.0, 0.0)
        assertEquals(150.0, LocalRuleBasedExtractor.parseBengaliNumberWords("দেড়শ") ?: 0.0, 0.0)
        // Tens/ones, incl. words that share letters with scale words.
        assertEquals(20.0, LocalRuleBasedExtractor.parseBengaliNumberWords("বিশ টাকা") ?: 0.0, 0.0)
        assertEquals(30.0, LocalRuleBasedExtractor.parseBengaliNumberWords("ত্রিশ") ?: 0.0, 0.0)
        assertEquals(10.0, LocalRuleBasedExtractor.parseBengaliNumberWords("দশ টাকা") ?: 0.0, 0.0)
        // Lakh support.
        assertEquals(200000.0, LocalRuleBasedExtractor.parseBengaliNumberWords("দুই লাখ") ?: 0.0, 0.0)
        // No number words, no digits: null (never a fabricated zero).
        assertNull(LocalRuleBasedExtractor.parseBengaliNumberWords("আজ বাজারে খরচ হয়েছে"))
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
