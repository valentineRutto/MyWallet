package com.valentinerutto.mywallet.data.remote


import com.valentinerutto.mywallet.data.model.LoginRequest
import com.valentinerutto.mywallet.data.model.LoginResponse
import com.valentinerutto.mywallet.data.model.SendMoneyRequest
import com.valentinerutto.mywallet.data.model.SendMoneyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BankingApiService {

    @POST("springboot-rest-api/api/v1/customers/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("springboot-rest-api/api/v1/customers/send-money")
    suspend fun sendMoney(@Body request: SendMoneyRequest): Response<SendMoneyResponse>
}
