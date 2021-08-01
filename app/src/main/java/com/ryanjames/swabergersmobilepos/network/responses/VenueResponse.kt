package com.ryanjames.swabergersmobilepos.network.responses

data class VenueResponse(
    val storeId: String?,
    val storeName: String?,
    val storeAddress: String?,
    val lat: Double?,
    val long: Double?,
    val priceLevel: String?,
    val rating: Float?,
    val numRating: Int?,
    val prepMin: Int?,
    val prepMax: Int?,
    val categories: List<String>?,
    val featuredImage: String?
)

data class VenueListResponse(val stores: List<VenueResponse>)