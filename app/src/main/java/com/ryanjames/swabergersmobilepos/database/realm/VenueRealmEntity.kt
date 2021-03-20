package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmObject

open class VenueRealmEntity(
    var venueId: String,
    var venueName: String,
    var venueAddress: String,
    var lat: Double,
    var long: Double
) : RealmObject() {
    constructor() : this("", "", "", 0.0, 0.0)
}