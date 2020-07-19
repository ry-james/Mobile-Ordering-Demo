package com.ryanjames.swabergersmobilepos.network.retrofit

import com.ryanjames.swabergersmobilepos.network.responses.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SwabergersApi {

    @GET("/modifiers")
    fun getModifierInfos(): Single<ModifierInfosResponse>

    @POST("/login")
    fun login(@Body loginRequestBody: LoginRequestBody): Single<LoginResponse>

    @POST("/refresh")
    fun refresh(): Single<RefreshTokenResponse>

    @GET("/menu")
    fun getMenu(): Single<MenuResponse>

    @POST("/order")
    fun postOrder(@Body orderBody: OrderBody): Single<OrderBody>

    @GET("/ordersummary")
    fun getOrderHistory(): Single<OrderHistoryResponse>
}