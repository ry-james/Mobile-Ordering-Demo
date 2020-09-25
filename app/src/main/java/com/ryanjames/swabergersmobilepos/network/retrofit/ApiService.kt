package com.ryanjames.swabergersmobilepos.network.retrofit

import android.content.SharedPreferences
import android.util.Log
import com.ryanjames.swabergersmobilepos.helper.SharedPrefsKeys
import com.ryanjames.swabergersmobilepos.network.responses.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ApiService(
    private val sharedPrefs: SharedPreferences,
    private val mobilePosApi: MobilePosApi
) {

    fun getMenu(): Single<MenuResponse> {
        return mobilePosApi.getMenu()
    }

    fun getBasicMenu(): Single<BasicMenuResponse> {
        return mobilePosApi.getBasicMenu()
    }

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        val loginRequestBody = LoginRequestBody(username, password)
        return mobilePosApi.login(loginRequestBody)
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

    fun postOrder(createUpdateOrderRequest: CreateUpdateOrderRequest): Single<GetOrderResponse> {
        return mobilePosApi.postOrder(createUpdateOrderRequest)
    }

    fun putOrder(createUpdateOrderRequest: CreateUpdateOrderRequest): Single<GetOrderResponse> {
        return mobilePosApi.putOrder(createUpdateOrderRequest)
    }

    fun getProductDetails(productId: String): Single<ProductDetailsResponse> {
        return mobilePosApi.getProductDetails(productId)
    }


    fun getOrderHistory(): Single<OrderHistoryResponse> {
        return mobilePosApi.getOrderHistory()
    }

    fun getOrderById(orderId: String): Single<GetOrderResponse> {
        val getOrderRequest = GetOrderRequest(orderId)
        return mobilePosApi.getOrder(getOrderRequest)
    }
}