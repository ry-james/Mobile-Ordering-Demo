package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Venue(
    val id: String,
    val name: String,
    val address: String?,
    val latLng: LatLng,
    val rating: Float,
    val numberOfRatings: Int,
    val deliveryTimeInMinsLow: Int,
    val deliveryTimeInMinsHigh: Int,
    val priceIndicator: String,
    val categories: List<String>,
    val featuredImage: String?
) : Parcelable

data class VenueMarker(
    val venue: Venue,
    @DrawableRes var icon: Int
)

data class HomeVenues(
    val featuredVenues: List<Venue>,
    val restaurants: List<Venue>
)

fun List<VenueMarker>.getLatLngBounds(): LatLngBounds {
    val venues = this.map { it.venue }
    val x1 = venues.minBy { it.latLng.latitude }?.latLng?.latitude ?: 0.0
    val x2 = venues.maxBy { it.latLng.latitude }?.latLng?.latitude ?: 0.0
    val y1 = venues.minBy { it.latLng.longitude }?.latLng?.longitude ?: 0.0
    val y2 = venues.maxBy { it.latLng.longitude }?.latLng?.longitude ?: 0.0
    return LatLngBounds(LatLng(x1, y1), LatLng(x2, y2))
}

