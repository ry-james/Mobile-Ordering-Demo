package com.ryanjames.swabergersmobilepos.feature.orderhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class OrderItemViewModel(val order: Order) : ViewModel() {

    private val _orderId = MutableLiveData<String>()
    val orderId: LiveData<String>
        get() = _orderId

    private val _itemsText = MutableLiveData<String>()
    val itemsText: LiveData<String>
        get() = _itemsText

    private val _priceText = MutableLiveData<String>()
    val priceText: LiveData<String>
        get() = _priceText

    fun setup() {
        _orderId.value = order.orderId.takeLast(12)
        _itemsText.value = "Items : ${order.lineItems.sumBy { it.quantity }}"
        _priceText.value = "$${order.price.toTwoDigitString()}"
    }

}