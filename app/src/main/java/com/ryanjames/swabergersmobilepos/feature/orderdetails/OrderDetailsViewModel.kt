package com.ryanjames.swabergersmobilepos.feature.orderdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class OrderDetailsViewModel @Inject constructor(val orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _orderSummary = MutableLiveData<BagSummary>()
    val getOrderSummary: LiveData<BagSummary>
        get() = _orderSummary

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal

    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

    fun retrieveOrder(orderId: String) {
        compositeDisposable.add(
            orderRepository.getOrderById(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { bagSummary ->
                        _orderSummary.value = bagSummary
                        updatePrices()
                    }, { error -> error.printStackTrace() }
                )
        )
    }

    private fun updatePrices() {
        _tax.value = getOrderSummary.value?.tax()?.toTwoDigitString() ?: "0.00"
        _subtotal.value = getOrderSummary.value?.subtotal()?.toTwoDigitString() ?: "0.00"
        _total.value = getOrderSummary.value?.price?.toTwoDigitString() ?: "0.00"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}