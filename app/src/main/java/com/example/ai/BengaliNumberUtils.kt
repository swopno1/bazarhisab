package com.example.ai

import java.math.BigDecimal
import java.math.RoundingMode

object BengaliNumberUtils {
    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    private val englishDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    /**
     * Converts any string containing Bengali digits into standard English digits (0-9).
     */
    fun toEnglishDigits(input: String): String {
        var result = input
        for (i in bengaliDigits.indices) {
            result = result.replace(bengaliDigits[i], englishDigits[i])
        }
        return result
    }

    /**
     * Converts English digits into Bengali digits.
     */
    fun toBengaliDigits(input: String): String {
        var result = input
        for (i in englishDigits.indices) {
            result = result.replace(englishDigits[i], bengaliDigits[i])
        }
        return result
    }

    /**
     * Lakh/crore (Indian-style) digit grouping: the last 3 digits form one
     * group, then groups of 2 ("100000" -> "1,00,000"). Done manually
     * because DecimalFormat secondary grouping differs between JVM and
     * Android runtimes.
     */
    internal fun groupIndianDigits(digits: String): String {
        if (digits.length <= 3) return digits
        val groups = mutableListOf(digits.takeLast(3))
        var rest = digits.dropLast(3)
        while (rest.length > 2) {
            groups.add(0, rest.takeLast(2))
            rest = rest.dropLast(2)
        }
        if (rest.isNotEmpty()) groups.add(0, rest)
        return groups.joinToString(",")
    }

    /**
     * Format a double amount as BDT (৳) string with paisa precision.
     * If isBengali is true, output digits in Bengali.
     *
     * Always shows two decimals with HALF_UP rounding (standard money
     * behavior), using lakh/crore grouping. Paisa must never be silently
     * dropped: 1250.5 displays as ৳ 1,250.50, not ৳ 1,250.
     */
    fun formatCurrency(amount: Double, isBengali: Boolean = false): String {
        if (!amount.isFinite()) return "৳ —"
        val rounded = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
        val plain = rounded.toPlainString()
        val negative = plain.startsWith("-")
        val core = if (negative) plain.substring(1) else plain
        val dot = core.indexOf('.')
        val grouped = groupIndianDigits(core.substring(0, dot)) + core.substring(dot)
        val signed = if (negative) "-$grouped" else grouped
        return if (isBengali) {
            "৳ " + toBengaliDigits(signed)
        } else {
            "৳ $signed"
        }
    }

    /**
     * Extract the first amount from free text that may contain ৳, Tk, Taka,
     * commas, or Bengali digits. Lenient by design: used for spoken and
     * scanned sentences where the number sits inside other words.
     */
    fun parseAmount(input: String?): Double? {
        if (input.isNullOrBlank()) return null
        val cleaned = toEnglishDigits(input)
            .replace("৳", "")
            .replace("Tk", "", ignoreCase = true)
            .replace("Taka", "", ignoreCase = true)
            .replace("টাকা", "")
            .replace(",", "")
            .trim()
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(cleaned) ?: return null
        return match.value.toDoubleOrNull()
    }

    /**
     * Strict amount parsing for typed input fields (manual entry, item
     * editor, budget). The whole cleaned input must be one number: signs
     * are preserved ("-5" parses as -5 so the >0 gate rejects it) and
     * trailing garbage ("12ab", "1-2") is rejected instead of silently
     * truncated to the first digits.
     */
    fun parseAmountStrict(input: String?): Double? {
        if (input.isNullOrBlank()) return null
        val cleaned = toEnglishDigits(input)
            .replace("৳", "")
            .replace("Tk", "", ignoreCase = true)
            .replace("Taka", "", ignoreCase = true)
            .replace("টাকা", "")
            .replace(",", "")
            .trim()
        if (!Regex("""^[+-]?\d+(\.\d+)?$""").matches(cleaned)) return null
        return cleaned.toDoubleOrNull()
    }
}
