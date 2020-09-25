package com.ryanjames.swabergersmobilepos.data

import com.ryanjames.swabergersmobilepos.domain.*
import java.util.*

object LineItemTestData {

    val basicProduct: Product
        get() = Product("P1000", "Sample Product", "Sample description", 12.5f, "sample", listOf(), listOf(), null)

    private val basicBundle: ProductBundle
        get() = ProductBundle("B1000", "Sample Bundle", 20f, "Sample", listOf())

    private val basicModifier: ModifierInfo
        get() = ModifierInfo("M1000", "Modifier Info Name", 0.5f, "sample")

    private val basicLineItem: LineItem
        get() = LineItem(UUID.randomUUID().toString(), basicProduct, null, hashMapOf(), hashMapOf(), 1)

    private val basicModifierGroup: ModifierGroup
        get() = ModifierGroup("MG1000", "Modifier Group Name", ModifierGroupAction.Optional, basicModifier, listOf(), 1, 1)

    private fun lineItemModifiers(): HashMap<ProductModifierGroupKey, List<ModifierInfo>> {
        val modifiers: HashMap<ProductModifierGroupKey, List<ModifierInfo>> = hashMapOf()
        val productModifierGroupKey = ProductModifierGroupKey(basicProduct, basicModifierGroup)
        modifiers[productModifierGroupKey] = listOf(basicModifier, basicModifier.copy(priceDelta = 3.5f))
        return modifiers
    }

    fun lineItemProductNoModifier(): LineItem {
        return basicLineItem
    }

    fun lineItemProductWithModifier(): LineItem {
        return basicLineItem.copy(modifiers = lineItemModifiers())
    }

    fun lineItemBundleNoModifiers(): LineItem {
        return basicLineItem.copy(bundle = basicBundle)
    }

    fun lineItemBundleWithModifiers(): LineItem {
        return basicLineItem.copy(bundle = basicBundle, modifiers = lineItemModifiers())
    }

}

fun LineItem.toBagLineItem(): BagLineItem {
    val bundleProducts = hashMapOf<String, List<String>>()
    this.productsInBundle.keys.forEach { key ->
        bundleProducts[key.productGroupId] = this.productsInBundle[key]?.map { it.productId } ?: listOf()
    }

    val modifiers = hashMapOf<ProductIdModifierGroupIdKey, List<String>>()
    this.modifiers.keys.forEach { key ->
        val productIdModifierGroupIdKey = ProductIdModifierGroupIdKey(key.product.productId, key.modifierGroup.modifierGroupId)
        modifiers[productIdModifierGroupIdKey] = this.modifiers[key]?.map { it.modifierId } ?: listOf()
    }

    return BagLineItem(
        lineItemId = lineItemId,
        lineItemName = lineItemName,
        productId = product.productId,
        bundleId = bundle?.bundleId,
        price = price,
        modifiersDisplay = "",
        productsInBundle = bundleProducts,
        modifiers = modifiers,
        quantity = quantity
    )
}