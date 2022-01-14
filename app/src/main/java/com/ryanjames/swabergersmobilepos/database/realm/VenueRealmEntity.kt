package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmList
import io.realm.RealmObject

open class VenueRealmEntity(
    var venueId: String,
    var venueName: String,
    var venueAddress: String,
    var lat: Double,
    var longitude: Double,
    var rating: Float,
    var numberOfRatings: Int,
    var deliveryTimeInMinsLow: Int,
    var deliveryTimeInMinsHigh: Int,
    var priceIndicator: String,
    var categories: RealmList<String>,
    var featuredImage: String?
) : RealmObject() {
    constructor() : this("", "", "", 0.0, 0.0, 0.0f, 0, 0, 0, "", RealmList(), null)
}