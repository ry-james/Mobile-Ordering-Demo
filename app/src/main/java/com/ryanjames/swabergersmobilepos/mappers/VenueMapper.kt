package com.ryanjames.swabergersmobilepos.mappers

import com.google.android.gms.maps.model.LatLng
import com.ryanjames.swabergersmobilepos.database.realm.VenueRealmEntity
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.helper.toRealmList
import com.ryanjames.swabergersmobilepos.network.responses.VenueResponse

fun VenueResponse.toDomain(): Venue? {
    if (storeId == null || storeName == null) {
        return null
    }
    return Venue(
        id = storeId,
        name = storeName,
        address = storeAddress,
        latLng = LatLng(lat ?: 0.0, long ?: 0.0),
        rating = rating ?: 0f,
        numberOfRatings = numRating ?: 0,
        deliveryTimeInMinsHigh = prepMax ?: 0,
        deliveryTimeInMinsLow = prepMin ?: 0,
        priceIndicator = priceLevel ?: "",
        categories = categories.orEmpty(),
        featuredImage = featuredImage
    )
}

fun VenueResponse.toLocal(): VenueRealmEntity? {
    if (storeId == null || storeName == null) {
        return null
    }
    return VenueRealmEntity(
        venueId = storeId,
        venueName = storeName,
        venueAddress = storeAddress ?: "",
        lat = lat ?: 0.0,
        longitude = long ?: 0.0,
        rating = rating ?: 0f,
        numberOfRatings = numRating ?: 0,
        deliveryTimeInMinsHigh = prepMax ?: 0,
        deliveryTimeInMinsLow = prepMin ?: 0,
        priceIndicator = priceLevel ?: "",
        categories = categories.toRealmList(),
        featuredImage = featuredImage
    )
}

fun VenueRealmEntity.toDomain(): Venue {
    return Venue(
        id = venueId,
        name = venueName,
        address = venueAddress,
        latLng = LatLng(lat, longitude),
        rating = rating,
        numberOfRatings = numberOfRatings,
        deliveryTimeInMinsHigh = deliveryTimeInMinsHigh,
        deliveryTimeInMinsLow = deliveryTimeInMinsHigh,
        priceIndicator = priceIndicator,
        categories = categories.toList(),
        featuredImage = featuredImage
    )
}

fun Venue.toLocal(): VenueRealmEntity {
    return VenueRealmEntity(
        venueId = id,
        venueName = name,
        venueAddress = address ?: "",
        lat = latLng.latitude,
        longitude = latLng.longitude,
        rating = rating,
        numberOfRatings = numberOfRatings,
        deliveryTimeInMinsLow = deliveryTimeInMinsLow,
        deliveryTimeInMinsHigh = deliveryTimeInMinsHigh,
        priceIndicator = priceIndicator,
        categories = categories.toRealmList(),
        featuredImage = featuredImage
    )
}