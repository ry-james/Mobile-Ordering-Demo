package com.ryanjames.swabergersmobilepos.feature.old.bagsummary

import android.view.View
import androidx.lifecycle.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.feature.checkout.ServiceOption
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(
    val orderRepository: OrderRepository,
    val venueRepository: VenueRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _localBag = MutableLiveData<Resource<BagSummary>>()
    val getLocalBag: LiveData<Resource<BagSummary>>
        get() = _localBag

    private val _selectedVenue = MutableLiveData<Venue>(venueRepository.getSelectedVenue())
    val selectedVenue: LiveData<Venue>
        get() = _selectedVenue

    private val _checkoutObservable = MutableLiveData<Resource<BagSummary>>()
    val checkoutObservable: LiveData<Resource<BagSummary>>
        get() = _checkoutObservable

    private val serviceOption = MutableLiveData<ServiceOption>(ServiceOption.Pickup)

    val checkedServiceOption = Transformations.map(serviceOption) { option ->
        if (option is ServiceOption.Pickup) {
            R.id.btn_pickup
        } else {
            R.id.btn_delivery
        }
    }

    val deliveryCardVisibility = Transformations.map(serviceOption) { option ->
        if (option is ServiceOption.Delivery) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val pickupCardVisibility = Transformations.map(serviceOption) { option ->
        if (option is ServiceOption.Pickup) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val deliveryAddress: LiveData<String> = Transformations.map(orderRepository.getDeliveryAddressObservable()) { address ->
        if (address.isNullOrEmpty()) {
            ""
        } else {
            address
        }
    }

    val deliveryAddressVisibility: LiveData<Int> = Transformations.map(orderRepository.getDeliveryAddressObservable()) { address ->
        if (address.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val changeAddressBtnText: LiveData<Int> = Transformations.map(orderRepository.getDeliveryAddressObservable()) { address ->
        if (address.isNullOrEmpty()) {
            R.string.set_delivery_address
        } else {
            R.string.change_address
        }
    }

    val loadingViewBinding: LiveData<LoadingDialogBinding> = Transformations.map(getLocalBag) { resource ->
        if (resource is Resource.InProgress) {
            loadingView(View.VISIBLE)
        } else {
            loadingView(View.GONE)
        }
    }

    val nonEmptyBagVisibility: LiveData<Int> = Transformations.map(getLocalBag) { resource ->
        if (resource is Resource.Success && resource.data.lineItems.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val messagingViewBinding: LiveData<MesssageViewBinding> = Transformations.map(getLocalBag) { resource ->
        if (resource is Resource.Success && resource.data.lineItems.isEmpty()) {
            emptyBagView(View.VISIBLE)
        } else if (resource is Resource.Error) {
            errorView(View.VISIBLE)
        } else {
            emptyBagView(View.GONE)
            errorView(View.GONE)
        }
    }

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    val tax: LiveData<String> = Transformations.map(_localBag) {
        "$".plus(localBagOrNull()?.tax()?.toTwoDigitString() ?: "0.00")
    }

    val subtotal: LiveData<String> = Transformations.map(_localBag) {
        "$".plus(localBagOrNull()?.subtotal()?.toTwoDigitString() ?: "0.00")
    }

    val total: LiveData<String> = Transformations.map(_localBag) {
        "$".plus(localBagOrNull()?.price?.toTwoDigitString() ?: "0.00")
    }

    private val _onOrderNotFound = MutableLiveData<Event<Boolean>>()
    val onOrderNotFound: LiveData<Event<Boolean>>
        get() = _onOrderNotFound

    private val _removeModeToggle = MutableLiveData<Boolean>(false)
    val removeModeToggle: LiveData<Boolean>
        get() = _removeModeToggle

    private val _itemsForRemovalList = MutableLiveData<List<BagLineItem>>()
    val itemsForRemovalList
        get() = _itemsForRemovalList.value?.toList() ?: listOf()

    private val _onRemovingItems = MutableLiveData<Resource<BagSummary>>()
    val onRemovingItems: LiveData<Resource<BagSummary>>
        get() = _onRemovingItems

    private fun removeModeVisibility(): Int = if (removeModeToggle.value == true && !isBagEmpty()) View.VISIBLE else View.GONE

    val removeModeVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(_removeModeToggle) { isRemoving ->
            this.value = removeModeVisibility()

            if (!isRemoving) {
                _itemsForRemovalList.value = listOf()
            }
        }
        addSource(_localBag) {
            this.value = removeModeVisibility()
        }
    }

    val viewModeVisibility: LiveData<Int> = Transformations.map(removeModeVisibility) { removeModeVisibility ->
        if (removeModeVisibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    val removeSelectedBtnEnabled: LiveData<Boolean> = Transformations.map(_itemsForRemovalList) { itemsForRemoval ->
        itemsForRemoval.isNotEmpty()
    }

    fun retrieveLocalBag() {
        _selectedVenue.value = venueRepository.getSelectedVenue()
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _localBag.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _localBag.value = Resource.Success(bagSummary)
                },
                    { error ->
                        error.printStackTrace()
                        _localBag.value = Resource.Error(error)
                        handleError(error)
                    })
        )
    }

    fun getSelectedVenue(): Venue? {
        return venueRepository.getSelectedVenue()
    }

    fun setSelectedVenue(venue: Venue) {
        venueRepository.setSelectedVenue(venue)
        _selectedVenue.value = venue
    }

    fun setLocalBag(bagSummary: BagSummary) {
        _localBag.value = Resource.Success(bagSummary)
    }

    val onServiceOptionToggleGroupListener = MaterialButtonToggleGroup.OnButtonCheckedListener { group, checkedId, isChecked ->
        if (checkedId == R.id.btn_delivery) {
            serviceOption.value = ServiceOption.Delivery()
        } else {
            serviceOption.value = ServiceOption.Pickup
        }
    }

    private fun handleError(error: Throwable) {
        if (error is HttpException) {
            if (error.code() == 404) {
                clearBag()
                _onOrderNotFound.value = Event(true)
            }
        }
    }

    fun localBagOrNull(): BagSummary? {
        val resource = getLocalBag.value
        if (resource is Resource.Success) {
            return resource.data
        }
        return null
    }

    private fun loadingView(visibility: Int): LoadingDialogBinding {
        return LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.fetching_bag,
            textColor = R.color.colorWhite
        )
    }

    private fun emptyBagView(visibility: Int): MesssageViewBinding {
        return MesssageViewBinding(
            visibility = visibility,
            image = R.drawable.ic_empty_bag,
            title = R.string.empty_bag_title,
            message = R.string.empty_bag_subtitle
        )
    }

    private fun errorView(visibility: Int): MesssageViewBinding {
        return MesssageViewBinding(
            visibility = visibility,
            image = R.drawable.ic_error,
            title = R.string.us_not_you,
            message = R.string.error_fetching_bag
        )
    }

    private fun isBagEmpty(): Boolean {
        return localBagOrNull()?.lineItems?.isEmpty() ?: true
    }

    fun setBagSummary(bagSummary: BagSummary) {
        _localBag.value = Resource.Success(bagSummary)
        _removeModeToggle.value = false
    }

    private fun clearBag() {
        orderRepository.clearLocalBag()
        venueRepository.clearSelectedVenue()
        _localBag.value = Resource.Success(BagSummary(emptyList(), 0f, OrderStatus.UNKNOWN, ""))
    }

    fun onClickRemove() {
        _removeModeToggle.value = true
    }

    fun onClickRemoveSelected() {
        compositeDisposable.add(
            orderRepository.removeBagLineItems(itemsForRemovalList, venueRepository.getSelectedVenue()?.id ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _onRemovingItems.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _onRemovingItems.value = Resource.Success(bagSummary)
                    _removeModeToggle.value = false
                    _localBag.value = Resource.Success(bagSummary)

                    if (bagSummary.lineItems.isEmpty()) {
                        clearBag()
                    }
                }, { error ->
                    _onRemovingItems.value = Resource.Error(error)
                    error.printStackTrace()
                })
        )
    }

    fun onClickCancelRemove() {
        _removeModeToggle.value = false
        _itemsForRemovalList.value = listOf()
    }

    fun addItemForRemoval(bagLineItem: BagLineItem) {
        val itemsForRemoval = _itemsForRemovalList.value?.toMutableList() ?: mutableListOf()
        val item = itemsForRemoval.find { it.lineItemId == bagLineItem.lineItemId }
        if (item == null) {
            itemsForRemoval.add(bagLineItem)
            _itemsForRemovalList.value = itemsForRemoval
        }
    }

    fun removeItemForRemoval(bagLineItem: BagLineItem) {
        val itemsForRemoval = _itemsForRemovalList.value?.toMutableList() ?: mutableListOf()
        val item = itemsForRemoval.find { it.lineItemId == bagLineItem.lineItemId }
        if (item != null) {
            itemsForRemoval.remove(item)
            _itemsForRemovalList.value = itemsForRemoval
        }
    }

    fun onClickPlaceOrder() {
        compositeDisposable.add(orderRepository.checkout(
                customerName = "Customer",
                serviceOption = serviceOption.value ?: ServiceOption.Pickup,
                venueId = venueRepository.getSelectedVenue()?.id ?: "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _checkoutObservable.value = Resource.InProgress
            }
            .subscribe({ bagSummary ->
                venueRepository.clearSelectedVenue()
                _checkoutObservable.value = Resource.Success(bagSummary)
            }, { error ->
                _checkoutObservable.value = Resource.Error(error)
            })
        )
    }

    fun notifyCheckoutSuccess() {
        clearBag()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}