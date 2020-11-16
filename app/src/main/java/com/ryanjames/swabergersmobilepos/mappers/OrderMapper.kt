package com.ryanjames.swabergersmobilepos.mappers

import com.ryanjames.swabergersmobilepos.database.realm.LineItemRealmEntity
import com.ryanjames.swabergersmobilepos.database.realm.ModifiersInProductRealmEntity
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmEntity
import com.ryanjames.swabergersmobilepos.database.realm.ProductsInLineItemRealmEntity
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.network.responses.*
import io.realm.RealmList
import java.util.*
import kotlin.collections.HashMap

fun LineItem.toLineItemRequest(): LineItemRequestBody {

    val productInOrderRequestList = mutableListOf<ProductInOrderRequestBody>()

    // Base Product
    val baseProductModifierList = mutableListOf<ModifierSelectionRequestBody>()
    val baseProductModifiers = this.modifiers.filterKeys { it.product.productId == this.product.productId }

    for (productModifierGroup in baseProductModifiers.keys) {
        val modifierRequest = ModifierSelectionRequestBody(
            modifierGroupId = productModifierGroup.modifierGroup.modifierGroupId,
            items = modifiers[productModifierGroup]?.map { it.modifierId } ?: listOf())
        baseProductModifierList.add(modifierRequest)
    }

    val baseProductRequest = ProductInOrderRequestBody(
        productItemId = UUID.randomUUID().toString(),
        productId = this.product.productId,
        productGroupId = this.product.productId,
        modifierSelections = baseProductModifierList
    )

    productInOrderRequestList.add(baseProductRequest)


    // Products in bundle
    for (productGroup in this.productsInBundle.keys) {
        val products = productsInBundle[productGroup] ?: listOf()

        for (product in products) {

            val modifiers = this.modifiers.filterKeys { it.product.productId == product.productId }
            val modifierSelectionsRequest = mutableListOf<ModifierSelectionRequestBody>()

            for (productModifierGroup in modifiers.keys) {
                val modifierRequest = ModifierSelectionRequestBody(
                    modifierGroupId = productModifierGroup.modifierGroup.modifierGroupId,
                    items = modifiers[productModifierGroup]?.map { it.modifierId } ?: listOf())
                modifierSelectionsRequest.add(modifierRequest)
            }

            val productInOrderRequest = ProductInOrderRequestBody(
                productItemId = UUID.randomUUID().toString(),
                productId = product.productId,
                productGroupId = productGroup.productGroupId,
                modifierSelections = modifierSelectionsRequest
            )
            productInOrderRequestList.add(productInOrderRequest)
        }

    }

    return LineItemRequestBody(
        lineItemId = lineItemId,
        quantity = quantity,
        products = productInOrderRequestList,
        baseProduct = product.productId,
        bundleId = bundle?.bundleId
    )
}

fun LineItemRealmEntity.toLineItemRequest(): LineItemRequestBody {

    val productInOrderRequestBodyList = mutableListOf<ProductInOrderRequestBody>()
    productsInBundle.map { productInBundle ->
        val modifierSelectionRequestBodyList = mutableListOf<ModifierSelectionRequestBody>()
        productInBundle.modifiers.map { modifier ->
            val modifierSelectionRequestBody = ModifierSelectionRequestBody(
                modifierGroupId = modifier.modifierGroupId,
                items = modifier.modifierIds
            )
            modifierSelectionRequestBodyList.add(modifierSelectionRequestBody)
        }

        val productInOrderRequestBody = ProductInOrderRequestBody(
            productItemId = productInBundle.productItemId,
            productId = productInBundle.productId,
            productGroupId = productInBundle.productGroupId,
            modifierSelections = modifierSelectionRequestBodyList
        )
        productInOrderRequestBodyList.add(productInOrderRequestBody)
    }

    return LineItemRequestBody(
        lineItemId = lineItemId,
        quantity = quantity,
        products = productInOrderRequestBodyList,
        baseProduct = productId,
        bundleId = bundleId
    )
}

fun GetOrderResponse.toBagSummary(): BagSummary {
    return BagSummary(
        lineItems = lineItems.map { it.toBagLineItem() },
        price = price,
        status = status.toOrderStatus(),
        orderId = orderId
    )
}

