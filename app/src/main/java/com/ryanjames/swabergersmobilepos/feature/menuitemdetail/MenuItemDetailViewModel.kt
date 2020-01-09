package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.*

class MenuItemDetailViewModel(val product: Product) : ViewModel() {

    // Events
    private val _onSelectBundleObservable = MutableLiveData<ProductBundle?>().apply { value = null }
    private val _onSelectProduct = MutableLiveData<HashMap<ProductGroup, List<Product?>>>()
    private val _onSelectProductGroupModifier = MutableLiveData<HashMap<Pair<Product, ModifierGroup>, List<ModifierInfo?>>>()

    val onSelectBundleObservable: LiveData<ProductBundle?>
        get() = _onSelectBundleObservable

    val onSelectProduct: LiveData<HashMap<ProductGroup, List<Product?>>>
        get() = _onSelectProduct

    val onSelectProductGroupModifier: LiveData<HashMap<Pair<Product, ModifierGroup>, List<ModifierInfo?>>>
        get() = _onSelectProductGroupModifier


    private val productSelections = HashMap<ProductGroup, List<Product?>>()
    private val productGroupModifierSelections = HashMap<Pair<Product, ModifierGroup>, List<ModifierInfo?>>()

    val strProductName = MutableLiveData<String>().apply { value = product.productName }

    init {
        initializeSelections()
    }

    private fun initializeSelections() {
        for (modifierGroup in product.modifierGroups) {
            productGroupModifierSelections[Pair(product, modifierGroup)] = listOf(modifierGroup.defaultSelection)
        }
        _onSelectProductGroupModifier.value = productGroupModifierSelections

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
    }

    fun setProductSelection(productGroup: ProductGroup, productIds: List<String>) {

        val productList = mutableListOf<Product>()
        for (productId in productIds) {
            productGroup.options.find { it.productId == productId }?.let { productList.add(it) }
        }

        productSelections[productGroup] = productList
        _onSelectProduct.value = productSelections

        productGroupModifierSelections.clear()

        for (product in productList) {
            product.modifierGroups.forEach {
                setProductGroupModifiers(product, it, listOf(it.defaultSelection.modifierId))
            }
        }

    }

    fun setProductGroupModifiers(product: Product, modifierGroup: ModifierGroup, ids: List<String>) {
        val modifierList = mutableListOf<ModifierInfo>()
        for (id in ids) {
            modifierGroup.options.find { it.modifierId == id }?.let { modifierList.add(it) }
        }

        val key = Pair(product, modifierGroup)
        productGroupModifierSelections[key] = modifierList
        _onSelectProductGroupModifier.value = productGroupModifierSelections

    }
}