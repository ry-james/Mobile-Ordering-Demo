package com.ryanjames.swabergersmobilepos.feature.orderhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.domain.OrderStatus
import com.ryanjames.swabergersmobilepos.helper.pluralize
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class OrderItemViewModel(val order: Order) : ViewModel() {

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String>
        get() = _storeName

    private val _description = MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    val placeholder = MutableLiveData<Int>(R.drawable.default_food_icon)
    val imageUrl = MutableLiveData(order.image)

    fun setup() {
        _storeName.value = order.storeName
        _description.value = String.format(
            "%s · %s\n%s · %s", "item".pluralize(order.noOfItems, "items"),
            "$".plus(order.price.toTwoDigitString()),
            order.formattedDate,
            getOrderStatus(order.status ?: OrderStatus.UNKNOWN)
        )
    }

    private fun getOrderStatus(status: OrderStatus): String {
        return when (status) {
            OrderStatus.CREATED -> "Pending"
            OrderStatus.CANCELLED -> "Cancelled"
            OrderStatus.CHECKOUT -> "Preparing"
            OrderStatus.UNKNOWN -> ""
            OrderStatus.PREPARING -> "Preparing"
            OrderStatus.DELIVERING -> "Delivering"
            OrderStatus.READY_FOR_PICKUP -> "Ready for pickup"
            OrderStatus.DELIVERED -> "Completed"
            OrderStatus.PICKED_UP -> "Completed"
        }
    }

}