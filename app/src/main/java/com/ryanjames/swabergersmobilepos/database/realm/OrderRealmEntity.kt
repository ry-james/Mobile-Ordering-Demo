package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey

open class LocalBagRealmEntity(
    var lineItems: RealmList<LineItemRealmEntity>
) : RealmObject() {
    constructor() : this(RealmList())
}

open class LineItemRealmEntity(
    @PrimaryKey @JvmField
    var id: String,
    var product: ProductRealmEntity? = null,
    var productBundle: ProductBundleRealmEntity? = null,
    var productsInBundle: RealmList<ProductsInBundleRealmEntity> = RealmList(),
    var modifiers: RealmList<ModifiersInProductRealmEntity> = RealmList(),
    var quantity: Int
) : RealmObject() {
    constructor() : this("", null, null, RealmList(), RealmList(), 1)
}

open class ProductsInBundleRealmEntity(
    var productGroupRealmEntity: ProductGroupRealmEntity? = null,
    var products: RealmList<ProductRealmEntity> = RealmList()
) : RealmObject() {
    constructor() : this(null, RealmList())
}

open class ModifiersInProductRealmEntity(
    var product: ProductRealmEntity? = null,
    var modifierGroup: ModifierGroupRealmEntity? = null,
    var modifiers: RealmList<ModifierInfoRealmEntity> = RealmList(),
    @LinkingObjects("modifiers")
    val lineItems: RealmResults<LineItemRealmEntity>? = null
) : RealmObject() {
    constructor() : this(null, null, RealmList())
}