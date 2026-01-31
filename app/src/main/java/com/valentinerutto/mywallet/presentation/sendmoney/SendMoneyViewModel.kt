package com.valentinerutto.mywallet.presentation.sendmoney

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
import java.util.UUID
import javax.inject.Inject

data class SendMoneyState(
    val balance: Double = 0.0,
    val isLoading: Boolean = true,
    /** Set to true the instant the work request is enqueued; UI navigates back. */
    val queued: Boolean = false
)

@HiltViewModel
class SendMoneyViewModel @Inject constructor(
    private val repository: BankingRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(SendMoneyState())
    val state: StateFlow<SendMoneyState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val balance = repository.getCurrentBalance()
            _state.value = SendMoneyState(balance = balance, isLoading = false)
        }
    }

    fun sendMoney(accountTo: String, amount: Double) {
        viewModelScope.launch {
            val txId = UUID.randomUUID().toString()
            val now  = System.currentTimeMillis()
            val customerAccount = repository.getCurrentCustomerAcc()

            // 1. Persist locally as QUEUED
            val transaction = Transaction(
                id = txId,
                accountTo = accountTo,
                amount = amount,
                accountFrom = customerAccount.toString(),
                status = TransactionStatus.QUEUED,
                createdAt = now,
                workManagerRequestId = null
            )
            repository.insertTransaction(transaction)

            // 2. Build & enqueue a WorkManager request (requires network)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SendMoneyWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_TRANSACTION_ID to txId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    Duration.ofSeconds(10)
                )
                .build()

            workManager.enqueue(workRequest)

            // 3. Store the WorkManager request ID back into the row
            repository.updateTransaction(
                transaction.copy(workManagerRequestId = workRequest.id.toString())
            )

            // 4. Signal UI that we're done – navigate back
            _state.value = _state.value.copy(queued = true)
        }
    }
}
