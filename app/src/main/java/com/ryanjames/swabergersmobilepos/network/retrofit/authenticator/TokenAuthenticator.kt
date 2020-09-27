package com.ryanjames.swabergersmobilepos.network.retrofit.authenticator

import android.content.SharedPreferences
import android.util.Log
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import com.ryanjames.swabergersmobilepos.network.retrofit.MobilePosApi
import com.ryanjames.swabergersmobilepos.network.retrofit.interceptors.RefreshAuthTokenInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit

class TokenAuthenticator(
    val sharedPreferences: SharedPreferences,
    val retrofit: Retrofit.Builder
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.code() == 401 && response.request().header("No-Authentication") == null) {

            Log.d("401", response.body().toString())

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(RefreshAuthTokenInterceptor(sharedPreferences))
                .apply {
                    val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                    addInterceptor(loggingInterceptor)
                }.build()

            val retrofit = retrofit.client(client).build()
            val tokenApiClient = retrofit.create<MobilePosApi>()

            val refreshTokenResponse = tokenApiClient.refresh().blockingGet()
            val newAccessToken = refreshTokenResponse.accessToken

            if (refreshTokenResponse != null) {
                with(sharedPreferences.edit()) {
                    putString(SharedPrefsKeys.KEY_AUTH_TOKEN, newAccessToken)
                    apply()
                }
            }

            val newBearerToken = "Bearer $newAccessToken"
            return response.request().newBuilder()
                .header("Authorization", newBearerToken).build()
        }
        return null

    }
}