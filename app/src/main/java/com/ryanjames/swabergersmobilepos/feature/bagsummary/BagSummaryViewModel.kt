package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.OrderDetails
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    var orderDetails: OrderDetails = OrderDetails(mutableListOf())
        set(value) {
            field = value
            updateBagVisibility()
            updatePrices()
        }

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

    init {
        updateBagVisibility()
    }

    private fun updateBagVisibility() {
        if (orderDetails.lineItems.isEmpty()) {
            _emptyBagVisibility.postValue(View.VISIBLE)
            _nonEmptyBagVisibility.postValue(View.GONE)
        } else {
            _emptyBagVisibility.postValue(View.GONE)
            _nonEmptyBagVisibility.postValue(View.VISIBLE)
        }

    }

    private fun updatePrices() {
        _tax.value = orderDetails.tax.toTwoDigitString()
        _subtotal.value = orderDetails.subTotal.toTwoDigitString()
        _total.value = "PHP. ${orderDetails.total.toTwoDigitString()}"
    }

    fun putLineItem(lineItem: LineItem) {
        for ((index, item) in orderDetails.lineItems.withIndex()) {
            if (item.id == lineItem.id) {
                orderDetails.lineItems[index] = lineItem
                updatePrices()
                orderRepository.updateLineItem(lineItem)
                return
            }
        }
        updateBagVisibility()
    }

    fun postOrder() {
        compositeDisposable.add(
            orderRepository.postOrder(orderDetails)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Log.d("ORDER", "CREATED") },
                    { it.printStackTrace() })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}