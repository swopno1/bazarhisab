package com.example.ai

import com.example.data.model.ExpenseCategories
import java.util.Locale

object LocalRuleBasedExtractor {

    fun parseBengaliNumberWords(text: String): Double? {
        val lower = text.lowercase()
        var total = 0.0

        val thousands = when {
            lower.contains("এক হাজার") || lower.contains("১ হাজার") -> 1000.0
            lower.contains("দুই হাজার") || lower.contains("২ হাজার") -> 2000.0
            lower.contains("তিন হাজার") || lower.contains("৩ হাজার") -> 3000.0
            lower.contains("চার হাজার") || lower.contains("৪ হাজার") -> 4000.0
            lower.contains("পাঁচ হাজার") || lower.contains("৫ হাজার") -> 5000.0
            lower.contains("দেড় হাজার") || lower.contains("দেড় হাজার") -> 1500.0
            lower.contains("আড়াই হাজার") || lower.contains("আড়াই হাজার") -> 2500.0
            else -> 0.0
        }
        total += thousands

        val hundreds = when {
            lower.contains("নয়শত") || lower.contains("নয়শ") || lower.contains("৯০০") -> 900.0
            lower.contains("আটশত") || lower.contains("আটশ") || lower.contains("৮০০") -> 800.0
            lower.contains("সাতশত") || lower.contains("সাতশ") || lower.contains("৭০০") -> 700.0
            lower.contains("ছয়শত") || lower.contains("ছয়শ") || lower.contains("৬০০") -> 600.0
            lower.contains("পাঁচশত") || lower.contains("পাঁচশ") || lower.contains("৫০০") -> 500.0
            lower.contains("চারশত") || lower.contains("চারশ") || lower.contains("৪০০") -> 400.0
            lower.contains("তিনশত") || lower.contains("তিনশ") || lower.contains("৩০০") -> 300.0
            lower.contains("দুইশত") || lower.contains("দুইশ") || lower.contains("২০০") -> 200.0
            lower.contains("একশত") || lower.contains("একশ") || lower.contains("১০০") -> 100.0
            else -> 0.0
        }
        total += hundreds

        return if (total > 0.0) total else null
    }

