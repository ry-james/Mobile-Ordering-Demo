package com.ryanjames.swabergersmobilepos.network.retrofit

import com.ryanjames.swabergersmobilepos.network.responses.*
import io.reactivex.Single
import retrofit2.http.*

interface MobilePosApi {

    @POST("/login")
    @Headers("No-Authentication: true")
    fun login(@Body loginRequestBody: LoginRequestBody): Single<LoginResponse>

    @POST("/refresh")
    fun refresh(): Single<RefreshTokenResponse>

    @GET("/menu")
    fun getMenu(): Single<MenuResponse>

    @POST("/order")
    fun postOrder(@Body createUpdateOrderRequest: CreateUpdateOrderRequest): Single<GetOrderResponse>

    @PUT("/order")
    fun putOrder(@Body createUpdateOrderRequest: CreateUpdateOrderRequest): Single<GetOrderResponse>

    @GET("/ordersummary")
    fun getOrderHistory(): Single<OrderHistoryResponse>

    @POST("/retrieveOrder")
    fun getOrder(@Body getOrderRequest: GetOrderRequest): Single<GetOrderResponse>

    @GET("/productDetail/{product_id}")
    fun getProductDetails(@Path(value = "product_id", encoded = true) productId: String): Single<ProductDetailsResponse>

    @GET("/basicMenu")
    fun getBasicMenu(): Single<BasicMenuResponse>
}