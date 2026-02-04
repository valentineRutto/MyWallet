package com.valentinerutto.mywallet.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.valentinerutto.mywallet.data.model.TransactionStatus
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

const val KEY_TRANSACTION_ID = "transaction_id"

@HiltWorker
class SendMoneyWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val repository: BankingRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val txId = inputData.getString(KEY_TRANSACTION_ID)
            ?: return Result.failure(
                buildOutputData("Transaction ID missing")
            )

        val transaction = repository.getTransactionById(txId)
            ?: return Result.success()
        Log.d("SendMoneyWorker", "Transaction synced: ${transaction.amount}")

        if (transaction.status == TransactionStatus.SYNCED) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        repository.updateTransaction(
            transaction.copy(
                status         = TransactionStatus.SYNCING,
                lastAttemptAt  = now,
                attemptCount   = transaction.attemptCount + 1
            )
        )

        val result = repository.executeSendMoney(transaction)

        return when (result) {
            is Resource.Success -> {
                repository.updateTransaction(
                    transaction.copy(
                        status        = TransactionStatus.SYNCED,
                        lastAttemptAt = now,
                        attemptCount  = transaction.attemptCount + 1,
                        lastError     = null
                    )
                )

                Result.success(buildOutputData("Transaction synced successfully"))

            }
            is Resource.Error -> {

                repository.updateTransaction(
                    transaction.copy(
                        status        = TransactionStatus.FAILED,
                        lastAttemptAt = now,
                        attemptCount  = transaction.attemptCount + 1,
                        lastError     = result.message
                    )
                )

                Log.d("SendMoneyWorker", "Transaction failed: ${result.message} ,  ${result.data?.message},  ${result.data?.status}")

                Result.failure(buildOutputData(
                    (result.message + result.data?.message) ?: "Unknown error"
                ))
            }
            else -> Result.failure(buildOutputData("Unexpected state"))
        }
    }

    private fun buildOutputData(error: String) =
        Data.Builder()
            .putString("error", error)
            .build()
}
