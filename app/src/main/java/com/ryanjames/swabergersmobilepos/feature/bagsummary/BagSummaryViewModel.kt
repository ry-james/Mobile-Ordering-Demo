package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.BagSummary
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

    private val _onOrderSucceeded = MutableLiveData<Event<Boolean>>()
    val onOrderSucceeded: LiveData<Event<Boolean>>
        get() = _onOrderSucceeded

    private val _onOrderFailed = MutableLiveData<Event<Boolean>>()
    val orderFailed: LiveData<Event<Boolean>>
        get() = _onOrderFailed

    private val _localBag = MutableLiveData<BagSummary>()
    val getLocalBag: LiveData<BagSummary>
        get() = _localBag

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    var customerInput: String? = null

    init {
        updateBagVisibility()
    }

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bagSummary ->
                    _localBag.value = bagSummary
                    updateBagVisibility()
                    updatePrices()
                },
                    { error -> error.printStackTrace() })
        )
    }

    private fun updateBagVisibility() {
        if (getLocalBag.value?.lineItems?.isNotEmpty() == true) {
            _emptyBagVisibility.value = View.GONE
            _nonEmptyBagVisibility.value = View.VISIBLE
        } else {
            _emptyBagVisibility.value = View.VISIBLE
            _nonEmptyBagVisibility.value = View.GONE
        }
    }

    private fun updatePrices() {
        _tax.value = getLocalBag.value?.tax()?.toTwoDigitString() ?: "0.00"
        _subtotal.value = getLocalBag.value?.subtotal()?.toTwoDigitString() ?: "0.00"
        _total.value = getLocalBag.value?.price?.toTwoDigitString() ?: "0.00"
    }

    fun setBagSummary(bagSummary: BagSummary) {
        _localBag.value = bagSummary
        updatePrices()
        updateBagVisibility()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}