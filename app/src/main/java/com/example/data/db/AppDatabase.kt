package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ExpenseDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.ShoppingListDao
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ExpenseEntity
import com.example.data.model.ExpenseItemEntity
import com.example.data.model.ShoppingListItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        ExpenseItemEntity::class,
        ShoppingListItemEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bazar_hisab_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default settings and welcome initial data
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            database.settingsDao().insertOrUpdate(
                                AppSettingsEntity(
                                    id = 1,
                                    language = "bn",
                                    householdName = "আমার পরিবার",
                                    monthlyBudget = 20000.0,
                                    currency = "BDT"
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
