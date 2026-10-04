package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.data.model.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpensesWithItems(): Flow<List<ExpenseWithItems>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseWithItemsById(id: Long): Flow<ExpenseWithItems?>

    @Transaction
    @Query("SELECT * FROM expenses WHERE date >= :startOfDay ORDER BY date DESC")
    fun getTodayExpenses(startOfDay: Long): Flow<List<ExpenseWithItems>>

    @Query("SELECT SUM(totalAmount) FROM expenses WHERE date >= :startOfDay")
    fun getTodayTotal(startOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM expenses WHERE date >= :startOfMonth")
    fun getMonthTotal(startOfMonth: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE merchant IS NOT NULL AND merchant = :merchant AND totalAmount = :amount AND date BETWEEN :startTime AND :endTime LIMIT 1")
    suspend fun findPotentialDuplicate(merchant: String?, amount: Double, startTime: Long, endTime: Long): ExpenseEntity?

    @Transaction
    @Query("""
        SELECT DISTINCT e.* FROM expenses e
        LEFT JOIN expense_items i ON e.id = i.expenseId
        WHERE e.title LIKE '%' || :query || '%'
           OR (e.merchant IS NOT NULL AND e.merchant LIKE '%' || :query || '%')
           OR (e.note IS NOT NULL AND e.note LIKE '%' || :query || '%')
           OR (i.name IS NOT NULL AND i.name LIKE '%' || :query || '%')
        ORDER BY e.date DESC
    """)
    fun searchExpenses(query: String): Flow<List<ExpenseWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItems(items: List<ExpenseItemEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM expense_items WHERE expenseId = :expenseId")
    suspend fun deleteExpenseItemsByExpenseId(expenseId: Long)

    @Transaction
    suspend fun insertExpenseWithItems(expense: ExpenseEntity, items: List<ExpenseItemEntity>): Long {
        val expenseId = insertExpense(expense)
        if (items.isNotEmpty()) {
            val itemsWithId = items.map { it.copy(expenseId = expenseId) }
            insertExpenseItems(itemsWithId)
        }
        return expenseId
    }

    @Transaction
    suspend fun updateExpenseWithItems(expense: ExpenseEntity, items: List<ExpenseItemEntity>) {
        updateExpense(expense)
        deleteExpenseItemsByExpenseId(expense.id)
        if (items.isNotEmpty()) {
            val itemsWithId = items.map { it.copy(expenseId = expense.id) }
            insertExpenseItems(itemsWithId)
        }
    }
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY isPurchased ASC, id DESC")
    fun getAllItems(): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM shopping_list_items WHERE isPurchased = 1")
    suspend fun clearCompleted()

    @Query("DELETE FROM shopping_list_items")
    suspend fun clearAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsSync(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: AppSettingsEntity)
}
