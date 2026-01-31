package com.valentinerutto.mywallet.data.repository

import com.valentinerutto.mywallet.data.local.PreferencesManager
import com.valentinerutto.mywallet.data.local.TransactionDao
import com.valentinerutto.mywallet.data.local.UserProfileDao
import com.valentinerutto.mywallet.data.model.LoginRequest
import com.valentinerutto.mywallet.data.model.SendMoneyRequest
import com.valentinerutto.mywallet.data.model.SendMoneyResponse
import com.valentinerutto.mywallet.data.model.StatementEntry
import com.valentinerutto.mywallet.data.model.Transaction
import com.valentinerutto.mywallet.data.model.User
import com.valentinerutto.mywallet.data.model.toUser
import com.valentinerutto.mywallet.data.model.toUserProfile
import com.valentinerutto.mywallet.data.remote.BankingApiService
import com.valentinerutto.mywallet.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
                userProfileDao.insertUserProfile(userProfile)
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

    suspend fun getCurrentBalance(): Double =
        userProfileDao.getUserProfile().first()?.balance ?: 0.0

    /** Called by the Worker to hit the network. */
    suspend fun executeSendMoney(transaction: Transaction): Resource<SendMoneyResponse> {
        return try {
            val customerId = getCurrentCustomerId()
                ?: return Resource.Error("Not logged in")
            val request = SendMoneyRequest(
                fromCustomerId = customerId,
                toRecipient = transaction.recipientName,
                amount = transaction.amount,
                note = transaction.note
            )
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

    // ── Statement (local seed) ──────────────────────────────────────────────
    fun getStatementEntries(): List<StatementEntry> = listOf(
        StatementEntry("Oct 31", "Payment Received",        2450.00),
        StatementEntry("Oct 30", "Retail Merchant",        -1299.00),
        StatementEntry("Oct 28", "Dining & Drinks",          -86.00),
        StatementEntry("Oct 28", "Transport Services",       -24.30),
        StatementEntry("Oct 25", "Coffee House",             -12.50),
        StatementEntry("Oct 20", "Software Subscription",    -15.99),
        StatementEntry("Oct 18", "Utility Billing",         -142.00)
    )
}
