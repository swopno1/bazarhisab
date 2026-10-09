package com.example.ui.history

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.BengaliNumberUtils
import com.example.data.model.ExpenseCategories
import com.example.data.model.ExpenseWithItems
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CurrencyText
import com.example.ui.home.ExpenseDetailBottomSheet
import com.example.ui.home.ExpenseListItemCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryFilterPeriod {
    ALL,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allExpenses by viewModel.allExpenses.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isBengali = settings?.language != "en"

    val monthLabel = if (isBengali) "মাস (Month)" else stringResource(R.string.filter_month)
    val weekLabel = if (isBengali) "সপ্তাহ (Week)" else stringResource(R.string.filter_week)
    val dayLabel = if (isBengali) "দিন (Day)" else stringResource(R.string.filter_today)
    val yearLabel = if (isBengali) "বছর (Year)" else stringResource(R.string.filter_year)
    val allLabel = if (isBengali) "সব (All)" else stringResource(R.string.filter_all)

    var selectedPeriod by remember { mutableStateOf(HistoryFilterPeriod.MONTH) }
    var periodOffset by remember { mutableStateOf(0) } // 0 = current, -1 = previous, etc.
    var selectedExpenseDetail by remember { mutableStateOf<ExpenseWithItems?>(null) }

    // Calculate time range for the current filter and offset
    val periodTimeRange = remember(selectedPeriod, periodOffset) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            HistoryFilterPeriod.ALL -> {
                0L to Long.MAX_VALUE
            }
            HistoryFilterPeriod.DAY -> {
                cal.add(Calendar.DAY_OF_YEAR, periodOffset)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
            HistoryFilterPeriod.WEEK -> {
                cal.add(Calendar.WEEK_OF_YEAR, periodOffset)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
            HistoryFilterPeriod.MONTH -> {
                cal.add(Calendar.MONTH, periodOffset)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
            HistoryFilterPeriod.YEAR -> {
                cal.add(Calendar.YEAR, periodOffset)
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                start to end
            }
        }
    }

    // Friendly Period Header Text
    val periodHeaderText = remember(selectedPeriod, periodOffset, isBengali) {
        val cal = Calendar.getInstance()
        val bengaliMonths = listOf(
            "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
        )
        when (selectedPeriod) {
            HistoryFilterPeriod.ALL -> {
                if (isBengali) "সব সময়ের হিসাব" else "All Time Expenses"
            }
            HistoryFilterPeriod.DAY -> {
                cal.add(Calendar.DAY_OF_YEAR, periodOffset)
                if (periodOffset == 0) {
                    val dayStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.time)
                    if (isBengali) "আজ (${BengaliNumberUtils.toBengaliDigits(cal.get(Calendar.DAY_OF_MONTH).toString())} ${bengaliMonths[cal.get(Calendar.MONTH)]} ${BengaliNumberUtils.toBengaliDigits(cal.get(Calendar.YEAR).toString())})"
                    else "Today ($dayStr)"
                } else if (periodOffset == -1) {
                    val dayStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.time)
                    if (isBengali) "গতকাল (${BengaliNumberUtils.toBengaliDigits(cal.get(Calendar.DAY_OF_MONTH).toString())} ${bengaliMonths[cal.get(Calendar.MONTH)]})"
                    else "Yesterday ($dayStr)"
                } else {
                    if (isBengali) {
                        "${BengaliNumberUtils.toBengaliDigits(cal.get(Calendar.DAY_OF_MONTH).toString())} ${bengaliMonths[cal.get(Calendar.MONTH)]} ${BengaliNumberUtils.toBengaliDigits(cal.get(Calendar.YEAR).toString())}"
                    } else {
                        SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(cal.time)
                    }
                }
            }
            HistoryFilterPeriod.WEEK -> {
                cal.add(Calendar.WEEK_OF_YEAR, periodOffset)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val startDay = cal.get(Calendar.DAY_OF_MONTH)
                val startMonth = cal.get(Calendar.MONTH)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                val endDay = cal.get(Calendar.DAY_OF_MONTH)
                val endMonth = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)

                if (isBengali) {
                    if (periodOffset == 0) {
                        "চলতি সপ্তাহ (${BengaliNumberUtils.toBengaliDigits(startDay.toString())} ${bengaliMonths[startMonth]} - ${BengaliNumberUtils.toBengaliDigits(endDay.toString())} ${bengaliMonths[endMonth]})"
                    } else {
                        "${BengaliNumberUtils.toBengaliDigits(startDay.toString())} ${bengaliMonths[startMonth]} - ${BengaliNumberUtils.toBengaliDigits(endDay.toString())} ${bengaliMonths[endMonth]} ${BengaliNumberUtils.toBengaliDigits(year.toString())}"
                    }
                } else {
                    if (periodOffset == 0) {
                        "This Week (${startDay} ${SimpleDateFormat("MMM", Locale.US).format(cal.time)} - ${endDay} ${SimpleDateFormat("MMM", Locale.US).format(cal.time)})"
                    } else {
                        "Week: $startDay - $endDay ${SimpleDateFormat("MMM yyyy", Locale.US).format(cal.time)}"
                    }
                }
            }
            HistoryFilterPeriod.MONTH -> {
                cal.add(Calendar.MONTH, periodOffset)
                val mIndex = cal.get(Calendar.MONTH)
                val year = cal.get(Calendar.YEAR)
                if (isBengali) {
                    "${bengaliMonths[mIndex]} ${BengaliNumberUtils.toBengaliDigits(year.toString())}"
                } else {
                    SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
                }
            }
            HistoryFilterPeriod.YEAR -> {
                cal.add(Calendar.YEAR, periodOffset)
                val year = cal.get(Calendar.YEAR)
                if (isBengali) {
                    "${BengaliNumberUtils.toBengaliDigits(year.toString())} সাল"
                } else {
                    year.toString()
                }
            }
        }
    }

    // Filter display list
    val displayExpenses = remember(searchQuery, searchResults, allExpenses, periodTimeRange) {
        val (startTime, endTime) = periodTimeRange
        val baseList = if (searchQuery.isNotBlank()) searchResults else allExpenses
        baseList.filter { it.expense.date in startTime..endTime }
    }

    val totalSpent = remember(displayExpenses) {
        displayExpenses.sumOf { it.expense.totalAmount }
    }

    // Category breakdown
    val categoryTotals = remember(displayExpenses) {
        displayExpenses.groupBy { it.expense.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.expense.totalAmount } }
            .toList()
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.history_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    // Export CSV for current filtered period
                    IconButton(
                        onClick = {
                            val csvData = generateCsvForExpenses(displayExpenses)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Bazar Hisab - $periodHeaderText")
                                putExtra(Intent.EXTRA_TEXT, csvData)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export Expenses"))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(stringResource(R.string.search_hint), fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Period Filter Tabs (Month, Week, Day, Year, All)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(
                        HistoryFilterPeriod.MONTH to monthLabel,
                        HistoryFilterPeriod.WEEK to weekLabel,
                        HistoryFilterPeriod.DAY to dayLabel,
                        HistoryFilterPeriod.YEAR to yearLabel,
                        HistoryFilterPeriod.ALL to allLabel
                    )

                    items(periods) { (period, label) ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPeriod = period
                                periodOffset = 0 // reset offset to current when changing mode
                            },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Interactive Period Navigator Card (when not ALL)
            if (selectedPeriod != HistoryFilterPeriod.ALL) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { periodOffset -= 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Period",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        // Tap to reset to current period
                                        periodOffset = 0
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = periodHeaderText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (periodOffset != 0) {
                                    Text(
                                        text = if (isBengali) "চলতি সময়ে ফিরতে চাপুন" else "Tap to return to current",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { periodOffset += 1 },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Period",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Total Spent & Count in Selected Period Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBengali) "এই সময়ের মোট খরচ" else "Total Spent",
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val countStr = if (isBengali)
                                "${BengaliNumberUtils.toBengaliDigits(displayExpenses.size.toString())}টি খরচ"
                            else
                                "${displayExpenses.size} items"
                            Text(
                                text = countStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        CurrencyText(
                            amount = totalSpent,
                            isBengali = isBengali,
                            fontSize = 22,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Category Breakdown Progress Bars for Selected Period
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.monthly_summary_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            categoryTotals.take(4).forEach { (catId, catAmount) ->
                                val category = ExpenseCategories.getCategory(catId)
                                val ratio = if (totalSpent > 0) (catAmount / totalSpent).toFloat() else 0f
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (isBengali) category.nameBn else category.nameEn,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        CurrencyText(
                                            amount = catAmount,
                                            isBengali = isBengali,
                                            fontSize = 13,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        color = category.color,
                                        trackColor = category.color.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Google AdMob Test Banner
            item {
                com.example.ads.AdmobBanner(isBengali = isBengali)
            }

            // Expenses List for Selected Period
            if (displayExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBengali) "এই সময়ে কোনো খরচের হিসাব নেই।" else stringResource(R.string.no_expenses_period),
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(displayExpenses, key = { it.expense.id }) { expenseWithItems ->
                    ExpenseListItemCard(
                        expenseWithItems = expenseWithItems,
                        isBengali = isBengali,
                        onClick = { selectedExpenseDetail = expenseWithItems }
                    )
                }
            }
        }
    }

    selectedExpenseDetail?.let { detail ->
        ExpenseDetailBottomSheet(
            expenseWithItems = detail,
            isBengali = isBengali,
            onDismiss = { selectedExpenseDetail = null },
            onDelete = {
                viewModel.deleteExpense(detail.expense.id)
                selectedExpenseDetail = null
            }
        )
    }
}

private fun generateCsvForExpenses(expenses: List<ExpenseWithItems>): String {
    val sb = StringBuilder()
    sb.append("ID,Date,Merchant,TotalAmount,Currency,Category,PaymentMethod,Items\n")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    for (item in expenses) {
        val e = item.expense
        val itemsStr = item.items.joinToString(";") {
            "${it.name} (${it.quantity ?: ""} ${it.unit ?: ""}: ${it.amount})"
        }.replace("\"", "'")
        sb.append("${e.id},\"${dateFormat.format(Date(e.date))}\",\"${e.merchant ?: ""}\",${e.totalAmount},${e.currency},${e.categoryId},${e.paymentMethod},\"$itemsStr\"\n")
    }
    return sb.toString()
}
