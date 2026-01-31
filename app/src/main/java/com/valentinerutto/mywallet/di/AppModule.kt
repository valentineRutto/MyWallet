package com.valentinerutto.mywallet.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.valentinerutto.mywallet.data.local.BankingDatabase
import com.valentinerutto.mywallet.data.local.PreferencesManager
import com.valentinerutto.mywallet.data.local.TransactionDao
import com.valentinerutto.mywallet.data.local.UserProfileDao
import com.valentinerutto.mywallet.data.remote.BankingApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.apply
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideBankingApiService(okHttpClient: OkHttpClient, gson: Gson): BankingApiService =
        Retrofit.Builder()
           // .baseUrl("http://192.168.100.20:8092/")
            .baseUrl("http://10.0.2.2:8092/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BankingApiService::class.java)

    @Provides
    @Singleton
    fun provideBankingDatabase(@ApplicationContext context: Context): BankingDatabase =
        Room.databaseBuilder(context, BankingDatabase::class.java, "banking_database")
            .build()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: BankingDatabase): UserProfileDao =
        database.userProfileDao()

    @Provides
    @Singleton
    fun provideTransactionDao(database: BankingDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
