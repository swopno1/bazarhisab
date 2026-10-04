package com.example.ui.reconciliation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.BengaliNumberUtils
import com.example.ai.ExtractedItem
import com.example.data.model.ExpenseCategories
import com.example.ui.ExpenseViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.ConfidenceIndicator
import com.example.ui.components.CurrencyText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconciliationScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onExpenseSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.reconciliationState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isBengali = settings?.language != "en"

    var editingItemIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingNewItem by remember { mutableStateOf(false) }

    var title by remember(state.title) { mutableStateOf(state.title) }
    var merchant by remember(state.merchant) { mutableStateOf(state.merchant) }
    var selectedPaymentMethod by remember(state.paymentMethod) { mutableStateOf(state.paymentMethod) }
    var note by remember(state.note) { mutableStateOf(state.note) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reconciliation_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.calculated_total),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        CurrencyText(
                            amount = state.calculatedTotal,
                            isBengali = isBengali,
                            fontSize = 22,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.updateDraftMeta(
                                title = title,
                                merchant = merchant,
                                date = state.date,
                                paymentMethod = selectedPaymentMethod,
                                category = state.categoryId,
                                note = note
                            )
                            viewModel.saveDraftExpense {
                                onExpenseSaved()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("save_expense_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.save_expense),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
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
            // Mismatch Alert Banner (Section 7: Total Validation)
            if (state.hasTotalMismatch) {
                item {
                    val calcStr = BengaliNumberUtils.formatCurrency(state.calculatedTotal, isBengali)
                    val recStr = BengaliNumberUtils.formatCurrency(state.receiptTotal ?: 0.0, isBengali)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF9800))),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.total_mismatch_warning, calcStr, recStr),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }
            }

            // Duplicate Detection Banner (Section 20)
            if (state.duplicateWarningExpense != null) {
                item {
                    val dupDate = SimpleDateFormat("dd MMMM", Locale.getDefault()).format(Date(state.duplicateWarningExpense!!.date))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF7043))),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.duplicate_receipt_warning, dupDate),
                                fontSize = 13.sp,
                                color = Color(0xFFB71C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Merchant / Shop & Title Header
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(stringResource(R.string.description_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = merchant,
                            onValueChange = { merchant = it },
                            label = { Text(stringResource(R.string.merchant_shop)) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Store, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Items List Header & + Add Item button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.items_header) + " (${state.items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedButton(
                        onClick = { isAddingNewItem = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_item_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.add_item), fontSize = 13.sp)
                    }
                }
            }

            // Items List
            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = if (isBengali) "কোনো পণ্য পাওয়া যায়নি। নিজে যোগ করুন।" else "No items extracted. Tap '+ Add Item' to add.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                itemsIndexed(state.items) { index, item ->
                    ItemReconciliationCard(
                        item = item,
                        isBengali = isBengali,
                        onClick = { editingItemIndex = index },
                        onDelete = { viewModel.removeDraftItem(index) }
                    )
                }
            }

            // Payment Method Selector
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.payment_method),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "cash" to stringResource(R.string.cash),
                                "bkash" to stringResource(R.string.bkash),
                                "nagad" to stringResource(R.string.nagad),
                                "card" to stringResource(R.string.card)
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedPaymentMethod == key,
                                    onClick = { selectedPaymentMethod = key },
                                    label = { Text(label, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(stringResource(R.string.notes_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet for Editing / Adding Item
    val activeItem = editingItemIndex?.let { state.items.getOrNull(it) }
    if (activeItem != null || isAddingNewItem) {
        ItemEditorBottomSheet(
            initialItem = activeItem,
            isBengali = isBengali,
            onDismiss = {
                editingItemIndex = null
                isAddingNewItem = false
            },
            onSave = { updatedItem ->
                if (editingItemIndex != null) {
                    viewModel.updateDraftItem(editingItemIndex!!, updatedItem)
                } else {
                    viewModel.addDraftItem(updatedItem)
                }
                editingItemIndex = null
                isAddingNewItem = false
            },
            onDelete = {
                if (editingItemIndex != null) {
                    viewModel.removeDraftItem(editingItemIndex!!)
                }
                editingItemIndex = null
                isAddingNewItem = false
            }
        )
    }
}

@Composable
fun ItemReconciliationCard(
    item: ExtractedItem,
    isBengali: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConfidenceIndicator(confidence = item.confidence)
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.quantity != null) {
                        Text(
                            text = "${item.quantity} ${item.unit ?: ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    CategoryBadge(categoryId = item.category, isBengali = isBengali)
                }
            }

            if (item.amount != null) {
                CurrencyText(
                    amount = item.amount,
                    isBengali = isBengali,
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = if (isBengali) "দাম দিন" else "Add Price",
                    color = Color(0xFFE65100),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemEditorBottomSheet(
    initialItem: ExtractedItem?,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (ExtractedItem) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var quantityStr by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "") }
    var amountStr by remember { mutableStateOf(initialItem?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(initialItem?.category ?: "groceries") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (initialItem != null) (if (isBengali) "পণ্য সংশোধন" else "Edit Item") else (if (isBengali) "নতুন পণ্য যোগ" else "Add New Item"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.item_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text(stringResource(R.string.item_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text(stringResource(R.string.item_unit)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text(stringResource(R.string.item_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (initialItem != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
                Button(
                    onClick = {
                        val parsedAmount = BengaliNumberUtils.parseAmount(amountStr)
                        val parsedQty = BengaliNumberUtils.parseAmount(quantityStr)
                        onSave(
                            ExtractedItem(
                                name = name.ifBlank { "পণ্য" },
                                quantity = parsedQty,
                                unit = unit.ifBlank { null },
                                amount = parsedAmount ?: 0.0,
                                category = selectedCategory,
                                confidence = 1.0f
                            )
                        )
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
