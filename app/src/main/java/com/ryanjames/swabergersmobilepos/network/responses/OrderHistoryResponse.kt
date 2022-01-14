package com.ryanjames.swabergersmobilepos.network.responses

import com.google.gson.annotations.SerializedName

data class OrderHistoryResponse(val orders: List<OrderHistoryItemResponse>)

data class OrderHistoryItemResponse(
    val orderId: String,
    val lineItems: List<LineItemResponse>,
    val price: Float,
    val creationDate: String,
    val status: String,
    val customerName: String?,
    val storeName: String,
    val storeId: String,
    val img: String?
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
