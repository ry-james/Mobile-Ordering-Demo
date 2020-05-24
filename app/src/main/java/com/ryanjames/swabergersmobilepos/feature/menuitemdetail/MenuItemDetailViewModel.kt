package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.StringResourceWithArgs
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import java.util.*
import javax.inject.Inject

class MenuItemDetailViewModel @Inject constructor() : ViewModel() {

    private var lineItem: LineItem = LineItem.EMPTY
    private lateinit var product: Product
    private var isModifying = true
    
    fun setupWithProduct(product: Product) {
        this.product = product
        this.lineItem = createLineItem(newLineItem = true)
        isModifying = false
        initializeSelections()
    }

    fun setupWithLineItem(lineItem: LineItem) {
        this.lineItem = lineItem
        this.product = lineItem.product
        isModifying = true
        initializeSelections()
    }

    private val _lineItemObservable = MutableLiveData<LineItem>()
    val lineItemObservable: LiveData<LineItem>
        get() = _lineItemObservable

    // Data Binding

    private val _strProductName = MutableLiveData<String>()
    val strProductName: LiveData<String>
        get() = _strProductName

    private val _strProductDescription = MutableLiveData<String>()
    val strProductDescription: LiveData<String>
        get() = _strProductDescription

    private val _strAddToBagBtn = MutableLiveData<StringResourceWithArgs>()
    val strAddToBag: LiveData<StringResourceWithArgs>
        get() = _strAddToBagBtn

    private fun initializeSelections() {
        _strProductName.value = product.productName
        _strProductDescription.value = product.productDescription
        if (isModifying) {
            setProductBundle(lineItem.bundle)
            lineItem.productsInBundle.putAll(lineItem.productsInBundle)
            lineItem.modifiers.putAll(lineItem.modifiers)
        } else {
            for (modifierGroup in product.modifierGroups) {
                lineItem.modifiers[ProductModifierGroupKey(product, modifierGroup)] = listOf(modifierGroup.defaultSelection)
            }
        }
        updateAndNotifyObservers()
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
        updateAndNotifyObservers()
    }

    fun setProductSelectionsForProductGroupByIds(productGroup: ProductGroup, productIds: List<String>) {
        val newProductList = mutableListOf<Product>()

        // Only add products that are in the product group
        for (productId in productIds) {
            productGroup.options.find { it.productId == productId }?.let { newProductList.add(it) }
        }
        setProductSelectionsForProductGroup(productGroup, newProductList)
        updateAndNotifyObservers()

    }

    fun setProductModifiersByIds(product: Product, modifierGroup: ModifierGroup, modifierIds: List<String>) {
        val modifierList = mutableListOf<ModifierInfo>()

        // Only add modifiers that are in the modifier group
        for (id in modifierIds) {
            modifierGroup.options.find { it.modifierId == id }?.let { modifierList.add(it) }
        }
        setModifiersForProduct(product, modifierGroup, modifierList)
        updateAndNotifyObservers()

    }

    fun setQuantity(quantity: Int) {
        lineItem = lineItem.copy(quantity = quantity.coerceAtLeast(1))
        updateAndNotifyObservers()
    }

    private fun setProductSelectionsForProductGroup(productGroup: ProductGroup, products: List<Product>) {
        val oldProductList = lineItem.productsInBundle[productGroup] ?: listOf()
        lineItem.productsInBundle[productGroup] = products

        // Add default modifiers for new product additions
        val newlyAddedProducts = products.minus(oldProductList)
        for (newProduct in newlyAddedProducts) {
            newProduct.modifierGroups.forEach { modifierGroup ->
                setModifiersForProduct(newProduct, modifierGroup, listOf(modifierGroup.defaultSelection))
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

    private fun updateAndNotifyObservers() {
        var price = lineItem.bundle?.price ?: product.price

        for ((_, modifiers) in lineItem.modifiers) {
            for (modifier in modifiers) {
                price += modifier.priceDelta
            }
        }

        price *= lineItem.quantity
        if (isModifying) {
            _strAddToBagBtn.value = StringResourceWithArgs(R.string.update_item, price.toTwoDigitString())
        } else {
            _strAddToBagBtn.value = StringResourceWithArgs(R.string.add_to_bag, price.toTwoDigitString())
        }

        _lineItemObservable.value = createLineItem()
    }

    private fun createLineItem(newLineItem: Boolean = false): LineItem {
        return LineItem(
            if (newLineItem) UUID.randomUUID().toString() else lineItem.id,
            product,
            lineItem.bundle,
            lineItem.productsInBundle,
            lineItem.modifiers,
            lineItem.quantity
        )
    }
}