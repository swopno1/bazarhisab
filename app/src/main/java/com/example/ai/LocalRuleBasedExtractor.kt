package com.example.ai

import com.example.data.model.ExpenseCategories
import java.util.Locale

object LocalRuleBasedExtractor {

    /** Bengali number words 1-99 (with common spelling variants). */
    private val BENGALI_ONES_TENS = mapOf(
        "এক" to 1, "দুই" to 2, "তিন" to 3, "চার" to 4, "পাঁচ" to 5,
        "ছয়" to 6, "সাত" to 7, "আট" to 8, "নয়" to 9, "দশ" to 10,
        "এগারো" to 11, "বারো" to 12, "তেরো" to 13, "চৌদ্দ" to 14, "পনেরো" to 15,
        "ষোলো" to 16, "ষোল" to 16, "সতেরো" to 17, "আঠারো" to 18, "ঊনিশ" to 19,
        "কুড়ি" to 20, "বিশ" to 20,
        "একুশ" to 21, "বাইশ" to 22, "তেইশ" to 23, "চব্বিশ" to 24, "পঁচিশ" to 25,
        "ছাব্বিশ" to 26, "সাতাশ" to 27, "আটাশ" to 28, "ঊনত্রিশ" to 29,
        "ত্রিশ" to 30, "একত্রিশ" to 31, "বত্রিশ" to 32, "তেত্রিশ" to 33,
        "চৌত্রিশ" to 34, "পঁয়ত্রিশ" to 35, "ছত্রিশ" to 36, "সাঁইত্রিশ" to 37,
        "আটত্রিশ" to 38, "ঊনচল্লিশ" to 39, "চল্লিশ" to 40,
        "একচল্লিশ" to 41, "বিয়াল্লিশ" to 42, "তেতাল্লিশ" to 43, "চুয়াল্লিশ" to 44,
        "পঁয়তাল্লিশ" to 45, "ছেচল্লিশ" to 46, "সাতচল্লিশ" to 47, "আটচল্লিশ" to 48,
        "ঊনপঞ্চাশ" to 49, "পঞ্চাশ" to 50,
        "একান্ন" to 51, "বাহান্ন" to 52, "তেপ্পান্ন" to 53, "চুয়ান্ন" to 54,
        "পঞ্চান্ন" to 55, "ছাপ্পান্ন" to 56, "সাতান্ন" to 57, "আটান্ন" to 58,
        "ঊনষাট" to 59, "ষাট" to 60,
        "একষট্টি" to 61, "বাষট্টি" to 62, "তেষট্টি" to 63, "চৌষট্টি" to 64,
        "পঁয়ষট্টি" to 65, "ছেষট্টি" to 66, "সাতষট্টি" to 67, "আটষট্টি" to 68,
        "ঊনসত্তর" to 69, "সত্তর" to 70,
        "একাত্তর" to 71, "বাহাত্তর" to 72, "তিয়াত্তর" to 73, "চুয়াত্তর" to 74,
        "পঁচাত্তর" to 75, "ছিয়াত্তর" to 76, "সাতাত্তর" to 77, "আটাত্তর" to 78,
        "ঊনআশি" to 79, "আশি" to 80,
        "একাশি" to 81, "বিরাশি" to 82, "তিরাশি" to 83, "চুরাশি" to 84,
        "পঁচাশি" to 85, "ছিয়াশি" to 86, "সাতাশি" to 87, "আটাশি" to 88,
        "ঊননব্বই" to 89, "নব্বই" to 90,
        "একানব্বই" to 91, "বিরানব্বই" to 92, "তিরানব্বই" to 93, "চুরানব্বই" to 94,
        "পঁচানব্বই" to 95, "ছিয়ানব্বই" to 96, "সাতানব্বই" to 97, "আটানব্বই" to 98,
        "নিরানব্বই" to 99
    )

    /**
     * Parse a Bengali number-word phrase (used only when the text contains
     * no digits at all): lakh + thousand + hundred + tens/ones, e.g.
     * "এক হাজার দুইশ পঞ্চাশ" is 1250. Larger scales are consumed first so
     * "পাঁচ" inside "পাঁচশ" is never double-counted as 5.
     */
    fun parseBengaliNumberWords(text: String): Double? {
        var remaining = BengaliNumberUtils.toEnglishDigits(text.lowercase())
        var total = 0.0

        fun takeMultiplier(word: String): Double? {
            val m = BENGALI_ONES_TENS[word]
            if (m != null) return m.toDouble()
            if (word == "দেড়" || word == "দেড়") return 1.5
            if (word == "আড়াই" || word == "আড়াই") return 2.5
            return word.toDoubleOrNull()?.takeIf { it in 1.0..99.0 }
        }

        // Note: \b is ASCII-only in Java regex, so Bengali scale words use
        // an explicit lookahead for whitespace, danda, comma, or end.
        // Lakh: "দুই লাখ" / "2 লক্ষ" / bare "লাখ".
        val lakhMatch = Regex("""([^\s]+)\s*লাখ(?=[\s।,]|$)|([^\s]+)\s*লক্ষ(?=[\s।,]|$)""").find(remaining)
        val lakhMult = lakhMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }
            ?.let { takeMultiplier(it) }
        if (lakhMult != null && lakhMatch != null) {
            total += lakhMult * 100000
            remaining = remaining.removeRange(lakhMatch.range)
        } else if (remaining.contains("লাখ") || remaining.contains("লক্ষ")) {
            total += 100000
            remaining = remaining.replace("লক্ষ", "").replace("লাখ", "")
        }

