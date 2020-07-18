package com.ryanjames.swabergersmobilepos.feature.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class MenuListItemViewModel(
    val product: Product,
    val onClickMenuItem: (product: Product) -> Unit
) : ViewModel() {

    val strProductName = MutableLiveData<String>()
    val strPrice = MutableLiveData<String>()

    fun setupViewModel() {
        strProductName.value = product.productName
        strPrice.value = product.price.toTwoDigitString()
    }

    fun onClickMenuItem() {
        onClickMenuItem.invoke(product)
    }

}