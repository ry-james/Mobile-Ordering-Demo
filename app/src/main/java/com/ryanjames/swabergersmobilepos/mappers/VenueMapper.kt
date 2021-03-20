package com.ryanjames.swabergersmobilepos.mappers

import com.google.android.gms.maps.model.LatLng
import com.ryanjames.swabergersmobilepos.database.realm.VenueRealmEntity
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.network.responses.VenueResponse

fun VenueResponse.toDomain(): Venue? {
    if (storeId == null || storeName == null) {
        return null
    }
    return Venue(storeId, storeName, storeAddress, LatLng(lat ?: 0.0, long ?: 0.0))
}

fun VenueResponse.toLocal(): VenueRealmEntity? {
    if (storeId == null || storeName == null) {
        return null
    }
    return VenueRealmEntity(storeId, storeName, storeAddress ?: "", lat ?: 0.0, long ?: 0.0)
}

fun VenueRealmEntity.toDomain(): Venue {
    return Venue(venueId, venueName, venueAddress, LatLng(lat, long))
}

fun Venue.toLocal(): VenueRealmEntity {
    return VenueRealmEntity(id, name, address ?: "", latLng.latitude, latLng.longitude)
}