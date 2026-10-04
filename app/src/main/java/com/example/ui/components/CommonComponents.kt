package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.BengaliNumberUtils
import com.example.data.model.ExpenseCategories

@Composable
fun CurrencyText(
    amount: Double,
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: Int = 20,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = BengaliNumberUtils.formatCurrency(amount, isBengali),
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CategoryBadge(
    categoryId: String?,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val category = ExpenseCategories.getCategory(categoryId)
    Surface(
        color = category.color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isBengali) category.nameBn else category.nameEn,
                color = category.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ConfidenceIndicator(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    if (confidence >= 0.85f) {
        Box(
            modifier = modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(14.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFE65100).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = "Check",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
