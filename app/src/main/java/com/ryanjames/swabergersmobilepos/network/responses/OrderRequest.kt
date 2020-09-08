package com.ryanjames.swabergersmobilepos.network.responses

data class CreateUpdateOrderRequest(
    val orderId: String,
    val lineItems: List<LineItemRequestBody>
)

data class LineItemRequestBody(
    val lineItemId: String,
    val quantity: Int,
    val products: List<ProductInOrderRequestBody>,
    val baseProduct: String,
    val bundleId: String?
)

data class ProductInOrderRequestBody(
    val productItemId: String,
    val productId: String,
    val modifierSelections: List<ModifierSelectionRequestBody>,
    val productGroupId: String
)

data class ModifierSelectionRequestBody(
    val modifierGroupId: String,
    val items: List<String>
)

data class GetOrderRequest(
    val orderId: String
)