package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class BagItemViewModel(val lineItem: BagLineItem) : ViewModel() {

    private val _quantity = MutableLiveData<String>()
    val quantity: LiveData<String>
        get() = _quantity

    private val _productName = MutableLiveData<String>()
    val productName: LiveData<String>
        get() = _productName

    private val _modifiers = MutableLiveData<String>()
    val modifiers: LiveData<String>
        get() = _modifiers

    private val _modifiersVisibility = MutableLiveData<Int>()
    val modifiersVisibility: LiveData<Int>
        get() = _modifiersVisibility

    private val _price = MutableLiveData<String>()
    val price: LiveData<String>
        get() = _price

    private val _lineVisibility = MutableLiveData<Int>().apply { View.VISIBLE }
    val lineVisibility: LiveData<Int>
        get() = _lineVisibility


    fun setup(isLastItem: Boolean) {
        _quantity.value = lineItem.quantity.toString()

        _productName.value = lineItem.lineItemName

        _price.value = lineItem.price.toTwoDigitString()

        if (isLastItem) {
            _lineVisibility.value = View.GONE
        } else {
            _lineVisibility.value = View.VISIBLE
        }

        if (lineItem.modifiersDisplay.isNotEmpty()) {
            _modifiersVisibility.value = View.VISIBLE
            _modifiers.value = lineItem.modifiersDisplay
        } else {
            _modifiersVisibility.value = View.GONE
        }
//
//        if (lineItem.modifiers.size > 0 || lineItem.productsInBundle.size > 0) {
//
//            val modifierText = StringBuilder()
//            // Product modifiers
//            val productModifiers = getModifiers(lineItem.product)
//            for ((index, modifier) in productModifiers.withIndex()) {
//                modifierText.append(modifier.modifierName)
//                if (productModifiers.size - 1 != index) {
//                    modifierText.append(", ")
//                }
//            }
//
//            // Bundle product modifiers
//            for ((key, products) in lineItem.productsInBundle) {
//                for (product in products) {
//                    modifierText.append("\n")
//                    modifierText.append(product.productName)
//
//                    val modifiers = getModifiers(product)
//
//                    if (modifiers.isNotEmpty()) {
//                        modifierText.append(" - ")
//                    }
//
//                    for ((modifierIndex, modifier) in modifiers.withIndex()) {
//                        modifierText.append(modifier.modifierName)
//                        if (modifiers.size - 1 != modifierIndex) {
//                            modifierText.append(", ")
//                        }
//                    }
//
//                }
//            }
//
//            _modifiersVisibility.value = View.VISIBLE
//            _modifiers.value = modifierText.toString().trim()
//
//        } else {
//            _modifiersVisibility.value = View.GONE
//        }

    }

}