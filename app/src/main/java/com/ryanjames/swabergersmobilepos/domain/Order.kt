package com.ryanjames.swabergersmobilepos.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(val lineItems: MutableList<LineItem>) : Parcelable {

    val total: Float
        get() {
            var sum = 0f
            for (lineItem in lineItems) {
                sum += lineItem.price
            }
            return sum
        }

    val subTotal: Float
        get() = total / 1.12f

    val tax: Float
        get() = subTotal * 0.12f

    val noOfItems: Int
        get() = lineItems.map { it.quantity }.sum()

}