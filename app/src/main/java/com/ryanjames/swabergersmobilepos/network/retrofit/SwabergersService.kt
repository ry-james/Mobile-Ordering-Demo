package com.ryanjames.swabergersmobilepos.network.retrofit

import android.content.SharedPreferences
import android.util.Log
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import com.ryanjames.swabergersmobilepos.network.ServiceGenerator
import com.ryanjames.swabergersmobilepos.network.responses.LoginRequestBody
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.network.responses.MenuResponse
import com.ryanjames.swabergersmobilepos.network.responses.ModifierInfosResponse
import com.ryanjames.swabergersmobilepos.network.retrofit.interceptors.AuthTokenInterceptor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class SwabergersService(private val sharedPrefs: SharedPreferences) {

    private fun createService(withAuth: Boolean = true): SwabergersApi {

        val httpClientBuilder = OkHttpClient.Builder()

        if (withAuth) {
            httpClientBuilder.addInterceptor(AuthTokenInterceptor(sharedPrefs))
                .authenticator(TokenAuthenticator(sharedPrefs))
        }

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        httpClientBuilder.addInterceptor(loggingInterceptor)

        val client = httpClientBuilder.build()

        val retrofit = ServiceGenerator.builder.client(client).build()
        return retrofit.create(SwabergersApi::class.java)
    }


    fun getModifierInfos(): Single<ModifierInfosResponse> {
        return createService().getModifierInfos()
    }

    fun getMenu(): Single<MenuResponse> {
        return createService().getMenu()
    }

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        val loginRequestBody = LoginRequestBody(username, password)
        return createService(withAuth = false).login(loginRequestBody)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { loginResponse: LoginResponse ->
                with(sharedPrefs.edit()) {
                    putString(SharedPrefsKeys.KEY_AUTH_TOKEN, loginResponse.accessToken)
                    putString(SharedPrefsKeys.KEY_REFRESH_TOKEN, loginResponse.refreshToken)
                    apply()
                }
            }.doOnError { error ->
                Log.e("ERROR", error.message, error)
            }
    }
}