package com.ryanjames.swabergersmobilepos.feature.venuedetail

import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class VenueDataModel(venue: Venue) {

    val venueName = venue.name
    val venueAddress = venue.address
    val rating = venue.rating.toTwoDigitString()
    val numRating = "(${venue.numberOfRatings} ratings)"
    val featuredImage = venue.featuredImage

}