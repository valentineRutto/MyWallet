package com.valentinerutto.mywallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

// Network request model
data class LoginRequest(
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("pin")
    val pin: String
)

data class LoginResponse(
    @SerialName("customerAccount")
    val customerAccount: CustomerAccount?,
    @SerialName("customerId")
    val customerId: String?,
    @SerialName("customerName")
    val customerName: String?,
    @SerialName("email")
    val email: String?
)
data class CustomerAccount(
    @SerialName("accountNo")
    val accountNo: String?,
    @SerialName("balance")
    val balance: Double?,

)

// Room entity for local storage
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
val customerId: String,
val customerName: String?,
val email: String?,
val accountNo: String?,
val balance: Double?,
    val pin: String
)

// UI state model
data class User(
    val customerId: String?,
    val fullName: String?,
    val email: String?,
    val accountNumber: String?,
    val balance: Double?
)

// Convert extension functions

// ─── Transaction sync status ────────────────────────────────────────────────
enum class TransactionStatus {
    QUEUED,
    SYNCING,
    SYNCED,
    FAILED
}

// ─── Room entity for local transactions ─────────────────────────────────────
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String,                          // UUID generated locally
    val recipientName: String,               // free-text "To" field
    val amount: Double,
    val note: String,                        // "For" / memo field
    val status: TransactionStatus,
    val createdAt: Long,                     // epoch millis
    val lastAttemptAt: Long = 0L,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val workManagerRequestId: String? = null // UUID of the enqueued WorkManager request
)

// ─── Network models for send-money ──────────────────────────────────────────
data class SendMoneyRequest(
    @SerializedName("fromCustomerId") val fromCustomerId: String,
    @SerializedName("toRecipient")    val toRecipient: String,
    @SerializedName("amount")         val amount: Double,
    @SerializedName("note")           val note: String
)

data class SendMoneyResponse(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("status")        val status: String,
    @SerializedName("message")       val message: String
)

data class StatementRequest(
    @SerializedName("customerId")
    val customerId: String
)

data class StatementEntry(

 val transactionId:String?,
    val accountNo:String?,
    val debitOrCredit :String?,
    val transactionType:String?,
   val amount:Double?
)
