package com.valentinerutto.mywallet.presentation.login

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

data class LoginState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: BankingRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    fun login(customerId: String, pin: String) {
        viewModelScope.launch {
            repository.login(customerId, pin).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _loginState.value = LoginState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _loginState.value = LoginState(
                            isLoading = false,
                            user = result.data,
                            isSuccess = true
                        )
                    }
                    is Resource.Error -> {
                        _loginState.value = LoginState(
                            isLoading = false,
                            error = result.message ?: "Unknown error occurred"
                        )
                    }
                }
            }
        }
    }
    
    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
    }
}
