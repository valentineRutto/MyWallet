package com.valentinerutto.mywallet.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.valentinerutto.mywallet.data.model.TransactionStatus
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

const val KEY_TRANSACTION_ID = "transaction_id"

@HiltWorker
class SendMoneyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: BankingRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val txId = inputData.getString(KEY_TRANSACTION_ID)
            ?: return Result.failure(
                buildOutputData("Transaction ID missing")
            )

        // 1. Load the transaction; if gone or already synced, succeed silently.
        val transaction = repository.getTransactionById(txId)
            ?: return Result.success()

        if (transaction.status == TransactionStatus.SYNCED) {
            return Result.success()
        }

        // 2. Mark as SYNCING
        val now = System.currentTimeMillis()
        repository.updateTransaction(
            transaction.copy(
                status         = TransactionStatus.SYNCING,
                lastAttemptAt  = now,
                attemptCount   = transaction.attemptCount + 1
            )
        )

        // 3. Hit the network
        val result = repository.executeSendMoney(transaction)

        return when (result) {
            is Resource.Success -> {
                // 4a. Success → mark SYNCED
                repository.updateTransaction(
                    transaction.copy(
                        status        = TransactionStatus.SYNCED,
                        lastAttemptAt = now,
                        attemptCount  = transaction.attemptCount + 1,
                        lastError     = null
                    )
                )
                Result.success()
            }
            is Resource.Error -> {
                // 4b. Failure → mark FAILED, store error
                repository.updateTransaction(
                    transaction.copy(
                        status        = TransactionStatus.FAILED,
                        lastAttemptAt = now,
                        attemptCount  = transaction.attemptCount + 1,
                        lastError     = result.message
                    )
                )
                // Return failure so WorkManager can apply its retry/backoff policy.
                Result.failure(buildOutputData(result.message ?: "Unknown error"))
            }
            else -> Result.failure(buildOutputData("Unexpected state"))
        }
    }

    private fun buildOutputData(error: String) =
        androidx.work.Data.Builder()
            .putString("error", error)
            .build()
}
