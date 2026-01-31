package com.valentinerutto.mywallet.data.remote


import com.valentinerutto.mywallet.data.model.BalanceResponse
import com.valentinerutto.mywallet.data.model.Last100StatementResponse
import com.valentinerutto.mywallet.data.model.LoginRequest
import com.valentinerutto.mywallet.data.model.LoginResponse
import com.valentinerutto.mywallet.data.model.SendMoneyRequest
import com.valentinerutto.mywallet.data.model.SendMoneyResponse
import com.valentinerutto.mywallet.data.model.StatementRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BankingApiService {

    @POST("springboot-rest-api/api/v1/customers/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/springboot-rest-api/api/v1/transactions/send-money")
    suspend fun sendMoney(@Body request: SendMoneyRequest): Response<SendMoneyResponse>

    @POST("springboot-rest-api/api/v1/transactions/last-100-transactions")
    suspend fun getLast100Transactions(@Body request: StatementRequest): Response<Last100StatementResponse>
    @POST("/springboot-rest-api/api/v1/accounts/balance")
    suspend fun getBalance(@Body request: StatementRequest): Response<BalanceResponse>
}
