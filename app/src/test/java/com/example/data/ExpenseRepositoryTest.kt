package com.example.data

import com.example.data.dao.ExpenseDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.ShoppingListDao
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExpenseRepositoryTest {

    private fun repositoryWithTotals(today: Double?, month: Double?): ExpenseRepository {
        val expenseDao = object : ExpenseDao {
            override fun getAllExpensesWithItems(): Flow<List<ExpenseWithItems>> = flowOf(emptyList())
            override fun getExpenseWithItemsById(id: Long): Flow<ExpenseWithItems?> = flowOf(null)
            override fun getTodayExpenses(startOfDay: Long): Flow<List<ExpenseWithItems>> = flowOf(emptyList())
            override fun getTodayTotal(startOfDay: Long): Flow<Double?> = flowOf(today)
            override fun getMonthTotal(startOfMonth: Long): Flow<Double?> = flowOf(month)
            override suspend fun findPotentialDuplicate(
                merchant: String?,
                amount: Double,
                startTime: Long,
                endTime: Long
            ): ExpenseEntity? = null

            override fun searchExpenses(query: String): Flow<List<ExpenseWithItems>> = flowOf(emptyList())
            override suspend fun insertExpense(expense: ExpenseEntity): Long = 0L
            override suspend fun insertExpenseItems(items: List<ExpenseItemEntity>) = Unit
            override suspend fun updateExpense(expense: ExpenseEntity) = Unit
            override suspend fun deleteExpenseById(id: Long) = Unit
            override suspend fun deleteExpenseItemsByExpenseId(expenseId: Long) = Unit
        }
        val shoppingDao = object : ShoppingListDao {
            override fun getAllItems() = flowOf(emptyList<com.example.data.model.ShoppingListItemEntity>())
            override suspend fun insertItem(item: com.example.data.model.ShoppingListItemEntity): Long = 0L
            override suspend fun insertItems(items: List<com.example.data.model.ShoppingListItemEntity>) = Unit
            override suspend fun updateItem(item: com.example.data.model.ShoppingListItemEntity) = Unit
            override suspend fun deleteItemById(id: Long) = Unit
            override suspend fun clearCompleted() = Unit
            override suspend fun clearAll() = Unit
        }
        val settingsDao = object : SettingsDao {
            override fun getSettings() = flowOf(null)
            override suspend fun getSettingsSync() = null
            override suspend fun insertOrUpdate(settings: com.example.data.model.AppSettingsEntity) = Unit
        }
        return ExpenseRepository(expenseDao, shoppingDao, settingsDao)
    }

    @Test
    fun `test null sums map to zero`() = runTest {
        val repository = repositoryWithTotals(today = null, month = null)
        assertEquals(0.0, repository.getTodayTotal().first(), 0.0)
        assertEquals(0.0, repository.getMonthTotal().first(), 0.0)
    }

    @Test
    fun `test sums pass through`() = runTest {
        val repository = repositoryWithTotals(today = 320.0, month = 12500.75)
        assertEquals(320.0, repository.getTodayTotal().first(), 0.0)
        assertEquals(12500.75, repository.getMonthTotal().first(), 0.0)
    }
}
