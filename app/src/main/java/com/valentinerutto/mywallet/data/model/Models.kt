package com.valentinerutto.mywallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Network request model
data class LoginRequest(
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("pin")
    val pin: String
)

// Network response model
data class LoginResponse(
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("accountNumber")
    val accountNumber: String,
    @SerializedName("balance")
    val balance: Double,
    @SerializedName("memberSince")
    val memberSince: String,
    @SerializedName("message")
    val message: String? = null
)

// Room entity for local storage
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val customerId: String,
    val fullName: String,
    val email: String,
    val accountNumber: String,
    val balance: Double,
    val memberSince: String,
    val pin: String
)

// UI state model
data class User(
    val customerId: String,
    val fullName: String,
    val email: String,
    val accountNumber: String,
    val balance: Double,
    val memberSince: String
)

// Convert extension functions
fun LoginResponse.toUserProfile(pin: String) = UserProfile(
    customerId = customerId,
    fullName = fullName,
    email = email,
    accountNumber = accountNumber,
    balance = balance,
    memberSince = memberSince,
    pin = pin
)

fun UserProfile.toUser() = User(
    customerId = customerId,
    fullName = fullName,
    email = email,
    accountNumber = accountNumber,
    balance = balance,
    memberSince = memberSince
)

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

// ─── Statement entry (dummy local data) ─────────────────────────────────────
data class StatementEntry(
    val date: String,       // e.g. "Oct 31"
    val description: String,
    val amount: Double      // negative = debit
)
