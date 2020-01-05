package com.ryanjames.swabergersmobilepos.network.retrofit.interceptors

import android.content.SharedPreferences
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import okhttp3.Interceptor
import okhttp3.Response

class RefreshAuthTokenInterceptor(val sharedPreferences: SharedPreferences) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val refreshToken = sharedPreferences.getString(SharedPrefsKeys.KEY_REFRESH_TOKEN, null)
        val bearerAuthToken = "Bearer $refreshToken"

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", bearerAuthToken)
            .build()

        return chain.proceed(newRequest)

    }
}