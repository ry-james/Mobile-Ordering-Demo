package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.database.realm.*
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.network.responses.*
import io.realm.Realm
import io.realm.RealmList
import java.util.*

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


    val product = product?.let { productMapper.mapLocalToDomain(it) } ?: Product.EMPTY
    val bundle = productBundle?.let { bundleMapper.mapLocalToDomain(it) }

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

    val productGroup = productGroupRealmEntity?.let { productGroupMapper.mapLocalToDomain(it) } ?: ProductGroup.EMPTY
    val products = productMapper.mapLocalToDomain(products)
    return Pair(productGroup, products)
}

fun ModifiersInProductRealmEntity.toDomain(): Pair<ProductModifierGroupKey, List<ModifierInfo>> {
    val modifierGroupMapper = ModifierGroupMapper()
    val modifierInfoMapper = ModifierInfoMapper()
    val productMapper = ProductMapper()

    val modifiers = modifierInfoMapper.mapLocalToDomain(modifiers)
    val product = product?.let { productMapper.mapLocalToDomain(it) } ?: Product.EMPTY
    val modifierGroup = modifierGroup?.let { modifierGroupMapper.mapLocalToDomain(it) } ?: ModifierGroup.EMPTY
    return Pair(ProductModifierGroupKey(product, modifierGroup), modifiers)
}

fun Order.toRemoteEntity(orderId: String): OrderBody {
    return OrderBody(orderId, lineItems.map { it.toRemoteEntity() })
}

fun OrderHistoryResponse.toDomain(): List<Order> {
    return this.orders.map { orderResponse ->
        Order(orderResponse.lineItems.map { lineItemResponse -> lineItemResponse.toDomain() }.toMutableList())
            .apply {
                orderId = orderResponse.orderId
                price = orderResponse.price
                formattedDate = orderResponse.creationDate
            }
    }
}

fun LineItemResponse.toDomain(): LineItem {
    val product = Product.EMPTY.copy(productName = lineItemName, price = this.price)
    return LineItem(lineItemId, product, null, hashMapOf(), hashMapOf(), quantity)
}

fun LineItem.toRemoteEntity(): LineItemRequestBody {

    val productInOrderRequestBodyList = mutableListOf<ProductInOrderRequestBody>()

    val baseProductModifierRequest = mutableListOf<ModifierSelectionRequestBody>()
    for (modifierGroup in product.modifierGroups) {
        val key = ProductModifierGroupKey(product, modifierGroup)
        baseProductModifierRequest.add(
            ModifierSelectionRequestBody(
                product.productId,
                modifierGroup.modifierGroupId,
                modifiers[key]?.sumByDouble { it.priceDelta.toDouble() }?.toFloat() ?: 0f,
                modifiers[key]?.map { it.modifierId }.orEmpty()
            )
        )
    }

    productInOrderRequestBodyList.add(ProductInOrderRequestBody(UUID.randomUUID().toString(), product.productId, baseProductModifierRequest))



    this.productsInBundle.forEach { (productGroup, productList) ->
        productList.forEach { product ->

            val modifierRequest = mutableListOf<ModifierSelectionRequestBody>()

            for (modifierGroup in product.modifierGroups) {
                val key = ProductModifierGroupKey(product, modifierGroup)
                modifierRequest.add(
                    ModifierSelectionRequestBody(
                        productGroup.productGroupId,
                        modifierGroup.modifierGroupId,
                        modifiers[key]?.sumByDouble { it.priceDelta.toDouble() }?.toFloat() ?: 0f,
                        modifiers[key]?.map { it.modifierId }.orEmpty()
                    )
                )

            }


            productInOrderRequestBodyList.add(ProductInOrderRequestBody(UUID.randomUUID().toString(), product.productId, modifierRequest))
        }

    }

    return LineItemRequestBody(id, lineItemName, quantity, price, unitPrice, productInOrderRequestBodyList, product.productId)
}