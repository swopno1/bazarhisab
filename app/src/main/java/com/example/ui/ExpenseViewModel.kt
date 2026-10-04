package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BengaliNumberUtils
import com.example.ai.ExpenseAiService
import com.example.ai.ExtractedExpense
import com.example.ai.ExtractedItem
import com.example.ai.GeminiExpenseAiService
import com.example.data.db.AppDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.data.model.ShoppingListItemEntity
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReconciliationState(
    val title: String = "বাজারের হিসাব",
    val merchant: String = "",
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "cash",
    val categoryId: String = "groceries",
    val note: String = "",
    val items: List<ExtractedItem> = emptyList(),
    val receiptTotal: Double? = null,
    val source: String = "MANUAL",
    val imageUri: String? = null,
    val isScanning: Boolean = false,
    val duplicateWarningExpense: ExpenseEntity? = null
) {
    val calculatedTotal: Double
        get() = items.sumOf { it.amount ?: 0.0 }

    val hasTotalMismatch: Boolean
        get() = receiptTotal != null && Math.abs(calculatedTotal - receiptTotal) > 0.01
}

data class HomeUiState(
    val todayTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val monthlyBudget: Double = 20000.0,
    val todayExpenses: List<ExpenseWithItems> = emptyList(),
    val recentExpenses: List<ExpenseWithItems> = emptyList(),
    val householdName: String = "আমার পরিবার",
    val language: String = "bn"
) {
    val budgetProgress: Float
        get() = if (monthlyBudget > 0) (monthTotal / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f

    val budgetRemaining: Double
        get() = (monthlyBudget - monthTotal).coerceAtLeast(0.0)

    val budgetPercentage: Int
        get() = if (monthlyBudget > 0) ((monthTotal / monthlyBudget) * 100).toInt() else 0
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val aiService: ExpenseAiService = GeminiExpenseAiService()

    val allExpenses: StateFlow<List<ExpenseWithItems>>
    val shoppingItems: StateFlow<List<ShoppingListItemEntity>>
    val settings: StateFlow<AppSettingsEntity?>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ExpenseWithItems>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _reconciliationState = MutableStateFlow(ReconciliationState())
    val reconciliationState = _reconciliationState.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing = _isAiProcessing.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage = _aiErrorMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.shoppingListDao(), db.settingsDao())

        allExpenses = repository.allExpenses
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        shoppingItems = repository.shoppingItems
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        settings = repository.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.getTodayTotal(),
        repository.getMonthTotal(),
        repository.getTodayExpenses(),
        repository.allExpenses,
        repository.settings
    ) { todayTotal, monthTotal, todayExpenses, allExps, settingsEntity ->
        HomeUiState(
            todayTotal = todayTotal,
            monthTotal = monthTotal,
            monthlyBudget = settingsEntity?.monthlyBudget ?: 20000.0,
            todayExpenses = todayExpenses,
            recentExpenses = allExps.take(10),
            householdName = settingsEntity?.householdName ?: "আমার পরিবার",
            language = settingsEntity?.language ?: "bn"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                repository.searchExpenses(query).collect {
                    _searchResults.value = it
                }
            }
        }
    }

    // --- AI & Draft Actions ---

    fun processReceiptImage(bitmap: Bitmap, imageUri: String? = null, onReady: () -> Unit) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiErrorMessage.value = null
            val result = aiService.extractFromReceipt(bitmap)
            _isAiProcessing.value = false

            val extracted = result.getOrElse {
                ExtractedExpense(
                    merchant = "রশিদ / Receipt",
                    items = listOf(ExtractedItem(name = "বাজারের সামগ্রী", amount = null)),
                    confidence = 0.5f
                )
            }
            loadDraftFromExtracted(extracted, source = "RECEIPT", imageUri = imageUri)
            onReady()
        }
    }

    fun processVoiceText(text: String, onReady: () -> Unit) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            _aiErrorMessage.value = null
            val result = aiService.extractFromVoiceText(text)
            _isAiProcessing.value = false

            val extracted = result.getOrElse {
                ExtractedExpense(
                    items = listOf(ExtractedItem(name = text.take(30), amount = null)),
                    confidence = 0.5f
                )
            }
            loadDraftFromExtracted(extracted, source = "VOICE")
            onReady()
        }
    }

    fun prepareManualDraft(amount: Double?, description: String, category: String, onReady: () -> Unit) {
        val items = if (amount != null && amount > 0) {
            listOf(ExtractedItem(name = description.ifBlank { "বাজার" }, amount = amount, category = category))
        } else emptyList()

        val draft = ExtractedExpense(
            merchant = null,
            total = amount,
            items = items,
            category = category,
            confidence = 1.0f
        )
        loadDraftFromExtracted(draft, source = "MANUAL")
        onReady()
    }

    fun convertShoppingListToDraft(onReady: () -> Unit) {
        val currentItems = shoppingItems.value
        val itemsToConvert = currentItems.filter { it.isPurchased }.ifEmpty { currentItems }
        val extractedItems = itemsToConvert.map {
            ExtractedItem(
                name = it.name,
                quantity = BengaliNumberUtils.parseAmount(it.quantity),
                unit = it.quantity?.replace(Regex("[0-9০-৯.]+"), "")?.trim(),
                amount = it.estimatedPrice,
                category = "groceries",
                confidence = 1.0f
            )
        }
        val draft = ExtractedExpense(
            items = extractedItems,
            total = extractedItems.sumOf { it.amount ?: 0.0 }.takeIf { it > 0 },
            category = "groceries"
        )
        loadDraftFromExtracted(draft, source = "SHOPPING_LIST")
        onReady()
    }

    private fun loadDraftFromExtracted(
        extracted: ExtractedExpense,
        source: String,
        imageUri: String? = null
    ) {
        val total = extracted.total ?: extracted.items.sumOf { it.amount ?: 0.0 }.takeIf { it > 0 }
        _reconciliationState.value = ReconciliationState(
            title = extracted.merchant?.ifBlank { "বাজারের খরচ" } ?: "বাজারের খরচ",
            merchant = extracted.merchant ?: "",
            date = System.currentTimeMillis(),
            paymentMethod = extracted.paymentMethod ?: "cash",
            categoryId = extracted.category ?: "groceries",
            note = extracted.notes ?: "",
            items = extracted.items,
            receiptTotal = extracted.total,
            source = source,
            imageUri = imageUri
        )

        // Check potential duplicate in background
        if (total != null && total > 0) {
            viewModelScope.launch {
                val duplicate = repository.findDuplicate(extracted.merchant, total, System.currentTimeMillis())
                if (duplicate != null) {
                    _reconciliationState.value = _reconciliationState.value.copy(
                        duplicateWarningExpense = duplicate
                    )
                }
            }
        }
    }

    fun updateDraftItem(index: Int, updatedItem: ExtractedItem) {
        val current = _reconciliationState.value.items.toMutableList()
        if (index in current.indices) {
            current[index] = updatedItem
            _reconciliationState.value = _reconciliationState.value.copy(items = current)
        }
    }

    fun addDraftItem(item: ExtractedItem) {
        val current = _reconciliationState.value.items.toMutableList()
        current.add(item)
        _reconciliationState.value = _reconciliationState.value.copy(items = current)
    }

    fun removeDraftItem(index: Int) {
        val current = _reconciliationState.value.items.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _reconciliationState.value = _reconciliationState.value.copy(items = current)
        }
    }

    fun updateDraftMeta(
        title: String,
        merchant: String,
        date: Long,
        paymentMethod: String,
        category: String,
        note: String
    ) {
        _reconciliationState.value = _reconciliationState.value.copy(
            title = title,
            merchant = merchant,
            date = date,
            paymentMethod = paymentMethod,
            categoryId = category,
            note = note
        )
    }

    fun saveDraftExpense(onSaved: () -> Unit) {
        val state = _reconciliationState.value
        val finalAmount = if (state.items.isNotEmpty()) {
            state.calculatedTotal
        } else {
            state.receiptTotal ?: 0.0
        }

        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = state.title.ifBlank { "বাজারের খরচ" },
                totalAmount = finalAmount,
                currency = "BDT",
                date = state.date,
                merchant = state.merchant.ifBlank { null },
                categoryId = state.categoryId,
                paymentMethod = state.paymentMethod,
                source = state.source,
                note = state.note.ifBlank { null },
                imageUri = state.imageUri
            )

            val items = state.items.map {
                ExpenseItemEntity(
                    expenseId = 0,
                    name = it.name.ifBlank { "পণ্য" },
                    quantity = it.quantity,
                    unit = it.unit,
                    unitPrice = it.unitPrice,
                    amount = it.amount ?: 0.0,
                    categoryId = it.category ?: state.categoryId,
                    confidence = it.confidence
                )
            }

            repository.insertExpense(expense, items)
            onSaved()
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    // --- Shopping List Operations ---

    fun addShoppingItem(name: String, quantity: String?, price: Double?) {
        viewModelScope.launch {
            repository.addShoppingItem(name, quantity, price)
        }
    }

    fun pasteAndParseShoppingList(rawText: String) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val parsed = aiService.parseShoppingList(rawText).getOrElse {
                emptyList()
            }
            _isAiProcessing.value = false

            val entities = parsed.map {
                ShoppingListItemEntity(
                    name = it.name,
                    quantity = if (it.quantity != null) "${it.quantity} ${it.unit ?: ""}".trim() else it.unit,
                    estimatedPrice = it.amount
                )
            }
            repository.addShoppingItems(entities)
        }
    }

    fun toggleShoppingItem(item: ShoppingListItemEntity) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
        }
    }

    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun clearCompletedShoppingItems() {
        viewModelScope.launch {
            repository.clearCompletedShoppingItems()
        }
    }

    // --- Settings Operations ---

    fun updateSettings(language: String? = null, householdName: String? = null, monthlyBudget: Double? = null) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettingsEntity()
            val updated = current.copy(
                language = language ?: current.language,
                householdName = householdName ?: current.householdName,
                monthlyBudget = monthlyBudget ?: current.monthlyBudget
            )
            repository.updateSettings(updated)
        }
    }

    fun getCsvData(): String {
        return repository.generateCsv(allExpenses.value)
    }
}