    /**
     * Parse natural language voice text into ExtractedExpense.
     */
    fun parseVoice(text: String): ExtractedExpense {
        val engDigitsText = BengaliNumberUtils.toEnglishDigits(text)
        val items = mutableListOf<ExtractedItem>()

        // Split by commas, 'আর', 'এবং', 'and', newline
        val parts = engDigitsText.split(Regex("[,।\n]+|\\s+আর\\s+|\\s+এবং\\s+|\\s+and\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        var merchant: String? = null
        if (text.contains("মুদি দোকান") || text.contains("দোকান")) {
            merchant = "মুদি দোকান"
        } else if (text.lowercase().contains("store") || text.lowercase().contains("shop")) {
            merchant = "Grocery Store"
        }

        for (part in parts) {
            // Find price in this segment
            val priceMatch = Regex("""(\d+(\.\d+)?)\s*(টাকা|টা|tk|taka)?""", RegexOption.IGNORE_CASE).find(part)
            if (priceMatch != null) {
                val amount = priceMatch.groupValues[1].toDoubleOrNull() ?: continue
                // Clean name
                var name = part.substring(0, priceMatch.range.first).trim()
                if (name.isBlank()) {
                    name = part.substring(priceMatch.range.last + 1).trim()
                }
                name = name.replace(Regex("""^(আজ|আজকে|কিনেছি|খরচ|হয়েছে|মোট|টাকা|today|bought|spent|for)\s*""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""\s*(কিনেছি|খরচ|হয়েছে|টাকা|bought|spent)$""", RegexOption.IGNORE_CASE), "")
                    .trim()

                if (name.isNotBlank()) {
                    // Extract quantity if present
                    val qtyMatch = Regex("""(\d+(\.\d+)?)\s*(কেজি|লিটার|টা|টি|গ্রাম|kg|g|l|pcs)""", RegexOption.IGNORE_CASE).find(name)
                    var qty: Double? = null
                    var unit: String? = null
                    if (qtyMatch != null) {
                        qty = qtyMatch.groupValues[1].toDoubleOrNull()
                        unit = qtyMatch.groupValues[3]
                        name = name.removeRange(qtyMatch.range).trim()
                    }

                    val cat = ExpenseCategories.matchCategoryByItemName(name)
                    items.add(
                        ExtractedItem(
                            name = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                            quantity = qty,
                            unit = unit,
                            amount = amount,
                            category = cat,
                            confidence = 0.88f
                        )
                    )
                }
            }
        }

        // If no individual items found, check if a single total amount was stated
        var totalAmount: Double? = null
        if (items.isEmpty()) {
            val amount = BengaliNumberUtils.parseAmount(text) ?: parseBengaliNumberWords(text)
            if (amount != null) {
                totalAmount = amount
                items.add(
                    ExtractedItem(
                        name = "দৈনিক বাজার / Groceries",
                        amount = amount,
                        category = "groceries",
                        confidence = 0.90f
                    )
                )
            }
        } else {
            totalAmount = items.sumOf { it.amount ?: 0.0 }
        }

        return ExtractedExpense(
            merchant = merchant,
            total = totalAmount,
            items = items,
            category = items.firstOrNull()?.category ?: "groceries",
            confidence = if (items.isNotEmpty()) 0.85f else 0.60f
        )
    }

    /**
     * Parse multi-line or bullet shopping list into individual items.
     */
    fun parseShoppingList(rawText: String): List<ExtractedItem> {
        val lines = rawText.split(Regex("[\r\n]+")).map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<ExtractedItem>()

        for (line in lines) {
            val cleanLine = line.replace(Regex("""^[-•*☐☑✓\d+\.]\s*"""), "").trim()
            if (cleanLine.isBlank()) continue

            val engDigits = BengaliNumberUtils.toEnglishDigits(cleanLine)

            // Pattern: [Item Name] [Optional Quantity] [Optional Price]
            // e.g. "চাল ৫ কেজি ৪৫০" or "Rice 5kg 450" or "ডিম ১২টা"
            val priceMatch = Regex("""\b(\d+(\.\d+)?)\s*(টাকা|tk|taka)?$""", RegexOption.IGNORE_CASE).find(engDigits)
            var price: Double? = null
            var remaining = engDigits

            if (priceMatch != null && priceMatch.range.first > 0) {
                price = priceMatch.groupValues[1].toDoubleOrNull()
                remaining = engDigits.substring(0, priceMatch.range.first).trim()
            }

            // Extract quantity: e.g. "5 kg", "২ কেজি", "12 pcs", "1 liter"
            val qtyMatch = Regex("""(\d+(\.\d+)?)\s*(কেজি|লিটার|গ্রাম|টা|টি|kg|g|l|liter|pcs|dozen)""", RegexOption.IGNORE_CASE).find(remaining)
            var qty: Double? = null
            var unit: String? = null
            var name = remaining

            if (qtyMatch != null) {
                qty = qtyMatch.groupValues[1].toDoubleOrNull()
                unit = qtyMatch.groupValues[3]
                name = remaining.removeRange(qtyMatch.range).trim()
            }

            if (name.isBlank()) {
                name = cleanLine
            }

            val category = ExpenseCategories.matchCategoryByItemName(name)
            results.add(
                ExtractedItem(
                    name = name.trim(),
                    quantity = qty,
                    unit = unit,
                    amount = price,
                    category = category,
                    confidence = if (price != null) 0.95f else 0.80f
                )
            )
        }

        return results
    }

    /**
     * Parses raw OCR text extracted from receipts or shopping memos.
     */
    fun parseReceiptText(rawText: String): ExtractedExpense {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return ExtractedExpense(
                merchant = "দোকানের রশিদ",
                items = listOf(ExtractedItem(name = "বাজারের সামগ্রী", amount = null)),
                confidence = 0.5f
            )
        }

        var detectedMerchant: String? = null
        var detectedTotal: Double? = null
        val extractedItems = mutableListOf<ExtractedItem>()

        // Look at top lines for potential store name
        for (i in 0 until minOf(3, lines.size)) {
            val line = lines[i]
            if (!line.contains(Regex("""\d{3,}""")) && line.length in 3..40) {
                detectedMerchant = line
                break
            }
        }

        for (line in lines) {
            val engLine = BengaliNumberUtils.toEnglishDigits(line)

            // Check if line indicates Total / Grand Total / মোট
            val totalMatch = Regex("""(total|subtotal|net|amount|grand total|মোট|সর্বমোট)[:\s]*([0-9]+(\.[0-9]+)?)""", RegexOption.IGNORE_CASE).find(engLine)
            if (totalMatch != null) {
                val candidateTotal = totalMatch.groupValues[2].toDoubleOrNull()
                if (candidateTotal != null && candidateTotal > (detectedTotal ?: 0.0)) {
                    detectedTotal = candidateTotal
                    continue
                }
            }

            // Parse item line
            val parsedList = parseShoppingList(line)
            for (item in parsedList) {
                if (item.amount != null || item.quantity != null) {
                    extractedItems.add(item)
                }
            }
        }

        val itemsToUse = if (extractedItems.isNotEmpty()) extractedItems else parseShoppingList(rawText)
        val finalTotal = detectedTotal ?: itemsToUse.sumOf { it.amount ?: 0.0 }.takeIf { it > 0 }

        return ExtractedExpense(
            merchant = detectedMerchant ?: "বাজারের রশিদ",
            total = finalTotal,
            items = itemsToUse.ifEmpty {
                listOf(ExtractedItem(name = "বাজারের সামগ্রী", amount = finalTotal, category = "groceries"))
            },
            category = itemsToUse.firstOrNull()?.category ?: "groceries",
            confidence = 0.90f
        )
    }
}