fun GetOrderLineItemResponse.toBagLineItem(): BagLineItem {

    val productsInBundle = HashMap(products.groupBy({ it.productGroupId }, { it.productId }))

    val modifierSelections = mutableMapOf<ProductIdModifierGroupIdKey, List<String>>()
    var modifiersDisplay = ""
    for (productResponse in products) {

        if (productResponse.modifierSelections.isEmpty() && bundleId != null) {
            modifiersDisplay += productResponse.productName + "\n"
        }

        for (modifierResponse in productResponse.modifierSelections) {
            val key = ProductIdModifierGroupIdKey(productResponse.productId, modifierResponse.modifierGroupId)
            val newList = modifierSelections.getOrElse(key) { listOf() }.toMutableList()
            newList.addAll(modifierResponse.modifiers.map { it.modifierId })
            modifierSelections[ProductIdModifierGroupIdKey(productResponse.productId, modifierResponse.modifierGroupId)] = newList

            modifiersDisplay += modifierResponse.modifiers.joinToString(",") { it.modifierName } + "\n"
        }

    }

    return BagLineItem(
        lineItemId = lineItemId,
        productId = baseProduct,
        bundleId = bundleId,
        lineItemName = lineItemName,
        price = price,
        productsInBundle = productsInBundle,
        modifiers = HashMap(modifierSelections),
        quantity = quantity,
        modifiersDisplay = modifiersDisplay.trim()
    )
}


fun GetOrderResponse.toLocal(): OrderRealmEntity {
    return OrderRealmEntity(
        orderId = orderId,
        lineItems = RealmList<LineItemRealmEntity>().apply { addAll(lineItems.map { it.toLocal() }) }
    )

}

fun GetOrderLineItemResponse.toLocal(): LineItemRealmEntity {
    return LineItemRealmEntity(
        lineItemId = lineItemId,
        productId = baseProduct,
        bundleId = bundleId,
        quantity = quantity,
        productsInBundle = RealmList<ProductsInLineItemRealmEntity>().apply { addAll(products.map { it.toLocal() }) }
    )
}

fun GetOrderProductResponse.toLocal(): ProductsInLineItemRealmEntity {
    return ProductsInLineItemRealmEntity(
        productItemId = productItemId,
        productGroupId = productGroupId,
        productId = productId,
        modifiers = RealmList<ModifiersInProductRealmEntity>().apply { addAll(modifierSelections.map { it.toLocal() }) }
    )
}

fun GetOrderModifierSelectionResponse.toLocal(): ModifiersInProductRealmEntity {
    return ModifiersInProductRealmEntity(
        modifierGroupId = modifierGroupId,
        modifierIds = RealmList<String>().apply { addAll(modifiers.map { it.modifierId }) }
    )
}

fun ProductDetailsResponse.toDomain(): Product {

    return Product(
        productId = productId,
        productName = productName,
        productDescription = productDescription ?: "",
        price = price,
        receiptText = receiptText,
        bundles = bundles.map { it.toDomain() },
        modifierGroups = modifierGroups.toDomain(),
        imageUrl = imageUrl
    )
}

private fun GetOrderMenuBundleResponse.toDomain(): ProductBundle {

    return ProductBundle(
        bundleId = bundleId ?: "",
        bundleName = bundleName ?: "",
        price = price ?: 0f,
        receiptText = receiptText ?: "",
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
    return Product(productId, productName, "", 0f, "", bundles = listOf(), modifierGroups = this.modifierGroups.toDomain(), imageUrl = null)
}

fun OrderHistoryResponse.toDomain(): List<Order> {
    return this.orders.map { orderResponse ->
        Order(
            lineItems = orderResponse.lineItems.map { lineItemResponse -> lineItemResponse.toDomain() }.toMutableList(),
            orderId = orderResponse.orderId,
            price = orderResponse.price,
            formattedDate = orderResponse.creationDate,
            status = orderResponse.status.toOrderStatus(),
            customerName = orderResponse.customerName ?: "Anonymous"
        )
    }
}

private fun String.toOrderStatus(): OrderStatus {
    return when (this) {
        "CREATED" -> OrderStatus.CREATED
        "CANCELLED" -> OrderStatus.CANCELLED
        "CHECKOUT" -> OrderStatus.CHECKOUT
        else -> OrderStatus.UNKNOWN
    }
}

fun LineItemResponse.toDomain(): LineItem {
    val product = Product.EMPTY.copy(productName = lineItemName, price = this.price)
    return LineItem(lineItemId, product, null, hashMapOf(), hashMapOf(), quantity)
}

private fun List<ModifierGroupResponse>.toDomain(): List<ModifierGroup> {
    val modifierGroupMapper = ModifierGroupMapper()
    val modifierGroupDb = modifierGroupMapper.mapRemoteToLocalDb(this)
    return modifierGroupMapper.mapLocalDbToDomain(modifierGroupDb)
}
