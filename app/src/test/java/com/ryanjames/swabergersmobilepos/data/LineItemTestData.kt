package com.ryanjames.swabergersmobilepos.data

import com.ryanjames.swabergersmobilepos.domain.*
import java.util.*

object LineItemTestData {

    private val basicProduct: Product
        get() = Product("P1000", "Sample Product", "Sample description", 12.5f, "sample", listOf(), listOf())

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