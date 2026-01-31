package com.valentinerutto.mywallet.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

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

data class User(
    val customerId: String?,
    val fullName: String?,
    val email: String?,
    val accountNumber: String?,
    val balance: Double?
)

enum class TransactionStatus {
    QUEUED,
    SYNCING,
    SYNCED,
    FAILED
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String,
    val accountTo: String,
    val amount: Double,
    val accountFrom: String,
    val status: TransactionStatus,
    val createdAt: Long,
    val lastAttemptAt: Long = 0L,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val workManagerRequestId: String? = null
)

data class SendMoneyRequest(
    @SerializedName("customerId") val customerId: String,
    @SerializedName("accountFrom")    val accountFrom: String,
    @SerializedName("amount")         val amount: Double,
    @SerializedName("accountTo")           val accountTo: String
)

data class SendMoneyResponse(
    @SerializedName("response_status")        val status: String,
    @SerializedName("response_message")       val message: String
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
