package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.View
import androidx.lifecycle.*
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _nonEmptyBagVisibility = MutableLiveData<Int>()
    val nonEmptyBagVisibility: LiveData<Int>
        get() = _nonEmptyBagVisibility

    private val _errorViewBinding = MutableLiveData<ErrorViewBinding>()
    val errorViewBinding: LiveData<ErrorViewBinding>
        get() = _errorViewBinding

    private val _localBag = MutableLiveData<Resource<BagSummary>>()
    val getLocalBag: LiveData<Resource<BagSummary>>
        get() = _localBag

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    val tax: LiveData<String> = Transformations.map(_localBag) {
        localBag()?.tax()?.toTwoDigitString() ?: "0.00"
    }

    val subtotal: LiveData<String> = Transformations.map(_localBag) {
        localBag()?.subtotal()?.toTwoDigitString() ?: "0.00"
    }

    val total: LiveData<String> = Transformations.map(_localBag) {
        localBag()?.price?.toTwoDigitString() ?: "0.00"
    }

    private val _onOrderNotFound = MutableLiveData<Event<Boolean>>()
    val onOrderNotFound: LiveData<Event<Boolean>>
        get() = _onOrderNotFound

    private val _removeModeToggle = MutableLiveData<Boolean>(false)
    val removeModeToggle: LiveData<Boolean>
        get() = _removeModeToggle

    private val itemsForRemovalList = mutableListOf<BagLineItem>()
    val itemsForRemoval
        get() = itemsForRemovalList.toList()

    private val _onRemovingItems = MutableLiveData<Resource<Boolean>>()
    val onRemovingItems: LiveData<Resource<Boolean>>
        get() = _onRemovingItems

    private fun removeModeVisibility(): Int = if (removeModeToggle.value == true && !isBagEmpty()) View.VISIBLE else View.GONE

    val removeModeVisibility: LiveData<Int> = MediatorLiveData<Int>().apply {
        addSource(_removeModeToggle) { isRemoving ->
            this.value = removeModeVisibility()

            if (!isRemoving) {
                itemsForRemovalList.clear()
            }
        }
        addSource(_localBag) {
            this.value = removeModeVisibility()
        }
    }

    val viewModeVisibility: LiveData<Int> = Transformations.map(removeModeVisibility) { removeModeVisibility ->
        if (removeModeVisibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private val _removeSelectedBtnEnabled = MutableLiveData<Boolean>()
    val removeSelectedBtnEnabled: LiveData<Boolean>
        get() = _removeSelectedBtnEnabled

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _nonEmptyBagVisibility.value = View.GONE
                    setErrorViewVisibility(View.GONE)
                    setEmptyBagViewVisibility(View.GONE)
                    setLoadingViewVisibility(View.VISIBLE)
                }
                .doFinally {
                    setLoadingViewVisibility(View.GONE)
                }
                .subscribe({ bagSummary ->
                    if (bagSummary != BagSummary.emptyBag) {
                        setLocalBag(bagSummary)
                    }
                    updateUI()
                },
                    { error ->
                        error.printStackTrace()
                        handleError(error)
                    })
        )
    }

    private fun handleError(error: Throwable) {
        if (error is HttpException) {
            if (error.code() == 404) {
                clearBag()
                _onOrderNotFound.value = Event(true)
            } else {
                setEmptyBagViewVisibility(View.GONE)
                _nonEmptyBagVisibility.value = View.GONE
                setErrorViewVisibility(View.VISIBLE)
            }
        }
    }

    private fun localBag(): BagSummary? {
        val resource = getLocalBag.value
        if (resource is Resource.Success) {
            return resource.data
        }
        return null
    }

    private fun setLocalBag(bagSummary: BagSummary) {
        _localBag.value = Resource.Success(bagSummary)
        if (bagSummary.lineItems.isEmpty()) {
            _removeModeToggle.value = false
        }
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.fetching_bag,
            textColor = R.color.colorWhite
        )
    }

    private fun setEmptyBagViewVisibility(visibility: Int) {
        _errorViewBinding.value = ErrorViewBinding(
            visibility = visibility,
            image = R.drawable.ic_empty_bag,
            title = R.string.empty_bag_title,
            message = R.string.empty_bag_subtitle
        )
    }

    private fun setErrorViewVisibility(visibility: Int) {
        _errorViewBinding.value = ErrorViewBinding(
            visibility = visibility,
            image = R.drawable.ic_error,
            title = R.string.us_not_you,
            message = R.string.error_fetching_bag
        )
    }

    private fun updateUI() {
        updateBagVisibility()
        updateRemoveModeVisibility()
    }

    private fun isBagEmpty(): Boolean {
        return localBag()?.lineItems?.isEmpty() ?: true
    }

    private fun updateRemoveModeVisibility() {
        _removeSelectedBtnEnabled.value = itemsForRemovalList.size > 0
    }

    private fun updateBagVisibility() {
        if (isBagEmpty()) {
            setEmptyBagViewVisibility(View.VISIBLE)
            _nonEmptyBagVisibility.value = View.GONE
        } else {
            setEmptyBagViewVisibility(View.GONE)
            _nonEmptyBagVisibility.value = View.VISIBLE
        }
    }

    fun setBagSummary(bagSummary: BagSummary) {
        setLocalBag(bagSummary)
        updateUI()
    }

    fun clearBag() {
        orderRepository.clearLocalBag()
        setLocalBag(BagSummary(emptyList(), 0f, OrderStatus.UNKNOWN, ""))
        updateUI()
    }

    fun onClickRemove() {
        _removeModeToggle.value = true
        updateUI()
    }

    fun onClickRemoveSelected() {
        compositeDisposable.add(
            orderRepository.removeBagLineItems(itemsForRemoval)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _onRemovingItems.value = Resource.InProgress
                }
                .subscribe({ bagSummary ->
                    _onRemovingItems.value = Resource.Success(true)
                    _removeModeToggle.value = false
                    setLocalBag(bagSummary)
                    updateUI()
                }, { error ->
                    _onRemovingItems.value = Resource.Error(error)
                    error.printStackTrace()
                })
        )
    }

    fun onClickCancelRemove() {
        _removeModeToggle.value = false
        updateRemoveModeVisibility()
    }

    fun addItemForRemoval(bagLineItem: BagLineItem) {
        val item = itemsForRemovalList.find { it.lineItemId == bagLineItem.lineItemId }
        if (item == null) {
            itemsForRemovalList.add(bagLineItem)
        }
        updateRemoveModeVisibility()
    }

    fun removeItemForRemoval(bagLineItem: BagLineItem) {
        val item = itemsForRemovalList.find { it.lineItemId == bagLineItem.lineItemId }
        if (item != null) {
            itemsForRemovalList.remove(item)
        }
        updateRemoveModeVisibility()
    }

    fun notifyCheckoutSuccess() {
        clearBag()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}