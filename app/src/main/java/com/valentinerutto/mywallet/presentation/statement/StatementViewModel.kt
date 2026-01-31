package com.valentinerutto.mywallet.presentation.statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.mywallet.data.model.StatementEntry
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatementState(
    val isLoading: Boolean = false,
    val entries: List<StatementEntry> = emptyList(),
    val total: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class StatementViewModel @Inject constructor(
    private val repository: BankingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatementState())
    val state: StateFlow<StatementState> = _state.asStateFlow()



    fun loadTransactions(customerId: String) {
        viewModelScope.launch {
            repository.getlast100Transactions(customerId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _state.value = StatementState(isLoading = true)
                    }
                    is Resource.Success -> {
                        val entries = resource.data ?: emptyList()
                        _state.value = StatementState(
                            isLoading = false,
                            entries = entries,
                            total = entries.sumOf { it.amount ?: 0.0 },
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = StatementState(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }


}
