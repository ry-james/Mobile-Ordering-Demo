package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.database.realm.*
import com.ryanjames.swabergersmobilepos.domain.*
import io.realm.Realm
import io.realm.RealmList

fun LineItem.toEntity(realm: Realm): LineItemRealmEntity {

    val productRealm = realm.where(ProductRealmEntity::class.java).equalTo("productId", product.productId).findFirst()
    val bundleRealm = realm.where(ProductBundleRealmEntity::class.java).equalTo("bundleId", bundle?.bundleId ?: "").findFirst()

    val productsInBundleRealmList = RealmList<ProductsInBundleRealmEntity>()
    for ((key, productList) in productsInBundle) {
        val prodGroupRealm = realm.where(ProductGroupRealmEntity::class.java).equalTo("productGroupId", key.productGroupId).findFirst()

        prodGroupRealm?.let {
            val productListRealm = RealmList<ProductRealmEntity>()
            productList.mapNotNull {
                realm.where(ProductRealmEntity::class.java).equalTo("productId", it.productId).findFirst()?.apply {
                    productListRealm.add(this)
                }
            }
            productsInBundleRealmList.add(realm.createObject(ProductsInBundleRealmEntity::class.java).apply {
                this.productGroupRealmEntity = prodGroupRealm
                this.products = productListRealm
            })
        }

    }

    val modifiersRealm = mutableListOf<ModifiersInProductRealmEntity>()
    for ((productModifierGroupKey, modifierInfoList) in modifiers) {
        val productKey = productModifierGroupKey.product
        val modifierGroupKey = productModifierGroupKey.modifierGroup

        val prodRealm = realm.where(ProductRealmEntity::class.java).equalTo("productId", productKey.productId).findFirst()
        val mgRealm = realm.where(ModifierGroupRealmEntity::class.java).equalTo("modifierGroupId", modifierGroupKey.modifierGroupId).findFirst()

        if (prodRealm != null && mgRealm != null) {
            val modifierInfoRealmList = RealmList<ModifierInfoRealmEntity>()
            modifierInfoList.mapNotNull {
                realm.where(ModifierInfoRealmEntity::class.java).equalTo("modifierId", it.modifierId).findFirst()?.apply {
                    modifierInfoRealmList.add(this)
                }
            }

            modifiersRealm.add(realm.createObject(ModifiersInProductRealmEntity::class.java).apply {
                modifierGroup = mgRealm
                product = prodRealm
                modifiers = modifierInfoRealmList
            })
        }

    }

    return LineItemRealmEntity(id, quantity = quantity).apply {
        product = productRealm
        productBundle = bundleRealm
        modifiers.addAll(modifiersRealm)
        productsInBundle.addAll(productsInBundleRealmList)
    }
}

fun LineItemRealmEntity.toDomain(): LineItem {

    val productMapper = ProductMapper()
    val bundleMapper = ProductBundleMapper()


    val product = product?.let { productMapper.mapToDomain(it) } ?: Product.EMPTY
    val bundle = productBundle?.let { bundleMapper.mapToDomain(it) }

    val products = hashMapOf<ProductGroup, List<Product>>()
    productsInBundle.map {
        val productGroupProductsPair = it.toDomain()
        products[productGroupProductsPair.first] = productGroupProductsPair.second
    }

    val modifiers = hashMapOf<ProductModifierGroupKey, List<ModifierInfo>>()
    this.modifiers.map {
        val productGroupModifiersPair = it.toDomain()
        modifiers[productGroupModifiersPair.first] = productGroupModifiersPair.second
    }

    return LineItem(id, product, bundle, products, modifiers, quantity)
}

fun ProductsInBundleRealmEntity.toDomain(): Pair<ProductGroup, List<Product>> {
    val productGroupMapper = ProductGroupMapper()
    val productMapper = ProductMapper()

    val productGroup = productGroupRealmEntity?.let { productGroupMapper.mapToDomain(it) } ?: ProductGroup.EMPTY
    val products = productMapper.mapToDomain(products)
    return Pair(productGroup, products)
}

fun ModifiersInProductRealmEntity.toDomain(): Pair<ProductModifierGroupKey, List<ModifierInfo>> {
    val modifierGroupMapper = ModifierGroupMapper()
    val modifierInfoMapper = ModifierInfoMapper()
    val productMapper = ProductMapper()

    val modifiers = modifierInfoMapper.mapToDomain(modifiers)
    val product = product?.let { productMapper.mapToDomain(it) } ?: Product.EMPTY
    val modifierGroup = modifierGroup?.let { modifierGroupMapper.mapToDomain(it) } ?: ModifierGroup.EMPTY
    return Pair(ProductModifierGroupKey(product, modifierGroup), modifiers)
}
