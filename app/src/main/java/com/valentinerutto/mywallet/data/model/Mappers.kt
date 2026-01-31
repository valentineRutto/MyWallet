package com.valentinerutto.mywallet.data.model


object Mappers {
    fun LoginResponse.toUserProfile(pin: String) = customerId?.let {
        UserProfile(
            customerId = it,
            customerName = customerName,
            email = email,
            accountNo  = customerAccount?.accountNo,
            balance = customerAccount?.balance,
            pin = pin
        )
    }

    fun UserProfile.toUser() = User(
        customerId = customerId,
        fullName = customerName,
        email = email,
        accountNumber = accountNo,
        balance = balance)

    fun Last100StatementResponse.map() = map { it.toStatement() }

    fun Last100StatementResponseItem.toStatement() = StatementEntry(
        transactionId = transactionId,
        accountNo = accountNo ?: "",
        debitOrCredit = debitOrCredit ?: "",
        transactionType = transactionType ?: "",
        amount = amount ?: 0.0
    )



}