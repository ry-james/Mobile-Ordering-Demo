package com.ryanjames.swabergersmobilepos.network.responses

import com.google.gson.annotations.SerializedName

data class OrderBody(
    val orderId: String,
    val lineItems: List<LineItemRequestBody>
)

data class LineItemRequestBody(
    val lineItemId: String,
    val lineItemName: String,
    val quantity: Int,
    val price: Float,
    @SerializedName("unit_price") val unitPrice: Float,
    val products: List<ProductInOrderRequestBody>,
    val baseProduct: String
)

data class ProductInOrderRequestBody(
    val productItemId: String,
    val productId: String,
    val modifierSelections: List<ModifierSelectionRequestBody>
)

data class ModifierSelectionRequestBody(
    val productGroupId: String,
    val modifierGroupId: String,
    val priceDelta: Float,
    val items: List<String>
)

data class OrderHistoryResponse(val orders: List<OrderResponse>)

data class OrderResponse(
    val orderId: String,
    val lineItems: List<LineItemResponse>,
    val price: Float
)

data class LineItemResponse(
    val lineItemId: String,
    val lineItemName: String,
    val quantity: Int,
    val price: Float,
    @SerializedName("unit_price") val unitPrice: Float,
    val products: List<ProductInOrderResponse>,
    val baseProduct: String
)

data class ProductInOrderResponse(
    val productItemId: String,
    val productId: String,
    val modifierSelections: List<ModifierSelectionResponse>
)

data class ModifierSelectionResponse(
    val productGroupId: String,
    val modifierGroupId: String,
    val priceDelta: Float,
    val items: List<String>
)