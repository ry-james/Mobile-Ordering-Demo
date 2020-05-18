package com.ryanjames.swabergersmobilepos.network.responses

import com.google.gson.annotations.SerializedName

data class MenuResponse(
    val categories: List<CategoryResponse>?,
    val products: List<ProductResponse>?,
    val bundles: List<BundleResponse>?,
    val modifierGroups: List<ModifierGroupResponse>?,
    val productGroups: List<ProductGroupResponse>?
)

data class CategoryResponse(
    val categoryId: String?,
    val categoryName: String?,
    val products: List<String>?
)

data class ProductResponse(
    val productId: String?,
    val productName: String?,
    val productDescription: String?,
    val price: Float?,
    val receiptText: String?,
    val categoryId: String?,
    val bundles: List<String>?,
    val modifierGroups: List<String>?
)

data class BundleResponse(
    val bundleId: String?,
    val bundleName: String?,
    val price: Float?,
    val receiptText: String?,
    val baseProduct: String?,
    val productGroups: List<String>?
)

data class ModifierGroupResponse(
    val modifierGroupId: String?,
    val modifierGroupName: String?,
    val action: String?,
    val defaultSelection: String?,
    val options: List<ModifierInfoResponse>?,
    val min: Int?,
    val max: Int?
)

data class ProductGroupResponse(
    val productGroupId: String?,
    val productGroupName: String?,
    val defaultProduct: String?,
    val options: List<String>?
)

data class ModifierInfosResponse(
    @SerializedName("modifiers") val modifierList: List<ModifierInfoResponse>?
)

data class ModifierInfoResponse(
    val modifierId: String?,
    val modifierName: String?,
    val priceDelta: Float?,
    val receiptText: String?
)
