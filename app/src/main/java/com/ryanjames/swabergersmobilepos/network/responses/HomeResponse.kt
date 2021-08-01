package com.ryanjames.swabergersmobilepos.network.responses

data class HomeResponse(
    val featuredStores: List<VenueResponse>,
    val restaurants: List<VenueResponse>
)