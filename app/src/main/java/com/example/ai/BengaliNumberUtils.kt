package com.example.ai

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

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
     * Format a double amount as BDT (৳) string.
     * If isBengali is true, output digits in Bengali.
     */
    fun formatCurrency(amount: Double, isBengali: Boolean = false): String {
        val formatter = DecimalFormat("#,##,##0")
        val formattedNumber = formatter.format(amount)
        return if (isBengali) {
            "৳ " + toBengaliDigits(formattedNumber)
        } else {
            "৳ $formattedNumber"
        }
    }

    /**
     * Safely parse amount from text that may contain ৳, Tk, Taka, commas, or Bengali digits.
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
}
