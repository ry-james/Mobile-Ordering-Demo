package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class MenuItemDetailViewModel(
    val product: Product,
    quantity: Int = 1
) : ViewModel() {

    var quantity: Int = quantity
        set(value) {
            field = if (value < 1) 1 else value
            updatePrice()
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

    private val productSelections = HashMap<ProductGroup, List<Product>>()
    private val productGroupModifierSelections = HashMap<ProductModifierGroupKey, List<ModifierInfo>>()

    private val _strProductName = MutableLiveData<String>().apply { value = product.productName }
    val strProductName: LiveData<String>
        get() = _strProductName

    private val _strAddToBagBtn = MutableLiveData<String>()
    val strAddToBag: LiveData<String>
        get() = _strAddToBagBtn

    init {
        initializeSelections()
    }

    private fun initializeSelections() {
        for (modifierGroup in product.modifierGroups) {
            productGroupModifierSelections[ProductModifierGroupKey(product, modifierGroup)] = listOf(modifierGroup.defaultSelection)
        }
        _onSelectProductGroupModifier.value = productGroupModifierSelections
        updatePrice()
    }

    fun setProductBundle(bundle: ProductBundle?) {

        if (_onSelectBundleObservable.value != bundle) {

            _onSelectBundleObservable.value = bundle
            if (bundle != null) {
                for (productGroup in bundle.productGroups) {
                    setProductSelection(productGroup, listOf(productGroup.defaultProduct.productId))
                }
            } else {
                productSelections.clear()
                productGroupModifierSelections.clear()
            }
        }
        updatePrice()
    }

    fun setProductSelection(productGroup: ProductGroup, productIds: List<String>) {
        val oldProductList = productSelections[productGroup] ?: listOf()
        val productList = mutableListOf<Product>()
        for (productId in productIds) {
            productGroup.options.find { it.productId == productId }?.let { productList.add(it) }
        }

        productSelections[productGroup] = productList
        _onSelectProduct.value = productSelections

        // Add default modifiers for new product additions
        val diffList = productList.minus(oldProductList)
        for (product in diffList) {
            product.modifierGroups.forEach {
                setProductGroupModifiers(product, it, listOf(it.defaultSelection.modifierId))
            }
        }

        // Delete modifiers for removed product selections
        for ((key, modifiers) in productGroupModifierSelections) {
            if (!productIds.contains(key.product.productId)) {
                removeProductModifiersFromMap(key.product)
            }
        }

        updatePrice()

    }

    private fun removeProductModifiersFromMap(product: Product) {
        for ((key, modifiers) in productGroupModifierSelections) {
            if (key.product == product) {
                productGroupModifierSelections.remove(key)
            }
        }
    }

    fun setProductGroupModifiers(product: Product, modifierGroup: ModifierGroup, ids: List<String>) {
        val modifierList = mutableListOf<ModifierInfo>()
        for (id in ids) {
            modifierGroup.options.find { it.modifierId == id }?.let { modifierList.add(it) }
        }

        val key = ProductModifierGroupKey(product, modifierGroup)
        productGroupModifierSelections[key] = modifierList
        _onSelectProductGroupModifier.value = productGroupModifierSelections
        updatePrice()

    }

    private fun updatePrice() {
        var price = onSelectBundleObservable.value?.price ?: product.price
        for ((key, value) in productGroupModifierSelections) {
            for (modifier in value) {
                price += modifier.priceDelta
            }
        }
        price *= quantity
        _strAddToBagBtn.value = "ADD TO BAG - PHP. ${price.toTwoDigitString()}"
    }

    fun createLineItem(): LineItem {
        return LineItem(
            product,
            _onSelectBundleObservable.value,
            _onSelectProduct.value ?: hashMapOf(),
            _onSelectProductGroupModifier.value ?: hashMapOf(),
            quantity
        )
    }
}