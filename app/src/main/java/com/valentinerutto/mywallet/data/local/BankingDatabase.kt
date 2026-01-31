package com.valentinerutto.mywallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.UserProfile


@Database(
    entities = [UserProfile::class, Transaction::class],
    version = 2,
    exportSchema = false
)
abstract class BankingDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun transactionDao(): TransactionDao
}
