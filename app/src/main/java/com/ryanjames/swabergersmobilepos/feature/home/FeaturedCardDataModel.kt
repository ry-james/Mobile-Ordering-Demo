package com.ryanjames.swabergersmobilepos.feature.home

import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class FeaturedCardDataModel(venue: Venue) {
    val name = venue.name
    val type = venue.categories.joinToString()
    val priceIndicator = venue.priceIndicator
    val rating = venue.rating.toTwoDigitString()
    val numberOfRating = "(${venue.numberOfRatings})"
    val deliveryTime = "${venue.deliveryTimeInMinsLow}-${venue.deliveryTimeInMinsHigh} mins"
    val featuredImage = venue.featuredImage
}