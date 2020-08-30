package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.network.responses.*


fun GetOrderResponse.toDomain(): Order {
    return Order(
        lineItems = lineItems.map { it.toDomain() }.toMutableList(),
        orderId = orderId,
        price = price,
        formattedDate = creationDate,
        customerName = ""
    )
}

fun GetOrderLineItemResponse.toDomain(): LineItem {

    val menuProduct = menuProduct.toDomain()
    val productsInBundle = hashMapOf<ProductGroup, List<Product>>()
    val modifiers = hashMapOf<ProductModifierGroupKey, List<ModifierInfo>>()
    val modifierInfoMapper = ModifierInfoMapper()


    val bundle = menuProduct.bundles.find { it.bundleId == bundleId }

    products.forEach { productInBundleResponse ->

        val productGroup = bundle?.productGroups?.find { it.productGroupId == productInBundleResponse.productGroupId }

        val product = if (productInBundleResponse.productId == menuProduct.productId) {
            menuProduct
        } else {
            productGroup?.options?.find { it.productId == productInBundleResponse.productId }
        }

        if (product != null) {

            // Add modifiers
            productInBundleResponse.modifierSelections.forEach { modifierSelectionResponse ->

                product.modifierGroups.find { it.modifierGroupId == modifierSelectionResponse.modifierGroupId }?.let { modifierGroup ->
                    val productModifierGroupKey = ProductModifierGroupKey(product, modifierGroup)
                    val modifierGroupList = (modifiers[productModifierGroupKey]?.toMutableList() ?: mutableListOf()).apply {
                        val modifierRealm = modifierInfoMapper.mapRemoteToLocalDb(modifierSelectionResponse.modifiers)
                        addAll(modifierInfoMapper.mapLocalDbToDomain(modifierRealm))
                    }
                    modifiers[productModifierGroupKey] = modifierGroupList
                }

            }
        }

        // Add products from bundles
        if (productGroup != null && product != null) {
            val productList = (productsInBundle[productGroup]?.toMutableList() ?: mutableListOf()).apply {
                add(product)
            }
            productsInBundle[productGroup] = productList
        }
    }

    return LineItem(
        lineItemId,
        product = menuProduct,
        bundle = bundle,
        productsInBundle = productsInBundle,
        modifiers = modifiers,
        quantity = quantity
    )
}

fun ProductDetailsResponse.toDomain(): Product {

    return Product(
        productId,
        productName,
        productDescription ?: "",
        price,
        receiptText,
        bundles = bundles.map { it.toDomain() },
        modifierGroups = modifierGroups.toDomain()
    )
}

private fun GetOrderMenuBundleResponse.toDomain(): ProductBundle {

    return ProductBundle(
        bundleId ?: "",
        bundleName ?: "",
        price ?: 0f,
        receiptText ?: "",
        productGroups = productGroups?.map { it.toDomain() } ?: listOf()
    )
}

private fun GetOrderMenuProductGroupResponse.toDomain(): ProductGroup {

    val products = options?.map { it.toDomain() }
    val defaultProd = products?.find { it.productId == defaultProduct }

    return ProductGroup(
        productGroupId ?: "",
        productGroupName ?: "",
        defaultProduct = defaultProd ?: Product.EMPTY,
        min = min ?: 1,
        max = max ?: 1,
        options = products ?: listOf()
    )
}

private fun GetOrderProductGroupProductResponse.toDomain(): Product {
    return Product(productId, productName, "", 0f, "", bundles = listOf(), modifierGroups = this.modifierGroups.toDomain())
}

private fun List<ModifierGroupResponse>.toDomain(): List<ModifierGroup> {
    val modifierGroupMapper = ModifierGroupMapper()
    val modifierGroupDb = modifierGroupMapper.mapRemoteToLocalDb(this)
    return modifierGroupMapper.mapLocalDbToDomain(modifierGroupDb)
}
