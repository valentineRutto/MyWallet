package com.valentinerutto.mywallet.presentation.statement

import androidx.lifecycle.ViewModel
import com.valentinerutto.mywallet.data.model.StatementEntry
import com.valentinerutto.mywallet.data.repository.BankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class StatementState(
    val entries: List<StatementEntry> = emptyList(),
    val total: Double = 0.0
)

@HiltViewModel
class StatementViewModel @Inject constructor(
    private val repository: BankingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatementState())
    val state: StateFlow<StatementState> = _state.asStateFlow()

    init {
        val entries = repository.getStatementEntries()
        _state.value = StatementState(
            entries = entries,
            total   = entries.sumOf { it.amount }
        )
    }
}