        // Thousand: "দশ হাজার" / "1.5 হাজার" via দেড় / bare "হাজার".
        val thousandMatch = Regex("""([^\s]+)\s*হাজার(?=[\s।,]|$)""").find(remaining)
        val thousandMult = thousandMatch?.groupValues?.get(1)?.let { takeMultiplier(it) }
        if (thousandMult != null && thousandMatch != null) {
            total += thousandMult * 1000
            remaining = remaining.removeRange(thousandMatch.range)
        } else if (remaining.contains("হাজার")) {
            total += 1000
            remaining = remaining.replace("হাজার", "")
        }

        // Hundred: "দুইশ" / "দুই শত" / "দেড়শ" / bare "শত".
        val hundredSpecial = when {
            remaining.contains("দেড়শ") || remaining.contains("দেড়শ") -> 150.0
            remaining.contains("আড়াইশ") || remaining.contains("আড়াইশ") -> 250.0
            else -> null
        }
        if (hundredSpecial != null) {
            total += hundredSpecial
            remaining = remaining
                .replace("দেড়শ", "").replace("দেড়শ", "")
                .replace("আড়াইশ", "").replace("আড়াইশ", "")
        } else {
            val hundredMatch = Regex("""([^\s]+?)\s*শত(?=[\s।,]|$)|([^\s]+?)শ(?=[\s।,]|$)""").find(remaining)
            val hundredWord = hundredMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }
            val hundredMult = hundredWord?.let { takeMultiplier(it) }
            if (hundredMult != null && hundredMatch != null) {
                total += hundredMult * 100
                remaining = remaining.removeRange(hundredMatch.range)
            } else if (remaining.contains("শত")) {
                total += 100
                remaining = remaining.replace("শত", "")
            }
        }

        // Tens/ones: longest whole-token match in what is left. Tokens
        // (not substrings) so "দশ" inside a shop name is never read as 10.
        val tokens = remaining.split(Regex("""[\s।,\n]+"""))
            .map { it.trim('।', ',', '.', '!', '?', ';', ':') }
            .filter { it.isNotEmpty() }
        val smallWord = BENGALI_ONES_TENS.keys
            .filter { word -> tokens.any { it == word } }
            .maxByOrNull { it.length }
        if (smallWord != null) {
            total += BENGALI_ONES_TENS.getValue(smallWord)
        }

        return if (total > 0.0) total else null
    }

    /**
     * Removes thousands-grouping commas between digits ("1,160" -> "1160",
     * "১,১৬০" -> "1160" once Bengali digits are converted). Plain commas
     * that separate items/phrases are left untouched.
     */
    private fun stripGroupedCommas(text: String): String =
        text.replace(Regex("(?<=\\d),(?=\\d)"), "")

    /** Quantity units. The negative lookahead stops "৮০ টাকা" being read as
     * quantity 80 with unit "টা" (the "টা" in "টাকা" is followed by a letter). */
    private const val QTY_UNIT_PATTERN =
        """(\d+(\.\d+)?)\s*(কেজি|লিটার|গ্রাম|টা|টি|kg|g|l|liter|pcs|dozen)(?![\p{L}])"""

    /**
     * Parse natural language voice text into ExtractedExpense.
     *
     * Price rule: strip the quantity+unit first, then take the LAST number
     * in the segment as the price — so "চাল ৫ কেজি ৪৫০ টাকা" is ৳450
     * (qty 5), not ৳5. This mirrors parseShoppingList.
     */
    fun parseVoice(text: String): ExtractedExpense {
        // Strip grouping commas BEFORE splitting: "১,২৫০" must survive as
        // 1250 instead of being split into "১" and "২৫০" at the comma.
        val engDigitsText = stripGroupedCommas(BengaliNumberUtils.toEnglishDigits(text))
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

        for (rawPart in parts) {
            val part = stripGroupedCommas(rawPart)

            // 1. Extract quantity+unit first, then remove it from the segment.
            val qtyMatch = Regex(QTY_UNIT_PATTERN, RegexOption.IGNORE_CASE).find(part)
            var qty: Double? = null
            var unit: String? = null
            var remaining = part
            if (qtyMatch != null) {
                qty = qtyMatch.groupValues[1].toDoubleOrNull()
                unit = qtyMatch.groupValues[3]
                remaining = remaining.removeRange(qtyMatch.range).trim()
            }

            // 2. Price is the LAST number left in the segment.
            val priceMatches = Regex("""(\d+(\.\d+)?)\s*(টাকা|tk|taka)?""", RegexOption.IGNORE_CASE)
                .findAll(remaining).toList()
            val priceMatch = priceMatches.lastOrNull()
            if (priceMatch != null) {
                val amount = priceMatch.groupValues[1].toDoubleOrNull() ?: continue
                // Clean name: text before the price (or after it, if that is all there is).
                var name = remaining.substring(0, priceMatch.range.first).trim()
                if (name.isBlank()) {
                    name = remaining.substring(priceMatch.range.last + 1).trim()
                }
                name = name.replace(Regex("""^(আজ|আজকে|কিনেছি|খরচ|হয়েছে|মোট|টাকা|today|bought|spent|for)\s*""", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("""\s*(কিনেছি|খরচ|হয়েছে|টাকা|bought|spent)$""", RegexOption.IGNORE_CASE), "")
                    .trim()

                if (name.isNotBlank()) {
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

            val engDigits = stripGroupedCommas(BengaliNumberUtils.toEnglishDigits(cleanLine))

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
            val qtyMatch = Regex(QTY_UNIT_PATTERN, RegexOption.IGNORE_CASE).find(remaining)
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
            val engLine = stripGroupedCommas(BengaliNumberUtils.toEnglishDigits(line))

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

