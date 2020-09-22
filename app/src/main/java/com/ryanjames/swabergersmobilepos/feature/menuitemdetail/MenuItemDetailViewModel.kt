package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.StringResourceWithArgs
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.deepEquals
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MenuItemDetailViewModel @Inject constructor(
    val orderRepository: OrderRepository,
    val menuRepository: MenuRepository
) : ViewModel() {

    private var lineItem: LineItem = LineItem.EMPTY
    private var initialLineItem: LineItem = LineItem.EMPTY
    private var isModifying = false
    private val compositeDisposable = CompositeDisposable()
    private var bagLineItem: BagLineItem? = null

    fun setupWithProductId(productId: String) {
        initialize(productId, false)
    }

    fun setupWithBagLineItem(bagLineItem: BagLineItem) {
        this.bagLineItem = bagLineItem
        initialize(bagLineItem.productId, true)
    }


    private fun initialize(productId: String, isModifying: Boolean) {
        this.isModifying = isModifying


        menuRepository.getProductDetails(productId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { _lineItemObservable.value = Resource.InProgress }
            .subscribe({ product ->
                _strProductName.value = product.productName
                _strProductDescription.value = product.productDescription

                if (product.imageUrl == null) {
                    _bgImageVisibility.value = View.GONE
                } else {
                    _bgImageVisibility.value = View.VISIBLE
                    _imageSrc.value = product.imageUrl
                }

                this.lineItem = LineItem.ofProduct(product)

                if (!isModifying) {
                    initializeDefaultModifiers()
                } else {
                    initializeLineItemSelections(product)
                }

                initialLineItem = this.lineItem.deepCopy()
                updateAndNotifyObservers()

            }, { error ->
                error.printStackTrace()
                _lineItemObservable.value = Resource.Error(Event(Exception(error)))
            })
            .disposedBy(compositeDisposable)
    }

    private fun initializeLineItemSelections(product: Product) {
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
            if (modifierGroup.defaultSelection != null) {
                this.lineItem.modifiers[ProductModifierGroupKey(lineItem.product, modifierGroup)] = listOf(modifierGroup.defaultSelection)
            }
        }
    }

    private val _lineItemObservable = MutableLiveData<Resource<LineItem>>()
    val lineItemObservable: LiveData<Resource<LineItem>>
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

    private val _btnRemoveVisibility = MutableLiveData<Int>().apply { View.GONE }
    val btnRemoveVisibility: LiveData<Int>
        get() = _btnRemoveVisibility

    private val _onAddItem = MutableLiveData<Resource<BagSummary>>()
    val onAddItem: LiveData<Resource<BagSummary>>
        get() = _onAddItem

    private val _onUpdateItem = MutableLiveData<Resource<BagSummary>>()
    val onUpdateItem: LiveData<Resource<BagSummary>>
        get() = _onUpdateItem

    private val _onRemoveItem = MutableLiveData<Resource<BagSummary>>()
    val onRemoveItem: LiveData<Resource<BagSummary>>
        get() = _onRemoveItem

    private val _imageSrc = MutableLiveData<String>()
    val imageSrc: LiveData<String>
        get() = _imageSrc

    private val _bgImageVisibility = MutableLiveData<Int>()
    val bgImageVisibility: LiveData<Int>
        get() = _bgImageVisibility

    private val _transitionId = map(_bgImageVisibility) {
        if (it == View.GONE) {
            R.id.transitionNoImage
        } else {
            R.id.transitionWithImage
        }
    }
    val transitionId: LiveData<Int>
        get() = _transitionId

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

    fun handleMealSelection(selectedId: String) {
        if (lineItem.product.productId == selectedId) {
            setProductBundle(null)
            return
        }
        lineItem.product.bundles.find { it.bundleId == selectedId }.let { setProductBundle(it) }
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
            updateAndNotifyObservers()
        }

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

    fun shouldShowDiscardChanges(): Boolean {
        return lineItem.quantity != initialLineItem.quantity ||
                lineItem.bundle != initialLineItem.bundle ||
                !lineItem.modifiers.deepEquals(initialLineItem.modifiers) ||
                !lineItem.productsInBundle.deepEquals(initialLineItem.productsInBundle)
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

    fun getLineItem(): LineItem? {
        val lineItem = lineItemObservable.value
        return if (lineItem is Resource.Success) {
            lineItem.data.peekContent()
        } else {
            null
        }
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
        if (isModifying) {
            _strAddToBagBtn.value = StringResourceWithArgs(R.string.update_item, lineItem.price.toTwoDigitString())
            _btnRemoveVisibility.value = View.VISIBLE
        } else {
            _strAddToBagBtn.value = StringResourceWithArgs(R.string.add_to_bag, lineItem.price.toTwoDigitString())
            _btnRemoveVisibility.value = View.GONE
        }
        _lineItemObservable.value = Resource.Success(Event(lineItem))
    }

    fun addOrUpdateItem() {
        if (isModifying) {
            updateItem()
        } else {
            addToBag()
        }
    }

    private fun addToBag() {
        compositeDisposable.add(
            orderRepository.addOrUpdateLineItem(lineItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _onAddItem.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _onAddItem.value = Resource.Success(Event(bagSummary))
                }, { error ->
                    _onAddItem.value = Resource.Error(Event(Exception(error)))
                    error.printStackTrace()
                })
        )
    }

    private fun updateItem() {
        compositeDisposable.add(
            orderRepository.addOrUpdateLineItem(lineItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _onUpdateItem.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _onUpdateItem.value = Resource.Success(Event(bagSummary))
                }, { error ->
                    _onUpdateItem.value = Resource.Error(Event(Exception(error)))
                    error.printStackTrace()
                })
        )
    }

    fun removeFromBag() {
        compositeDisposable.add(
            orderRepository.removeLineItem(lineItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _onRemoveItem.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _onRemoveItem.value = Resource.Success(Event(bagSummary))
                }, { error ->
                    _onRemoveItem.value = Resource.Error(Event(Exception(error)))
                    error.printStackTrace()
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}