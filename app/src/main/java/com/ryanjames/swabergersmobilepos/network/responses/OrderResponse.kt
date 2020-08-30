package com.ryanjames.swabergersmobilepos.network.responses

import com.google.gson.annotations.SerializedName

data class GetOrderResponse(
    val orderId: String,
    val lineItems: List<GetOrderLineItemResponse>,
    val price: Float,
    val creationDate: String
)

data class GetOrderLineItemResponse(
    val lineItemId: String,
    @SerializedName("unit_price") val unitPrice: Float,
    val price: Float,
    val quantity: Int,
    val lineItemName: String,
    val products: List<GetOrderProductResponse>,
    val menuProduct: ProductDetailsResponse,
    val bundleId: String?
)

data class GetOrderProductResponse(
    val modifierSelections: List<GetOrderModifierSelectionResponse>,
    val productItemId: String,
    val productId: String,
    val productName: String,
    val productGroupId: String
)

data class GetOrderModifierSelectionResponse(
    val productGroupId: String,
    val modifierGroupId: String,
    val priceDelta: Int,
    val modifiers: List<ModifierInfoResponse>
)

data class ProductDetailsResponse(
    val productId: String,
    val productName: String,
    val productDescription: String?,
    val price: Float,
    val receiptText: String,
    val modifierGroups: List<ModifierGroupResponse>,
    val bundles: List<GetOrderMenuBundleResponse>
)

data class GetOrderMenuBundleResponse(
    val bundleId: String?,
    val bundleName: String?,
    val price: Float?,
    val receiptText: String?,
    val baseProduct: String?,
    val productGroups: List<GetOrderMenuProductGroupResponse>?
)

data class GetOrderMenuProductGroupResponse(
    val productGroupId: String?,
    val productGroupName: String?,
    val defaultProduct: String?,
    val options: List<GetOrderProductGroupProductResponse>?,
    val min: Int?,
    val max: Int?
)

data class GetOrderProductGroupProductResponse(
    val modifierGroups: List<ModifierGroupResponse>,
    val productId: String,
    val productName: String,
    val price: Float
)

