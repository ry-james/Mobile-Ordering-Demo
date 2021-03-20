package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmObject

open class GlobalRealmEntity(
    var localBagOrderId: String?,
    var currentVenue: String?
): RealmObject() {
    constructor() : this(null, null)
}