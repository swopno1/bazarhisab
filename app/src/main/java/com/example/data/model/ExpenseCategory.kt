package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val icon: ImageVector,
    val color: Color
)

object ExpenseCategories {
    val ALL = listOf(
        CategoryInfo("groceries", "Groceries", "মুদি ও বাজার", Icons.Filled.ShoppingCart, Color(0xFF2E7D32)),
        CategoryInfo("vegetables", "Vegetables", "শাক-সবজি", Icons.Filled.LocalFlorist, Color(0xFF388E3C)),
        CategoryInfo("fish", "Fish", "মাছ", Icons.Filled.SetMeal, Color(0xFF0288D1)),
        CategoryInfo("meat", "Meat", "মাংস", Icons.Filled.Restaurant, Color(0xFFD32F2F)),
        CategoryInfo("eggs", "Eggs", "ডিম", Icons.Filled.Egg, Color(0xFFF57C00)),
        CategoryInfo("rice_grains", "Rice & Grains", "চাল ও ডাল", Icons.Filled.Grain, Color(0xFF8D6E63)),
        CategoryInfo("oil_spices", "Oil & Spices", "তেল ও মসলা", Icons.Filled.Opacity, Color(0xFFFBC02D)),
        CategoryInfo("fruits", "Fruits", "ফলমূল", Icons.Filled.BreakfastDining, Color(0xFFE91E63)),
        CategoryInfo("household", "Household", "গৃহস্থালি", Icons.Filled.Home, Color(0xFF5D4037)),
        CategoryInfo("food", "Food & Snacks", "খাবার ও নাস্তা", Icons.Filled.LocalDining, Color(0xFF7B1FA2)),
        CategoryInfo("medicine", "Medicine", "ওষুধ", Icons.Filled.MedicalServices, Color(0xFF0097A7)),
        CategoryInfo("transport", "Transport", "যাতায়াত", Icons.Filled.DirectionsBus, Color(0xFF455A64)),
        CategoryInfo("bills", "Bills & Utility", "বিলসমূহ", Icons.Filled.Receipt, Color(0xFF512DA8)),
        CategoryInfo("other", "Other", "অন্যান্য", Icons.Filled.Category, Color(0xFF616161))
    )

    fun getCategory(id: String?): CategoryInfo {
        return ALL.find { it.id.equals(id, ignoreCase = true) } ?: ALL.first { it.id == "groceries" }
    }

    fun matchCategoryByItemName(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("চাল") || lower.contains("rice") || lower.contains("ডাল") || lower.contains("dal") || lower.contains("lentil") || lower.contains("আটা") || lower.contains("ময়দা") || lower.contains("flour") -> "rice_grains"
            lower.contains("মাছ") || lower.contains("fish") || lower.contains("ইলিশ") || lower.contains("রুই") || lower.contains("চিংড়ি") || lower.contains("shrimp") -> "fish"
            lower.contains("মাংস") || lower.contains("meat") || lower.contains("গরু") || lower.contains("beef") || lower.contains("মুরগি") || lower.contains("chicken") || lower.contains("খাসি") || lower.contains("mutton") -> "meat"
            lower.contains("ডিম") || lower.contains("egg") -> "eggs"
            lower.contains("আলু") || lower.contains("potato") || lower.contains("পেঁয়াজ") || lower.contains("পেঁয়াজ") || lower.contains("onion") || lower.contains("রসুন") || lower.contains("garlic") || lower.contains("আদা") || lower.contains("সবজি") || lower.contains("vegetable") || lower.contains("টমেটো") || lower.contains("tomato") || lower.contains("মরিচ") || lower.contains("chili") || lower.contains("পটল") || lower.contains("বেগুন") || lower.contains("শসা") || lower.contains("লাউ") -> "vegetables"
            lower.contains("তেল") || lower.contains("oil") || lower.contains("সয়াবিন") || lower.contains("সরিষা") || lower.contains("হলুদ") || lower.contains("মরিচ গুঁড়া") || lower.contains("spice") || lower.contains("লবণ") || lower.contains("salt") || lower.contains("চিনি") || lower.contains("sugar") -> "oil_spices"
            lower.contains("ফল") || lower.contains("fruit") || lower.contains("আম") || lower.contains("কলা") || lower.contains("banana") || lower.contains("আপেল") || lower.contains("apple") || lower.contains("কমলা") || lower.contains("orange") -> "fruits"
            lower.contains("ওষুধ") || lower.contains("medicine") || lower.contains("ঔষধ") || lower.contains("ফার্মেসি") || lower.contains("pharma") -> "medicine"
            lower.contains("রিকশা") || lower.contains("ভাড়া") || lower.contains("bus") || lower.contains("cng") || lower.contains("uber") || lower.contains("transport") -> "transport"
            lower.contains("বিল") || lower.contains("বিদ্যুৎ") || lower.contains("গ্যাস") || lower.contains("ওয়াইফাই") || lower.contains("wifi") || lower.contains("bill") -> "bills"
            lower.contains("সাবান") || lower.contains("soap") || lower.contains("শ্যাম্পু") || lower.contains("shampoo") || lower.contains("পেস্ট") || lower.contains("toothpaste") || lower.contains("সার্ফ") || lower.contains("detergent") -> "household"
            else -> "groceries"
        }
    }
}
