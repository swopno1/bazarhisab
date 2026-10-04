package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.ShoppingListDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.data.model.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val shoppingListDao: ShoppingListDao,
    private val settingsDao: SettingsDao
) {
    val allExpenses: Flow<List<ExpenseWithItems>> = expenseDao.getAllExpensesWithItems()

    val shoppingItems: Flow<List<ShoppingListItemEntity>> = shoppingListDao.getAllItems()

    val settings: Flow<AppSettingsEntity?> = settingsDao.getSettings()

    fun getTodayExpenses(): Flow<List<ExpenseWithItems>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return expenseDao.getTodayExpenses(calendar.timeInMillis)
    }

    fun getTodayTotal(): Flow<Double> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return expenseDao.getTodayTotal(calendar.timeInMillis).map { it ?: 0.0 }
    }

    fun getMonthTotal(): Flow<Double> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return expenseDao.getMonthTotal(calendar.timeInMillis).map { it ?: 0.0 }
    }

    fun searchExpenses(query: String): Flow<List<ExpenseWithItems>> {
        return expenseDao.searchExpenses(query)
    }

    suspend fun findDuplicate(merchant: String?, amount: Double, timestamp: Long): ExpenseEntity? {
        val window = 24 * 60 * 60 * 1000L // 24 hours
        return expenseDao.findPotentialDuplicate(
            merchant = merchant,
            amount = amount,
            startTime = timestamp - window,
            endTime = timestamp + window
        )
    }

    suspend fun insertExpense(expense: ExpenseEntity, items: List<ExpenseItemEntity>): Long {
        return expenseDao.insertExpenseWithItems(expense, items)
    }

    suspend fun updateExpense(expense: ExpenseEntity, items: List<ExpenseItemEntity>) {
        expenseDao.updateExpenseWithItems(expense, items)
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    fun getExpenseById(id: Long): Flow<ExpenseWithItems?> {
        return expenseDao.getExpenseWithItemsById(id)
    }

    suspend fun addShoppingItem(name: String, quantity: String?, price: Double?): Long {
        return shoppingListDao.insertItem(
            ShoppingListItemEntity(
                name = name.trim(),
                quantity = quantity?.trim(),
                estimatedPrice = price
            )
        )
    }

    suspend fun addShoppingItems(items: List<ShoppingListItemEntity>) {
        shoppingListDao.insertItems(items)
    }

    suspend fun toggleShoppingItem(item: ShoppingListItemEntity) {
        shoppingListDao.updateItem(item.copy(isPurchased = !item.isPurchased))
    }

    suspend fun deleteShoppingItem(id: Long) {
        shoppingListDao.deleteItemById(id)
    }

    suspend fun clearCompletedShoppingItems() {
        shoppingListDao.clearCompleted()
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        settingsDao.insertOrUpdate(settings)
    }

    fun generateCsv(expenses: List<ExpenseWithItems>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Merchant,Category,Amount,Currency,PaymentMethod,Source,Items,Note\n")
        for (item in expenses) {
            val exp = item.expense
            val itemsSummary = item.items.joinToString(";") {
                "${it.name} (${it.quantity ?: ""} ${it.unit ?: ""}: ${it.amount})"
            }.replace(",", " ").replace("\"", "'")
            val dateStr = dateFormat.format(Date(exp.date))
            sb.append("${exp.id},\"$dateStr\",\"${exp.title}\",\"${exp.merchant ?: ""}\",\"${exp.categoryId}\",${exp.totalAmount},${exp.currency},\"${exp.paymentMethod}\",\"${exp.source}\",\"$itemsSummary\",\"${exp.note ?: ""}\"\n")
        }
        return sb.toString()
    }
}
