package com.valentinerutto.mywallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.banking.app.data.model.Transaction
import com.banking.app.data.model.UserProfile

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.exec(
            """CREATE TABLE IF NOT EXISTS transactions (
                id TEXT NOT NULL PRIMARY KEY,
                recipientName TEXT NOT NULL,
                amount REAL NOT NULL,
                note TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'QUEUED',
                createdAt INTEGER NOT NULL,
                lastAttemptAt INTEGER NOT NULL DEFAULT 0,
                attemptCount INTEGER NOT NULL DEFAULT 0,
                lastError TEXT,
                workManagerRequestId TEXT
            )"""
        )
    }
}

@Database(
    entities = [UserProfile::class, Transaction::class],
    version = 2,
    exportSchema = false
)
abstract class BankingDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun transactionDao(): TransactionDao
}
