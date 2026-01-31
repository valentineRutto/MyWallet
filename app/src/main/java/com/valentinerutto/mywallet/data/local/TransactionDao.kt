package com.valentinerutto.mywallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    /** Live stream of ALL transactions, newest first. */
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    /** Single lookup by local UUID – used by the Worker after sync. */
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Transaction?

    /** All rows that still need to be pushed to the server. */
    @Query("SELECT * FROM transactions WHERE status IN (:statuses) ORDER BY createdAt ASC")
    suspend fun getByStatuses(statuses: List<TransactionStatus>): List<Transaction>

   @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
