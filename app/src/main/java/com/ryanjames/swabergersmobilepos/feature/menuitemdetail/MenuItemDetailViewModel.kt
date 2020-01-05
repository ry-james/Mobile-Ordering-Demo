package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.*

class MenuItemDetailViewModel(val product: Product) : ViewModel() {

    // Events
    private val _onSelectBundleObservable = MutableLiveData<ProductBundle?>().apply { value = null }
    private val _onSelectProductModifier = MutableLiveData<HashMap<ModifierGroup, ModifierInfo?>>()
    private val _onSelectProduct = MutableLiveData<HashMap<ProductGroup, Product?>>()

    val onSelectBundleObservable: LiveData<ProductBundle?>
        get() = _onSelectBundleObservable

    val onSelectProductModifier: LiveData<HashMap<ModifierGroup, ModifierInfo?>>
        get() = _onSelectProductModifier

    val onSelectProduct: LiveData<HashMap<ProductGroup, Product?>>
        get() = _onSelectProduct

    private val modifierSelections = HashMap<ModifierGroup, ModifierInfo?>()
    private val productSelections = HashMap<ProductGroup, Product?>()

    val strProductName = MutableLiveData<String>().apply { value = product.productName }

    init {
        initializeSelections()
    }

    private fun initializeSelections() {
        for (modifierGroup in product.modifierGroups) {
            modifierSelections[modifierGroup] = modifierGroup.defaultSelection
        }
        _onSelectProductModifier.value = modifierSelections

    }

    fun setProductBundle(bundle: ProductBundle?) {
        _onSelectBundleObservable.value = bundle

        if (bundle != null) {
            for (productGroup in bundle.productGroups) {
                setProductSelection(productGroup, productGroup.defaultProduct.productId)
            }
        }
    }

    fun setProductSelection(productGroup: ProductGroup, productId: String) {
        val product = productGroup.options.find { it.productId == productId }
        productSelections[productGroup] = product
        _onSelectProduct.value = productSelections
    }

    fun setModifierGroupSelection(modifierGroup: ModifierGroup, id: String) {
        val modifierInfo = modifierGroup.options.find { it.modifierId == id }
        modifierSelections[modifierGroup] = modifierInfo
        _onSelectProductModifier.value = modifierSelections
    }
}