package com.valentinerutto.mywallet.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceResponse(
    @SerialName("accountNo")
    val accountNo: String?,
    @SerialName("balance")
    val balance: Double?
)