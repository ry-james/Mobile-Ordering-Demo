package com.ryanjames.swabergersmobilepos.feature.bagsummary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.OrderDetails
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class BagSummaryViewModel(orderDetails: OrderDetails) : ViewModel() {

    var orderDetails: OrderDetails = orderDetails
        set(value) {
            field = value
            _tax.value = value.tax.toTwoDigitString()
            _subtotal.value = value.subTotal.toTwoDigitString()
            _total.value = "PHP. ${value.total.toTwoDigitString()}"
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


}