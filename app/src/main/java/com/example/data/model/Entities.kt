package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["date"]), Index(value = ["merchant"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val householdId: Long = 1,
    val title: String,
    val totalAmount: Double,
    val currency: String = "BDT",
    val date: Long = System.currentTimeMillis(),
    val merchant: String? = null,
    val categoryId: String = "groceries",
    val paymentMethod: String = "cash",
    val source: String = "MANUAL", // MANUAL, RECEIPT, VOICE, SHOPPING_LIST
    val note: String? = null,
    val imageUri: String? = null,
    val imageHash: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expense_items",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"])]
)
data class ExpenseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseId: Long,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val unitPrice: Double? = null,
    val amount: Double,
    val categoryId: String? = null,
    val confidence: Float = 1.0f
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: String? = null,
    val estimatedPrice: Double? = null,
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val language: String = "bn", // "bn" or "en"
    val householdName: String = "আমার পরিবার",
    val monthlyBudget: Double = 20000.0,
    val currency: String = "BDT",
    val firstLaunchCompleted: Boolean = true
)

data class ExpenseWithItems(
    @Embedded
    val expense: ExpenseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val items: List<ExpenseItemEntity>
)
