package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.ExpenseDao
import com.example.data.db.AppDatabase
import com.example.data.model.ExpenseEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.expenseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test sums are null on empty database`() = runTest {
        assertNull(dao.getTodayTotal(0L).first())
        assertNull(dao.getMonthTotal(0L).first())
    }

    @Test
    fun `test sums aggregate inserted totals`() = runTest {
        val now = System.currentTimeMillis()
        dao.insertExpense(ExpenseEntity(title = "A", totalAmount = 100.0, date = now))
        dao.insertExpense(ExpenseEntity(title = "B", totalAmount = 50.5, date = now))
        assertEquals(150.5, dao.getTodayTotal(0L).first() ?: 0.0, 0.001)
        assertEquals(150.5, dao.getMonthTotal(0L).first() ?: 0.0, 0.001)
    }

    @Test
    fun `test duplicate match tolerates paisa noise`() = runTest {
        val now = System.currentTimeMillis()
        dao.insertExpense(
            ExpenseEntity(title = "Shop", totalAmount = 100.0, merchant = "Karim Store", date = now)
        )
        val window = 24 * 60 * 60 * 1000L
        // Exact and near-exact totals match.
        assertNotNull(dao.findPotentialDuplicate("Karim Store", 100.0, now - window, now + window))
        assertNotNull(dao.findPotentialDuplicate("Karim Store", 100.005, now - window, now + window))
        // Clearly different totals and merchants do not.
        assertNull(dao.findPotentialDuplicate("Karim Store", 100.5, now - window, now + window))
        assertNull(dao.findPotentialDuplicate("Other Store", 100.0, now - window, now + window))
    }
}
