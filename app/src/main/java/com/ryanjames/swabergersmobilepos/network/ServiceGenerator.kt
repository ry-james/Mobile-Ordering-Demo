package com.ryanjames.swabergersmobilepos.network

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {

    //    val API_BASE_URL = "https://test-swabergers.herokuapp.com/"
    val API_BASE_URL = "http://10.0.2.2:5000"
//    val API_BASE_URL = "http://192.168.1.234:5000"

    val builder = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //NOTE: Jon Bott - Add this line for RxRetrofit

    fun <S> createService(serviceClass: Class<S>): S {
        return createService(serviceClass, null)
    }

    private fun <S> createService(serviceClass: Class<S>, authToken: String?): S {
        if (authToken != null) {
            addRequestHeaders(authToken)
        }
        // val client = httpClient.authenticator(TokenAuthenticator()).build()
        // val retrofit = builder.client(client).build()
        return builder.build().create(serviceClass)
    }

    private fun addRequestHeaders(authToken: String?) {
        OkHttpClient.Builder().interceptors().add(Interceptor { chain ->
            val original = chain.request()

            // Request customization: add request headers
            val requestBuilder =
                original.newBuilder().header("Authorization", authToken).method(original.method(), original.body())

            val request = requestBuilder.build()
            chain.proceed(request)
        })
    }
}