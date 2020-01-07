package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.*

class MenuItemDetailViewModel(val product: Product) : ViewModel() {

    // Events
    private val _onSelectBundleObservable = MutableLiveData<ProductBundle?>().apply { value = null }
    private val _onSelectProduct = MutableLiveData<HashMap<ProductGroup, Product?>>()
    private val _onSelectProductGroupModifier = MutableLiveData<HashMap<Pair<Product, ModifierGroup>, ModifierInfo?>>()

    val onSelectBundleObservable: LiveData<ProductBundle?>
        get() = _onSelectBundleObservable

    val onSelectProduct: LiveData<HashMap<ProductGroup, Product?>>
        get() = _onSelectProduct

    val onSelectProductGroupModifier: LiveData<HashMap<Pair<Product, ModifierGroup>, ModifierInfo?>>
        get() = _onSelectProductGroupModifier


    private val productSelections = HashMap<ProductGroup, Product?>()
    private val productGroupModifierSelections = HashMap<Pair<Product, ModifierGroup>, ModifierInfo?>()

    val strProductName = MutableLiveData<String>().apply { value = product.productName }

    init {
        initializeSelections()
    }

    private fun initializeSelections() {
        for (modifierGroup in product.modifierGroups) {
            productGroupModifierSelections[Pair(product, modifierGroup)] = modifierGroup.defaultSelection
        }
        _onSelectProductGroupModifier.value = productGroupModifierSelections

    }

    fun setProductBundle(bundle: ProductBundle?) {

        if (_onSelectBundleObservable.value != bundle) {

            _onSelectBundleObservable.value = bundle
            if (bundle != null) {
                for (productGroup in bundle.productGroups) {
                    setProductSelection(productGroup, productGroup.defaultProduct.productId)
                }
            } else {
                productSelections.clear()
                productGroupModifierSelections.clear()
            }
        }
    }

    fun setProductSelection(productGroup: ProductGroup, productId: String) {
        val product = productGroup.options.find { it.productId == productId }
        if (productSelections[productGroup] != product) {
            productSelections[productGroup] = product
            _onSelectProduct.value = productSelections

            productGroupModifierSelections.clear()

            product?.modifierGroups?.forEach {
                setProductGroupModifier(product, it, it.defaultSelection.modifierId)
            }
        }
    }

    fun setProductGroupModifier(product: Product, modifierGroup: ModifierGroup, id: String) {
        val modifierInfo = modifierGroup.options.find { it.modifierId == id }
        val key = Pair(product, modifierGroup)
        if (productGroupModifierSelections[key] != modifierInfo) {
            productGroupModifierSelections[key] = modifierInfo
            _onSelectProductGroupModifier.value = productGroupModifierSelections
        }
    }
}