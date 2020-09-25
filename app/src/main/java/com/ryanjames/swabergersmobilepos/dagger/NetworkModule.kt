package com.ryanjames.swabergersmobilepos.dagger

import android.content.SharedPreferences
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import com.ryanjames.swabergersmobilepos.network.retrofit.MobilePosApi
import com.ryanjames.swabergersmobilepos.network.retrofit.TokenAuthenticator
import com.ryanjames.swabergersmobilepos.network.retrofit.interceptors.AuthTokenInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
open class NetworkModule {

    @Singleton
    @Provides
    open fun provideSwabergersService(sharedPreferences: SharedPreferences, mobilePosApi: MobilePosApi): ApiService {
        return ApiService(sharedPreferences, mobilePosApi)
    }

    @Singleton
    @Provides
    open fun provideRetrofitBuilder(): Retrofit.Builder {
        val apiBaseUrl = "https://test-swabergers.herokuapp.com/"
        return Retrofit.Builder()
            .baseUrl(apiBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    }

    @Singleton
    @Provides
    open fun provideTokenAuthenticator(sharedPreferences: SharedPreferences, retrofit: Retrofit.Builder): TokenAuthenticator {
        return TokenAuthenticator(sharedPreferences, retrofit)
    }

    @Singleton
    @Provides
    open fun provideAuthTokenInterceptor(sharedPreferences: SharedPreferences): AuthTokenInterceptor {
        return AuthTokenInterceptor(sharedPreferences)
    }

    @Singleton
    @Provides
    open fun provideMobilePosApi(
        okHttpClientBuilder: OkHttpClient.Builder,
        authTokenInterceptor: AuthTokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        retrofitBuilder: Retrofit.Builder
    ): MobilePosApi {
        val httpClientBuilder = okHttpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authTokenInterceptor)
            .authenticator(tokenAuthenticator)

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClientBuilder.addInterceptor(loggingInterceptor)

        val client = httpClientBuilder.build()

        val retrofit = retrofitBuilder.client(client).build()
        return retrofit.create(MobilePosApi::class.java)
    }


    @Singleton
    @Provides
    open fun provideOkHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
    }


}