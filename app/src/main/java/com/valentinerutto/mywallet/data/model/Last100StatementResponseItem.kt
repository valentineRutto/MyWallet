package com.valentinerutto.mywallet.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Last100StatementResponseItem(
    @SerialName("accountNo")
    val accountNo: String?,
    @SerialName("amount")
    val amount: Double?,
    @SerialName("balance")
    val balance: Double?,
    @SerialName("customerId")
    val customerId: String?,
    @SerialName("debitOrCredit")
    val debitOrCredit: String?,
    @SerialName("id")
    val id: Int?,
    @SerialName("transactionId")
    val transactionId: String?,
    @SerialName("transactionType")
    val transactionType: String?
)