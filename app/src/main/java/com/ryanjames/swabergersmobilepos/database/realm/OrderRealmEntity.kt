package com.ryanjames.swabergersmobilepos.database.realm

import androidx.room.PrimaryKey
import io.realm.RealmList
import io.realm.RealmObject

open class OrderRealmEntity(
    var orderId: String,
    var lineItems: RealmList<LineItemRealmEntity>
) : RealmObject() {
    constructor() : this("", RealmList())
}

open class LocalBagRealmEntity(
    var lineItems: RealmList<LineItemRealmEntity>,
    var venueId: String
) : RealmObject() {
    constructor() : this(RealmList(), "")
}

open class LineItemRealmEntity(
    @PrimaryKey
    var lineItemId: String,
    var productId: String,
    var bundleId: String?,
    var quantity: Int,
    var productsInBundle: RealmList<ProductsInLineItemRealmEntity>
) : RealmObject() {
    constructor() : this("", "", "", 0, RealmList())

    fun deleteChildrenFromRealm() {
        productsInBundle.map { it.deleteChildrenFromRealm() }
        productsInBundle.deleteAllFromRealm()
    }
}

open class ProductsInLineItemRealmEntity(
    var productItemId: String,
    var productId: String,
    var productGroupId: String,
    var modifiers: RealmList<ModifiersInProductRealmEntity>
) : RealmObject() {
    constructor() : this("", "", "", RealmList())

    fun deleteChildrenFromRealm() {
        modifiers.deleteAllFromRealm()
    }
}

open class ModifiersInProductRealmEntity(
    var modifierGroupId: String,
    var modifierIds: RealmList<String>
) : RealmObject() {
    constructor() : this("", RealmList())
}