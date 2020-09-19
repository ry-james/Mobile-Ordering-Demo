package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.LoadingDialogBinding
import com.ryanjames.swabergersmobilepos.domain.OrderStatus
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _emptyBagVisibility = MutableLiveData<Int>()
    val emptyBagVisibility: LiveData<Int>
        get() = _emptyBagVisibility

    private val _serverIssueVisibility = MutableLiveData<Int>()
    val serverIssueVisibility: LiveData<Int>
        get() = _serverIssueVisibility

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _nonEmptyBagVisibility = MutableLiveData<Int>()
    val nonEmptyBagVisibility: LiveData<Int>
        get() = _nonEmptyBagVisibility

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal

    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

    private val _localBag = MutableLiveData<Resource<BagSummary>>()
    val getLocalBag: LiveData<Resource<BagSummary>>
        get() = _localBag

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    private val _checkoutObservable = MutableLiveData<Resource<BagSummary>>()
    val checkoutObservable: LiveData<Resource<BagSummary>>
        get() = _checkoutObservable

    init {
        updateBagVisibility()
    }

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _emptyBagVisibility.value = View.GONE
                    _nonEmptyBagVisibility.value = View.GONE
                    _serverIssueVisibility.value = View.GONE
                    setLoadingViewVisibility(View.VISIBLE)
                }
                .subscribe({ bagSummary ->
                    if (bagSummary == BagSummary.emptyBag) {
                        updateBagVisibility()
                    } else {
                        setLocalBag(bagSummary)
                        updateBagVisibility()
                        updatePrices()
                    }
                },
                    { error ->
                        error.printStackTrace()
                        _emptyBagVisibility.value = View.GONE
                        _nonEmptyBagVisibility.value = View.GONE
                        _serverIssueVisibility.value = View.VISIBLE
                        setLoadingViewVisibility(View.GONE)
                    })
        )
    }

    private fun localBag(): BagSummary? {
        val resource = getLocalBag.value
        if (resource is Resource.Success) {
            return resource.data.peekContent()
        }
        return null
    }

    private fun setLocalBag(bagSummary: BagSummary) {
        _localBag.value = Resource.Success(Event(bagSummary))
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = "Fetching bag...",
            textColor = R.color.colorWhite
        )
    }

    private fun updateBagVisibility() {
        setLoadingViewVisibility(View.GONE)
        if (localBag()?.lineItems?.isNotEmpty() == true) {
            _emptyBagVisibility.value = View.GONE
            _nonEmptyBagVisibility.value = View.VISIBLE
        } else {
            _emptyBagVisibility.value = View.VISIBLE
            _nonEmptyBagVisibility.value = View.GONE
        }
    }

    private fun updatePrices() {
        _tax.value = localBag()?.tax()?.toTwoDigitString() ?: "0.00"
        _subtotal.value = localBag()?.subtotal()?.toTwoDigitString() ?: "0.00"
        _total.value = localBag()?.price?.toTwoDigitString() ?: "0.00"
    }

    fun setBagSummary(bagSummary: BagSummary) {
        setLocalBag(bagSummary)
        updatePrices()
        updateBagVisibility()
    }

    fun clearBag() {
        setLocalBag(BagSummary(emptyList(), 0f, OrderStatus.UNKNOWN))
        updatePrices()
        updateBagVisibility()
    }

    fun checkout(customerName: String) {
        compositeDisposable.add(orderRepository.checkout(customerName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _checkoutObservable.value = Resource.InProgress
            }
            .subscribe({ bagSummary ->
                _checkoutObservable.value = Resource.Success(Event(bagSummary))
            }, { error ->
                _checkoutObservable.value = Resource.Error(Event(Exception(error)))
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}