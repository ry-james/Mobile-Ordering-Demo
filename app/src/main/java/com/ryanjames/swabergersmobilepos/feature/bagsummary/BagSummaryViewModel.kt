package com.ryanjames.swabergersmobilepos.feature.bagsummary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.OrderDetails
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    var orderDetails: OrderDetails = OrderDetails(mutableListOf())
        set(value) {
            field = value
            updatePrices()
        }

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal


    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

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

    }

}