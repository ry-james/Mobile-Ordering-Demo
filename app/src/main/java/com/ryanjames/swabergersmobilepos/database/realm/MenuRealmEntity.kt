package com.ryanjames.swabergersmobilepos.database.realm

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

open class BasicMenuRealmEntity(
    var categories: RealmList<BasicCategoryRealmEntity>,
    var createdAt: Date
) : RealmObject() {
    constructor() : this(RealmList(), Date())
}

open class BasicCategoryRealmEntity(
    var categoryId: String,
    var categoryName: String,
    var products: RealmList<BasicProductRealmEntity>
) : RealmObject() {

    constructor() : this("", "", RealmList())
}

open class BasicProductRealmEntity(
    var productId: String,
    var productName: String,
    var price: Float
) : RealmObject() {
    constructor() : this("", "", 0f)
}

open class ProductRealmEntity(
    var productId: String,
    var productName: String,
    var productDescription: String,
    var price: Float,
    var receiptText: String,
    var modifierGroups: RealmList<ModifierGroupRealmEntity>,
    var bundles: RealmList<ProductBundleRealmEntity>
) : RealmObject() {
    constructor() : this("", "", "", 0f, "", RealmList(), RealmList())

    fun deleteChildren() {
        bundles.map { it.deleteChildren() }
        bundles.deleteAllFromRealm()
        modifierGroups.map { it.deleteChildren() }
        modifierGroups.deleteAllFromRealm()
    }
}

open class ModifierInfoRealmEntity(
    var modifierId: String,
    var modifierName: String,
    var priceDelta: Float,
    var receiptText: String
) : RealmObject() {
    constructor() : this("", "", 0f, "")
}

open class ModifierGroupRealmEntity(
    var modifierGroupId: String,
    var modifierGroupName: String,
    var action: String,
    var options: RealmList<ModifierInfoRealmEntity>,
    var defaultSelection: String?,
    var min: Int,
    var max: Int
) : RealmObject() {

    constructor() : this("", "", "", RealmList(), "", 1, 1)

    fun getDefaultSelection(): ModifierInfoRealmEntity? {
        return options.find { it.modifierId == defaultSelection }
    }

    fun deleteChildren() {
        options.deleteAllFromRealm()
    }
}

open class ProductBundleRealmEntity(
    var bundleId: String,
    var bundleName: String,
    var price: Float,
    var receiptText: String,
    var productGroups: RealmList<ProductGroupRealmEntity>
) : RealmObject() {

    fun deleteChildren() {
        productGroups.deleteAllFromRealm()
    }

    constructor() : this("", "", 0f, "", RealmList())
}

open class ProductGroupRealmEntity(
    var productGroupId: String,
    var productGroupName: String,
    var options: RealmList<ProductRealmEntity>,
    var defaultProduct: ProductRealmEntity?,
    var min: Int,
    var max: Int
) : RealmObject() {
    constructor() : this("", "", RealmList(), null, 1, 1)
}