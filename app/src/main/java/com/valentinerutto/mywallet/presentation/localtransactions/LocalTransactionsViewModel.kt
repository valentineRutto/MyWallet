package com.valentinerutto.mywallet.presentation.localtransactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.TransactionStatus
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.worker.KEY_TRANSACTION_ID
import com.valentinerutto.mywallet.worker.SendMoneyWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

data class LocalTransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val pendingCount: Int = 0
)

@HiltViewModel
class LocalTransactionsViewModel @Inject constructor(
    private val repository: BankingRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(LocalTransactionsState())
    val state: StateFlow<LocalTransactionsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTransactions().collect { list ->
                _state.value = LocalTransactionsState(
                    transactions = list,
                    pendingCount = list.count {
                        it.status == TransactionStatus.QUEUED ||
                        it.status == TransactionStatus.SYNCING ||
                        it.status == TransactionStatus.FAILED
                    }
                )
            }
        }
    }

    /** Re-enqueue a single FAILED transaction. */
    fun retry(transaction: Transaction) {
        viewModelScope.launch {
            // Reset to QUEUED so the worker picks it up fresh
            repository.updateTransaction(
                transaction.copy(status = TransactionStatus.QUEUED, lastError = null)
            )
            enqueueWork(transaction.id)
        }
    }

    /** Enqueue work for every QUEUED or FAILED transaction. */
    fun syncAll() {
        viewModelScope.launch {
            _state.value.transactions
                .filter { it.status == TransactionStatus.QUEUED || it.status == TransactionStatus.FAILED }
                .forEach { tx ->
                    if (tx.status == TransactionStatus.FAILED) {
                        repository.updateTransaction(
                            tx.copy(status = TransactionStatus.QUEUED, lastError = null)
                        )
                    }
                    enqueueWork(tx.id)
                }
        }
    }

    private fun enqueueWork(txId: String) {
        val constraints = Constraints.Builder()
            .setRequiresNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SendMoneyWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(KEY_TRANSACTION_ID to txId))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofSeconds(10))
            .build()

        workManager.enqueue(workRequest)
    }
}
