package com.ryanjames.swabergersmobilepos.network.responses

data class VenueResponse (
    val storeId: String?,
    val storeName: String?,
    val storeAddress: String?,
    val lat: Double?,
    val long: Double?)

data class VenueListResponse(val stores: List<VenueResponse>)