package com.valentinerutto.mywallet.data.repository

import android.widget.Toast
import androidx.compose.runtime.State
import com.valentinerutto.mywallet.data.local.PreferencesManager
import com.valentinerutto.mywallet.data.local.TransactionDao
import com.valentinerutto.mywallet.data.local.UserProfileDao
import com.valentinerutto.mywallet.data.model.BalanceResponse
import com.valentinerutto.mywallet.data.model.LoginRequest
import com.valentinerutto.mywallet.data.model.Mappers.toStatement
import com.valentinerutto.mywallet.data.model.Mappers.toUser
import com.valentinerutto.mywallet.data.model.Mappers.toUserProfile
import com.valentinerutto.mywallet.data.model.SendMoneyRequest
import com.valentinerutto.mywallet.data.model.SendMoneyResponse
import com.valentinerutto.mywallet.data.model.StatementEntry
import com.valentinerutto.mywallet.data.model.StatementRequest
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.User
import com.valentinerutto.mywallet.data.remote.BankingApiService
import com.valentinerutto.mywallet.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.internal.platform.PlatformRegistry.applicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BankingRepository @Inject constructor(
    private val apiService: BankingApiService,
    private val userProfileDao: UserProfileDao,
    private val transactionDao: TransactionDao,
    private val preferencesManager: PreferencesManager
) {

    // ── Login ───────────────────────────────────────────────────────────────
    fun login(customerId: String, pin: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.login(LoginRequest(customerId, pin))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!

                val userProfile = loginResponse.toUserProfile(pin)

                userProfileDao.insertUserProfile(userProfile!!)
                preferencesManager.setLoggedIn(true, customerId)

                emit(Resource.Success(userProfile.toUser()))

            } else {
                val msg = when (response.code()) {
                    401 -> "Invalid customer ID or PIN"
                    404 -> "Customer not found"
                    500 -> "Server error. Please try again later"
                    else -> "Login failed: ${response.message()}"
                }
                emit(Resource.Error(msg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }


    fun getlast100Transactions(customerId: String): Flow<Resource<List<StatementEntry>>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getLast100Transactions(StatementRequest(customerId))

            if (response.isSuccessful && response.body() != null) {
                val statements = response.body()!!.map { it.toStatement() }

                preferencesManager.setLoggedIn(true, customerId)

                emit(Resource.Success(statements))
            } else {
                val msg = when (response.code()) {
                    401 -> "Invalid customer ID or PIN"
                    404 -> "Customer not found"
                    500 -> "Server error. Please try again later"
                    else -> "Transaction fetch failed: ${response.message()}"
                }
                emit(Resource.Error(msg))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error. Check your internet"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }

    fun getBalance(customerId: String): Flow<Resource<BalanceResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = apiService.getBalance(StatementRequest(customerId))

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val msg = when (response.code()) {
                    404 -> "Customer not found"
                    500 -> "Server error. Please try again"
                    else -> "Failed to fetch balance: ${response.message()}"
                }
                emit(Resource.Error(msg))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error. Check your internet"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.localizedMessage}"))
        }
    }
    fun getUserProfile(): Flow<User?> =
        userProfileDao.getUserProfile().map { it?.toUser() }

    fun isLoggedIn(): Flow<Boolean> = preferencesManager.isLoggedIn

    suspend fun logout() {
        transactionDao.deleteAll()
        userProfileDao.deleteAllProfiles()
        preferencesManager.logout()
    }

    // ── Transactions ────────────────────────────────────────────────────────
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun getTransactionById(id: String): Transaction? =
        transactionDao.getById(id)

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    suspend fun getCurrentCustomerId(): String? =
        preferencesManager.customerId.first()

  suspend fun getCurrentCustomerAcc(): String? =
       userProfileDao.getUserProfile().first()?.accountNo


    suspend fun getCurrentBalance(): Double =
        userProfileDao.getUserProfile().first()?.balance ?: 0.0

    /** Called by the Worker to hit the network. */
    suspend fun executeSendMoney(transaction: Transaction): Resource<SendMoneyResponse> {
        return try {
            val customerId = getCurrentCustomerId()
                ?: return Resource.Error("Not logged in")

            val request = SendMoneyRequest(
                customerId = customerId,
                accountTo = transaction.accountTo,
                amount = transaction.amount,
                accountFrom = transaction.accountFrom
            )
            Toast.makeText(applicationContext, "Transaction synced!${transaction.amount}", Toast.LENGTH_SHORT).show()

            val response = apiService.sendMoney(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val msg = when (response.code()) {
                    400 -> "Bad request – check amount or recipient"
                    401 -> "Session expired – please log in again"
                    402 -> "Insufficient funds on server"
                    else -> "Send failed (${response.code()}): ${response.message()}"
                }
                Resource.Error(msg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

}
