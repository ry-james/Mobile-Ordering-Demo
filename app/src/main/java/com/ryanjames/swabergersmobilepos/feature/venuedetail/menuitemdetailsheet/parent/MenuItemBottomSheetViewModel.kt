package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.StringResourceArgs
import com.ryanjames.swabergersmobilepos.core.StringWrapper
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier.MenuItemModifierDataModel
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier.PickerItemAdapter
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.manager.LineItemManager
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MenuItemBottomSheetViewModel @Inject constructor(
    val menuRepository: MenuRepository,
    val orderRepository: OrderRepository,
    val venueRepository: VenueRepository
) : ViewModel() {

    private var product: Product? = null
        set(value) {
            field = value
            value?.let {
                _productName.value = it.productName
                _productDescription.value = it.productDescription
                _imageUrl.value = it.imageUrl
            }
        }

    private var modifyingItem: ModifyingItem? = null

    private val _onViewPagerPositionChange = MutableLiveData(0)
    val onViewPagerPositionChange: LiveData<Int>
        get() = _onViewPagerPositionChange

    private lateinit var lineItemManager: LineItemManager

    fun setViewPagerPosition(position: Int) {
        _onViewPagerPositionChange.value = if (_onViewPagerPositionChange.value == 1) 0 else 1
    }

    private val _lineItemObservable = MutableLiveData<Resource<LineItem>>()
    val lineItemObservable: LiveData<Resource<LineItem>>
        get() = _lineItemObservable

    private val compositeDisposable = CompositeDisposable()

    private val _onBindMenuItemModifierDataModel = MutableLiveData<MenuItemModifierDataModel>()
    val onBindMenuItemModifierDataModel: LiveData<MenuItemModifierDataModel>
        get() = _onBindMenuItemModifierDataModel

    private val _productName = MutableLiveData<String>()
    val productName: LiveData<String>
        get() = _productName

    private val _productDescription = MutableLiveData<String>()
    val productDescription: LiveData<String>
        get() = _productDescription

    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?>
        get() = _imageUrl

    private val _addToBagBtnText = MutableLiveData<Int>()
    val addToBagBtnText: LiveData<Int>
        get() = _addToBagBtnText

    private val _onShowStartNewOrderDialog = MutableLiveData<Event<Venue>>()
    val onShowStartNewOrderDialog: LiveData<Event<Venue>>
        get() = _onShowStartNewOrderDialog

    private var venue: Venue? = null

    private val _loadingView = MutableLiveData<LoadingDialogBinding>(
        LoadingDialogBinding(
            visibility = View.VISIBLE,
            loadingText = R.string.loading_item,
            textColor = R.color.textColorBlack
        )
    )
    val loadingView: LiveData<LoadingDialogBinding>
        get() = _loadingView

    val imageVisibility = Transformations.map(imageUrl) { url ->
        if (url == null) View.GONE else View.VISIBLE
    }

    val contentVisibility = Transformations.map(loadingView) {
        if (it.visibility == View.GONE && _lineItemObservable.value is Resource.Success) View.VISIBLE else View.GONE
    }

    val price: LiveData<String> = Transformations.map(lineItemObservable) { lineItemResource ->
        return@map if (lineItemResource is Resource.Success) {
            "\$${lineItemResource.data.price.toTwoDigitString()}"
        } else {
            ""
        }
    }

    val quantity: LiveData<String> = Transformations.map(lineItemObservable) { lineItemResource ->
        return@map if (lineItemResource is Resource.Success) {
            lineItemResource.data.quantity.toString()
        } else {
            ""
        }
    }

    private val _addItemEvent = MutableLiveData<Event<Resource<BagSummary>>>()
    val addItemEvent: LiveData<Event<Resource<BagSummary>>>
        get() = _addItemEvent

    private val _updateItemEvent = MutableLiveData<Event<Resource<BagSummary>>>()
    val updateItemEvent: LiveData<Event<Resource<BagSummary>>>
        get() = _updateItemEvent

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingView.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.loading_item,
            textColor = R.color.textColorBlack
        )
    }

    fun onClickPlusQty() {
        lineItemManager.incrementQuantity()
    }

    fun onClickMinusQty() {
        lineItemManager.decrementQuantity()
    }

    fun onClickAddToBagOrUpdate() {
        venue?.let { venue ->
            val currentVenue = venueRepository.getSelectedVenue()
            if (currentVenue == null) {
                venueRepository.setSelectedVenue(venue)
            } else if (currentVenue.id != venue.id) {
                _onShowStartNewOrderDialog.value = Event(currentVenue)
                return
            }

            addOrUpdateLineItem(venue)
        }

    }

    fun clearBagAndChangeVenue() {
        venue?.let { venue ->
            orderRepository.clearLocalBag()
            venueRepository.setSelectedVenue(venue)
            addOrUpdateLineItem(venue)
        }
    }

    private fun addOrUpdateLineItem(venue: Venue) {
        orderRepository.addOrUpdateLineItem(lineItemManager.getLineItem(), venue.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                if (lineItemManager.isModifying()) {
                    _updateItemEvent.value = Event(Resource.InProgress)
                } else {
                    _addItemEvent.value = Event(Resource.InProgress)
                }
            }
            .subscribe({ bagSummary ->
                if (lineItemManager.isModifying()) {
                    _updateItemEvent.value = Event(Resource.Success(bagSummary))
                } else {
                    _addItemEvent.value = Event(Resource.Success(bagSummary))
                }
            }, { error ->
                error.printStackTrace()
                if (lineItemManager.isModifying()) {
                    _updateItemEvent.value = Event(Resource.Error(error))
                } else {
                    _addItemEvent.value = Event(Resource.Error(error))
                }
            })
            .disposedBy(compositeDisposable)
    }

    fun initializeWithProduct(productId: String, venue: Venue) {
        initialize(productId, venue, null)
    }

    fun initializeWithLineItem(lineItem: BagLineItem, venue: Venue) {
        initialize(lineItem.productId, venue, lineItem)
    }

    private fun initialize(productId: String, venue: Venue, bagLineItem: BagLineItem?) {
        this.venue = venue

        _addToBagBtnText.value = if (bagLineItem == null) R.string.add_item_to_bag else R.string.update_item_in_bag

        menuRepository.getProductDetails(productId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _lineItemObservable.value = Resource.InProgress
                setLoadingViewVisibility(View.VISIBLE)
            }
            .doFinally {
                setLoadingViewVisibility(View.GONE)
            }
            .subscribe({ product ->
                this.product = product
                if (!this::lineItemManager.isInitialized) {
                    lineItemManager = LineItemManager(product, listener = this::onLineItemUpdate, bagLineItem = bagLineItem)
                } else {
                    _lineItemObservable.value = Resource.Success(lineItemManager.getLineItem())
                }
            }, { error ->
                error.printStackTrace()
                _lineItemObservable.value = Resource.Error(error)
            })
            .disposedBy(compositeDisposable)
    }

    private fun onLineItemUpdate(lineItem: LineItem) {
        _lineItemObservable.value = Resource.Success(lineItem)
    }

    fun getMenuItemModifierDataModel(): MenuItemModifierDataModel? {
        return _onBindMenuItemModifierDataModel.value
    }

    fun selectMealOptionRow() {
        modifyingItem = ModifyingItem.MealOption()

        val options = mutableListOf<PickerItemAdapter.PickerItem>()
        options.add(PickerItemAdapter.PickerItem(product?.productId ?: "", StringResourceArgs(R.string.ala_carte), StringResourceArgs(R.string.usd_price, product?.price ?: 0)))
        options.addAll(product?.bundles?.map { bundle ->
            PickerItemAdapter.PickerItem(bundle.bundleId, StringWrapper(bundle.bundleName), StringResourceArgs(R.string.usd_price, bundle.price))
        } ?: emptyList())
        val selectedItemId = listOfNotNull(lineItemManager.getLineItem().bundle?.bundleId ?: product?.productId)

        product?.let {
            _onBindMenuItemModifierDataModel.value = MenuItemModifierDataModel(
                minSelection = 1,
                maxSelection = 1,
                defaultSelections = selectedItemId,
                titleText = StringResourceArgs(R.string.select_meal_option),
                subTitleText = null,
                options = options
            )
        }
        setViewPagerPosition(1)
    }

    fun selectProductGroupRow(productGroup: ProductGroup) {
        modifyingItem = ModifyingItem.ProductGroup(productGroup)

        val title = StringResourceArgs(R.string.select_something, productGroup.productGroupName.toLowerCase(Locale.getDefault()))
        val subtitle = if (productGroup.min >= 1) StringResourceArgs(R.string.subtitle_required, productGroup.max) else StringResourceArgs(R.string.subtitle_optional, productGroup.max)
        val options = mutableListOf<PickerItemAdapter.PickerItem>()
        for (option in productGroup.options) {
            val item = PickerItemAdapter.PickerItem(option.productId, StringWrapper(option.productName))
            options.add(item)
        }
        val selectedIds = lineItemManager.getSelectedIdsForProductGroup(productGroup)

        _onBindMenuItemModifierDataModel.value = MenuItemModifierDataModel(
            minSelection = productGroup.min,
            maxSelection = productGroup.max,
            defaultSelections = selectedIds,
            titleText = title,
            subTitleText = subtitle,
            options = options
        )
        setViewPagerPosition(1)
    }

    fun selectProductModifierGroupRow(product: Product, modifierGroup: ModifierGroup) {
        modifyingItem = ModifyingItem.ProductModifierGroup(product, modifierGroup)

        val options = mutableListOf<PickerItemAdapter.PickerItem>()
        for (option in modifierGroup.options) {
            val priceDelta = if (option.priceDelta != 0f) StringResourceArgs(R.string.price_delta, option.priceDelta) else null
            val item = PickerItemAdapter.PickerItem(option.modifierId, StringWrapper(option.modifierName), priceDelta)
            options.add(item)
        }

        val selectedIds = lineItemManager.getSelectedIdsForModifierGroup(product, modifierGroup)

        _onBindMenuItemModifierDataModel.value = MenuItemModifierDataModel(
            minSelection = modifierGroup.min,
            maxSelection = modifierGroup.max,
            defaultSelections = selectedIds,
            titleText = StringResourceArgs(R.string.select_something, modifierGroup.modifierGroupName.toLowerCase(Locale.getDefault())),
            subTitleText = StringWrapper(getRequiredText(modifierGroup)),
            options = options
        )
        setViewPagerPosition(1)
    }

    private fun getRequiredText(modifierGroup: ModifierGroup): String {
        return if (modifierGroup.min != modifierGroup.max) {
            "Select up to ${modifierGroup.max}"
        } else {
            "Required"
        }
    }

    fun saveSelection(selectedIds: List<String>) {
        (modifyingItem as? ModifyingItem.MealOption)?.let {
            val selectedId = selectedIds.getOrElse(0) { product?.productId }
            if (product?.productId == selectedId) {
                lineItemManager.setProductBundle(null)
                return
            }
            product?.bundles?.find { it.bundleId == selectedId }.let { lineItemManager.setProductBundle(it) }
            return
        }

        (modifyingItem as? ModifyingItem.ProductGroup)?.let {
            lineItemManager.setProductSelectionsForProductGroupByIds(it.productGroup, selectedIds)
            return
        }

        (modifyingItem as? ModifyingItem.ProductModifierGroup)?.let {
            lineItemManager.setProductModifiersByIds(it.product, it.modifierGroup, selectedIds)
            return
        }
    }

    private sealed class ModifyingItem {
        class MealOption : ModifyingItem()
        class ProductGroup(val productGroup: com.ryanjames.swabergersmobilepos.domain.ProductGroup) : ModifyingItem()
        class ProductModifierGroup(val product: Product, val modifierGroup: ModifierGroup) : ModifyingItem()
    }

}