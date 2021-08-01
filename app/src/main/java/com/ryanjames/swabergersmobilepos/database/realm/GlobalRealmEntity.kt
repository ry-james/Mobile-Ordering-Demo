package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmObject

open class GlobalRealmEntity(
    var localBagOrderId: String?,
    var currentVenue: String?,
    var deliveryAddress: String?
) : RealmObject() {
    constructor() : this(null, null, null)
}