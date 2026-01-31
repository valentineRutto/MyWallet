package com.valentinerutto.mywallet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentinerutto.mywallet.data.model.User
import com.valentinerutto.mywallet.data.repository.BankingRepository
import com.valentinerutto.mywallet.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = true
)

data class BalanceState(
    val isLoading: Boolean = false,
    val balance: Double? = null,
    val accountNo: String? = null,
    val customerName: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BankingRepository
) : ViewModel() {
    
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            repository.getUserProfile().collect { user ->
                _homeState.value = HomeState(
                    user = user,
                    isLoading = false
                )
            }
        }
    }
    
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onLogoutComplete()
        }
    }


    private val _balanceState = MutableStateFlow(BalanceState())
    val balanceState: StateFlow<BalanceState> = _balanceState.asStateFlow()

    private val _showBalanceSheet = MutableStateFlow(false)
    val showBalanceSheet: StateFlow<Boolean> = _showBalanceSheet.asStateFlow()

    fun checkBalance(customerId: String?) {
        _showBalanceSheet.value = true
        viewModelScope.launch {
            if (customerId != null) {
                repository.getBalance(customerId).collect { resource ->
                    _balanceState.value = when (resource) {
                        is Resource.Loading -> BalanceState(isLoading = true)
                        is Resource.Success -> {
                            val data = resource.data
                            BalanceState(
                                balance = data?.balance,
                                accountNo = data?.accountNo,
                            )
                        }

                        is Resource.Error -> BalanceState(error = resource.message)
                    }
                }
            }
        }
    }

    fun hideBalanceSheet() {
        _showBalanceSheet.value = false
        _balanceState.value = BalanceState()
    }
}
