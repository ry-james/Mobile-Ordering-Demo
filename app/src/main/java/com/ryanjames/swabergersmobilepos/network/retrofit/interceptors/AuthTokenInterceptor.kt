package com.ryanjames.swabergersmobilepos.network.retrofit.interceptors

import android.content.SharedPreferences
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(val sharedPreferences: SharedPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authToken = sharedPreferences.getString(SharedPrefsKeys.KEY_AUTH_TOKEN, null)
        val bearerAuthToken = "Bearer $authToken"

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", bearerAuthToken)
            .build()

        return chain.proceed(newRequest)

    }
}