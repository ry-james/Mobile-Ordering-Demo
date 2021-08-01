package com.ryanjames.swabergersmobilepos.manager

import com.ryanjames.swabergersmobilepos.domain.*

class LineItemManager(val product: Product, private val bagLineItem: BagLineItem? = null, val listener: (LineItem) -> Unit = { }) {

    private var lineItem: LineItem = LineItem.EMPTY

    init {
        this.lineItem = LineItem.ofProduct(product)
        if (bagLineItem == null) {
            initializeDefaultModifiers()
        } else {
            initializeLineItemSelections()
        }
        notifyObserver()
    }

    private fun initializeLineItemSelections() {
        // Base Product Modifiers
        initializeModifierSelectionsFor(product)

        // Products in bundle
        val bundle = product.bundles.find { bagLineItem?.bundleId == it.bundleId }
        lineItem = lineItem.copy(bundle = bundle, quantity = bagLineItem?.quantity ?: 1, lineItemId = bagLineItem?.lineItemId ?: lineItem.lineItemId)
        bagLineItem?.productsInBundle?.forEach { map ->
            val productGroupId = map.key
            val productIds = map.value

            bundle?.productGroups?.find { it.productGroupId == productGroupId }?.let { productGroup ->
                val products = productIds.mapNotNull { productId -> productGroup.options.find { it.productId == productId } }
                lineItem.productsInBundle[productGroup] = products

                // Modifiers in bundle products
                products.forEach { bundleProduct ->
                    initializeModifierSelectionsFor(bundleProduct)
                }
            }

        }
    }

    private fun initializeModifierSelectionsFor(product: Product) {
        val productModifiers = bagLineItem?.modifiers?.filterKeys { it.productId == product.productId }
        productModifiers?.forEach {
            val modifierGroupId = it.key.modifierGroupId
            val modifierIds = it.value
            product.modifierGroups.find { it.modifierGroupId == modifierGroupId }?.let { modifierGroup ->
                val modifiers = modifierIds.mapNotNull { modifierId -> modifierGroup.options.find { it.modifierId == modifierId } }
                lineItem.modifiers[ProductModifierGroupKey(product, modifierGroup)] = modifiers
            }
        }
    }


    private fun initializeDefaultModifiers() {
        for (modifierGroup in lineItem.product.modifierGroups) {
            val defaultModifierList = if (modifierGroup.defaultSelection != null) listOf(modifierGroup.defaultSelection) else listOf()
            this.lineItem.modifiers[ProductModifierGroupKey(lineItem.product, modifierGroup)] = defaultModifierList
        }
    }

    fun setProductBundle(bundle: ProductBundle?) {

        if (lineItem.bundle != bundle) {
            lineItem = lineItem.copy(bundle = bundle)

            // If bundle is not null, set the default product for each product group in the bundle
            if (bundle != null) {
                for (productGroup in bundle.productGroups) {
                    setProductSelectionsForProductGroup(productGroup, listOf(productGroup.defaultProduct))
                }
            }
            // If bundle is null, clear all selected bundle products and its modifiers
            else {
                lineItem.productsInBundle.flatMap { it.value }.forEach { removeModifiersForProduct(it) }
                lineItem.productsInBundle.clear()
            }
        }
        notifyObserver()
    }

    fun setProductSelectionsForProductGroupByIds(productGroup: ProductGroup, productIds: List<String>) {
        val newProductList = mutableListOf<Product>()

        // Only add products that are in the product group
        for (productId in productIds) {
            productGroup.options.find { it.productId == productId }?.let { newProductList.add(it) }
        }

        // Don't update if all productIds are not part of the product group
        if (newProductList.isNotEmpty()) {
            setProductSelectionsForProductGroup(productGroup, newProductList)
            notifyObserver()
        }

    }

    fun setProductModifiersByIds(product: Product, modifierGroup: ModifierGroup, modifierIds: List<String>) {
        val modifierList = mutableListOf<ModifierInfo>()

        // Only add modifiers that are in the modifier group
        for (id in modifierIds) {
            modifierGroup.options.find { it.modifierId == id }?.let { modifierList.add(it) }
        }
        setModifiersForProduct(product, modifierGroup, modifierList)
        notifyObserver()
    }

    fun setQuantity(quantity: Int) {
        lineItem = lineItem.copy(quantity = quantity.coerceAtLeast(1))
        notifyObserver()
    }

    fun incrementQuantity() {
        setQuantity(lineItem.quantity + 1)
    }

    fun decrementQuantity() {
        setQuantity(lineItem.quantity - 1)
    }

    private fun setProductSelectionsForProductGroup(productGroup: ProductGroup, products: List<Product>) {
        val oldProductList = lineItem.productsInBundle[productGroup] ?: listOf()
        lineItem.productsInBundle[productGroup] = products

        // Add default modifiers for new product additions
        val newlyAddedProducts = products.minus(oldProductList)
        for (newProduct in newlyAddedProducts) {
            newProduct.modifierGroups.forEach { modifierGroup ->
                if (modifierGroup.defaultSelection != null) {
                    setModifiersForProduct(newProduct, modifierGroup, listOf(modifierGroup.defaultSelection))
                }
            }
        }

        // Delete modifiers for removed product selections
        val removedProducts = oldProductList.minus(products)
        removedProducts.forEach { removeModifiersForProduct(it) }
    }

    private fun setModifiersForProduct(product: Product, modifierGroup: ModifierGroup, modifiers: List<ModifierInfo>) {
        val key = ProductModifierGroupKey(product, modifierGroup)
        lineItem.modifiers[key] = modifiers
    }

    private fun removeModifiersForProduct(product: Product) {
        val productGroupModifiersToRemove = mutableListOf<ProductModifierGroupKey>()
        for ((productGroupModifierKey, _) in lineItem.modifiers) {
            if (productGroupModifierKey.product == product) {
                productGroupModifiersToRemove.add(productGroupModifierKey)
            }
        }
        productGroupModifiersToRemove.forEach { lineItem.modifiers.remove(it) }
    }

    fun getLineItem(): LineItem {
        return lineItem.deepCopy()
    }

    fun isModifying(): Boolean = bagLineItem != null

    private fun notifyObserver() {
        listener.invoke(lineItem)
    }

    fun getSelectedIdsForProductGroup(productGroup: ProductGroup): List<String> {
        return getLineItem().productsInBundle[productGroup]?.map { it.productId } ?: listOf(productGroup.defaultProduct.productId)
    }

    fun getSelectedIdsForModifierGroup(product: Product, modifierGroup: ModifierGroup): List<String> {
        val modifierId = getLineItem().modifiers[ProductModifierGroupKey(product, modifierGroup)]?.map { it.modifierId }
        return when {
            modifierId != null -> modifierId
            modifierGroup.defaultSelection?.modifierId != null -> listOf(modifierGroup.defaultSelection.modifierId)
            else -> listOf()
        }
    }

}