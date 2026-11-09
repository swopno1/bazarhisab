package com.example

import com.example.ai.ExtractedItem
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ExpenseWithItems
import com.example.ui.HomeUiState
import com.example.ui.ReconciliationState
import com.example.ui.computeFinalAmount
import com.example.ui.history.categoryShare
import com.example.ui.history.csvAmount
import com.example.ui.history.csvField
import com.example.ui.history.generateCsvForExpenses
import com.example.ui.reconciliation.EditedAmount
import com.example.ui.reconciliation.validateEditedAmount
import com.example.ui.resolveDraftItemAmounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseMathTest {

    @Test
    fun `test budget progress clamps and guards zero budget`() {
        assertEquals(0.5f, HomeUiState(monthTotal = 10000.0, monthlyBudget = 20000.0).budgetProgress, 0.001f)
        assertEquals(1f, HomeUiState(monthTotal = 25000.0, monthlyBudget = 20000.0).budgetProgress, 0.001f)
        assertEquals(0f, HomeUiState(monthTotal = 5000.0, monthlyBudget = 0.0).budgetProgress, 0.001f)
    }

    @Test
    fun `test budget remaining floors overspend at zero`() {
        assertEquals(15000.0, HomeUiState(monthTotal = 5000.0, monthlyBudget = 20000.0).budgetRemaining, 0.0)
        assertEquals(0.0, HomeUiState(monthTotal = 25000.0, monthlyBudget = 20000.0).budgetRemaining, 0.0)
    }

    @Test
    fun `test budget percentage truncates and guards zero budget`() {
        assertEquals(25, HomeUiState(monthTotal = 5000.0, monthlyBudget = 20000.0).budgetPercentage)
        assertEquals(99, HomeUiState(monthTotal = 1999.0, monthlyBudget = 2000.0).budgetPercentage)
        assertEquals(0, HomeUiState(monthTotal = 5000.0, monthlyBudget = 0.0).budgetPercentage)
    }

    @Test
    fun `test calculated total counts nulls as zero`() {
        val state = ReconciliationState(
            items = listOf(
                ExtractedItem(name = "Rice", amount = 450.0),
                ExtractedItem(name = "Mystery", amount = null)
            )
        )
        assertEquals(450.0, state.calculatedTotal, 0.0)
    }

    @Test
    fun `test mismatch flag uses paisa tolerance`() {
        assertFalse(ReconciliationState(receiptTotal = 100.0, items = listOf(ExtractedItem(name = "A", amount = 100.005))).hasTotalMismatch)
        assertTrue(ReconciliationState(receiptTotal = 100.0, items = listOf(ExtractedItem(name = "A", amount = 100.02))).hasTotalMismatch)
        assertFalse(ReconciliationState(receiptTotal = null, items = listOf(ExtractedItem(name = "A", amount = 50.0))).hasTotalMismatch)
    }

    @Test
    fun `test item amount resolution prefers explicit then quantity times price`() {
        val resolved = resolveDraftItemAmounts(
            listOf(
                ExtractedItem(name = "Explicit", quantity = 5.0, unitPrice = 90.0, amount = 450.0),
                ExtractedItem(name = "Derived", quantity = 2.0, unitPrice = 50.0, amount = null),
                ExtractedItem(name = "Priceless", quantity = null, unitPrice = null, amount = null),
                ExtractedItem(name = "Half", quantity = 2.0, unitPrice = null, amount = null)
            )
        )
        assertEquals(450.0, resolved[0].amount ?: 0.0, 0.0)
        assertEquals(100.0, resolved[1].amount ?: 0.0, 0.0)
        assertNull(resolved[2].amount)
        assertNull(resolved[3].amount)
    }

    @Test
    fun `test final amount prefers positive item sum over receipt total`() {
        assertEquals(1150.0, computeFinalAmount(1150.0, 1160.0), 0.0)
        assertEquals(1160.0, computeFinalAmount(0.0, 1160.0), 0.0)
        assertEquals(1160.0, computeFinalAmount(0.0, 1160.0), 0.0)
        assertEquals(0.0, computeFinalAmount(0.0, null), 0.0)
        assertEquals(0.0, computeFinalAmount(0.0, 0.0), 0.0)
    }

    @Test
    fun `test edited amount validation`() {
        assertEquals(EditedAmount.Value(450.0), validateEditedAmount("৪৫০"))
        assertEquals(EditedAmount.Blank, validateEditedAmount(""))
        assertEquals(EditedAmount.Blank, validateEditedAmount("   "))
        assertEquals(EditedAmount.Invalid, validateEditedAmount("abc"))
        assertEquals(EditedAmount.Invalid, validateEditedAmount("12ab"))
        assertEquals(EditedAmount.Invalid, validateEditedAmount("0"))
        assertEquals(EditedAmount.Invalid, validateEditedAmount("-5"))
    }

    @Test
    fun `test category share guards zero total`() {
        assertEquals(0.25f, categoryShare(250.0, 1000.0), 0.001f)
        assertEquals(0f, categoryShare(250.0, 0.0), 0.0f)
    }

    @Test
    fun `test csv escapes quotes and fixes decimals`() {
        assertEquals("\"a\"\"b\"", csvField("a\"b"))
        assertEquals("1250.00", csvAmount(1250.0))
        assertEquals("0.30", csvAmount(0.1 + 0.2))
    }

    @Test
    fun `test csv export row is well formed`() {
        val csv = generateCsvForExpenses(
            listOf(
                ExpenseWithItems(
                    expense = ExpenseEntity(
                        id = 7,
                        title = "Weekly \"big\" shop",
                        totalAmount = 1250.5,
                        merchant = "Karim \"Best\" Store",
                        categoryId = "groceries"
                    ),
                    items = listOf(
                        ExpenseItemEntity(id = 1, expenseId = 7, name = "Rice", amount = 450.0)
                    )
                )
            )
        )
        val lines = csv.trim().split("\n")
        assertEquals(2, lines.size)
        assertTrue(lines[0].startsWith("ID,Date,Merchant,TotalAmount"))
        assertTrue(lines[1].contains("\"Karim \"\"Best\"\" Store\""))
        assertTrue(lines[1].contains(",1250.50,BDT,"))
    }
}
