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

    @DELETE("/cancelorder/{order_id}")
    fun cancelOrder(@Path(value = "order_id", encoded = true) orderId: String): Single<Any>

    @GET("/stores")
    fun getStores(): Single<VenueListResponse>

    @GET("/home")
    fun getFeaturedStores(): Single<HomeResponse>

    @GET("/basicmenu/{store_id}")
    fun getBasicMenuByStore(@Path(value = "store_id", encoded = true) storeId: String): Single<BasicMenuResponse>

    @GET("/store/{store_id}")
    fun getStoreById(@Path(value = "store_id", encoded = true) storeId: String): Single<VenueResponse>
}