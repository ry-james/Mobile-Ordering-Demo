package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class MenuItemDetailViewModel @Inject constructor(val resources: Resources) : ViewModel() {

    private var lineItem: LineItem? = null
    private lateinit var product: Product

    var quantity: Int = 1
        set(value) {
            field = if (value < 1) 1 else value
            updatePrice()
        }

    private val productsInBundleMap = HashMap<ProductGroup, List<Product>>()
    private val productGroupModifierSelections = HashMap<ProductModifierGroupKey, List<ModifierInfo>>()

    fun setupWithProduct(product: Product) {
        this.product = product
        initializeSelections()
    }

    fun setupWithLineItem(lineItem: LineItem) {
        this.lineItem = lineItem
        this.product = lineItem.product
        this.quantity = lineItem.quantity
        initializeSelections()
    }

    // Events

    private val _onSelectBundleObservable = MutableLiveData<ProductBundle?>().apply { value = null }
    val onSelectBundleObservable: LiveData<ProductBundle?>
        get() = _onSelectBundleObservable

    private val _onSelectProduct = MutableLiveData<HashMap<ProductGroup, List<Product>>>()
    val onSelectProduct: LiveData<HashMap<ProductGroup, List<Product>>>
        get() = _onSelectProduct

    private val _onSelectProductGroupModifier = MutableLiveData<HashMap<ProductModifierGroupKey, List<ModifierInfo>>>()
    val onSelectProductGroupModifier: LiveData<HashMap<ProductModifierGroupKey, List<ModifierInfo>>>
        get() = _onSelectProductGroupModifier

    // Data Binding

    private val _strProductName = MutableLiveData<String>()
    val strProductName: LiveData<String>
        get() = _strProductName

    private val _strProductDescription = MutableLiveData<String>()
    val strProductDescription: LiveData<String>
        get() = _strProductDescription

    private val _strAddToBagBtn = MutableLiveData<String>()
    val strAddToBag: LiveData<String>
        get() = _strAddToBagBtn

    private fun initializeSelections() {
        _strProductName.value = product.productName
        _strProductDescription.value = product.productDescription
        if (!isModifying()) {
            for (modifierGroup in product.modifierGroups) {
                productGroupModifierSelections[ProductModifierGroupKey(product, modifierGroup)] = listOf(modifierGroup.defaultSelection)
            }
            _onSelectProductGroupModifier.value = productGroupModifierSelections
        } else {
            lineItem?.let {
                setProductBundle(it.bundle)
                productsInBundleMap.putAll(it.productsInBundle)
                _onSelectProduct.value = productsInBundleMap
                productGroupModifierSelections.putAll(it.modifiers)
                _onSelectProductGroupModifier.value = productGroupModifierSelections
            }

        }
        updatePrice()
    }

    private fun isModifying(): Boolean = lineItem != null

    fun setProductBundle(bundle: ProductBundle?) {

        if (_onSelectBundleObservable.value != bundle) {

            _onSelectBundleObservable.value = bundle

            // If bundle is not null, set the default product for each product group in the bundle
            // If bundle is null, clear all selected bundle products and reset all modifiers
            if (bundle != null) {
                for (productGroup in bundle.productGroups) {
                    setProductSelectionsForProductGroup(productGroup, listOf(productGroup.defaultProduct.productId))
                }
            } else {
                productsInBundleMap.clear()
                productGroupModifierSelections.clear()
            }
        }
        updatePrice()
    }

    fun setProductSelectionsForProductGroup(productGroup: ProductGroup, productIds: List<String>) {
        val oldProductList = productsInBundleMap[productGroup] ?: listOf()
        val newProductList = mutableListOf<Product>()

        // Only add products that are in the product group
        for (productId in productIds) {
            productGroup.options.find { it.productId == productId }?.let { newProductList.add(it) }
        }

        productsInBundleMap[productGroup] = newProductList
        _onSelectProduct.value = productsInBundleMap

        // Add default modifiers for new product additions
        val newlyAddedProducts = newProductList.minus(oldProductList)
        for (newProduct in newlyAddedProducts) {
            newProduct.modifierGroups.forEach { modifierGroup ->
                addProductGroupModifiers(newProduct, modifierGroup, listOf(modifierGroup.defaultSelection.modifierId))
            }
        }

        // Delete modifiers for removed product selections
        oldProductList.minus(newProductList).forEach { removeProductModifiersFromMap(it) }

        updatePrice()

    }

    private fun removeProductModifiersFromMap(product: Product) {
        for ((productGroupModifierKey, _) in productGroupModifierSelections) {
            if (productGroupModifierKey.product == product) {
                productGroupModifierSelections.remove(productGroupModifierKey)
            }
        }
    }

    fun addProductGroupModifiers(product: Product, modifierGroup: ModifierGroup, modifierIds: List<String>) {
        val modifierList = mutableListOf<ModifierInfo>()

        // Only add modifiers that are in the modifier group
        for (id in modifierIds) {
            modifierGroup.options.find { it.modifierId == id }?.let { modifierList.add(it) }
        }

        val key = ProductModifierGroupKey(product, modifierGroup)
        productGroupModifierSelections[key] = modifierList
        _onSelectProductGroupModifier.value = productGroupModifierSelections
        updatePrice()

    }

    private fun updatePrice() {
        var price = onSelectBundleObservable.value?.price ?: product.price
        for ((_, modifiers) in productGroupModifierSelections) {
            for (modifier in modifiers) {
                price += modifier.priceDelta
            }
        }
        price *= quantity
        if (isModifying()) {
            _strAddToBagBtn.value = String.format(resources.getString(R.string.update_item), price.toTwoDigitString())
        } else {
            _strAddToBagBtn.value = String.format(resources.getString(R.string.add_to_bag), price.toTwoDigitString())
        }
    }

    fun createLineItem(): LineItem {
        val id = lineItem?.id ?: UUID.randomUUID().toString()
        return LineItem(
            id,
            product,
            _onSelectBundleObservable.value,
            _onSelectProduct.value ?: hashMapOf(),
            _onSelectProductGroupModifier.value ?: hashMapOf(),
            quantity
        )
    }
}