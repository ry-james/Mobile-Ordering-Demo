package com.ryanjames.swabergersmobilepos.network.retrofit

import android.content.SharedPreferences
import android.util.Log
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import com.ryanjames.swabergersmobilepos.network.ServiceGenerator
import com.ryanjames.swabergersmobilepos.network.retrofit.interceptors.RefreshAuthTokenInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.create

class TokenAuthenticator(
    val sharedPreferences: SharedPreferences
) : Authenticator {


    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code() == 401) {

            Log.d("401", response.body().toString())

            val httpClientBuilder = OkHttpClient.Builder()
            httpClientBuilder.addInterceptor(RefreshAuthTokenInterceptor(sharedPreferences))


            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClientBuilder.addInterceptor(loggingInterceptor)
            val client = httpClientBuilder.build()

            val retrofit = ServiceGenerator.builder.client(client).build()
            val tokenApiClient = retrofit.create<SwabergersApi>()

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