package com.valentinerutto.mywallet.presentation.sendmoney

import android.util.Log
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
import com.valentinerutto.mywallet.data.worker.KEY_TRANSACTION_ID
import com.valentinerutto.mywallet.data.worker.SendMoneyWorker
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

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SendMoneyWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_TRANSACTION_ID to txId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    Duration.ofSeconds(10)
                ).addTag("send_money_$txId")
                .build()

            workManager.enqueue(workRequest)

            workManager.getWorkInfoByIdLiveData(workRequest.id).observeForever { workInfo ->
                workInfo?.let {
                    Log.d("SendMoneyVM", "Work ${workRequest.id} state: ${it.state}")
                    Log.d("SendMoneyVM", "Output: ${it.outputData}")
                }
            }

            repository.updateTransaction(
                transaction.copy(workManagerRequestId = workRequest.id.toString())
            )

            _state.value = _state.value.copy(queued = true)
        }
    }
}
