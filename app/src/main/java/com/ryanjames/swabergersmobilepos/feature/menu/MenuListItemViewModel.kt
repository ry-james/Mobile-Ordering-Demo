package com.ryanjames.swabergersmobilepos.feature.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

class MenuListItemViewModel(
    val product: Product,
    val onClickMenuItem: (product: Product) -> Unit
) : ViewModel() {

    val strProductName = MutableLiveData<String>()
    val strPrice = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String?>()
    val placeholder = MutableLiveData<Int>(R.drawable.default_food_icon)

    fun setupViewModel() {
        strProductName.value = product.productName
        strPrice.value = "$" + product.price.toTwoDigitString()
        imageUrl.value = product.imageUrl
    }

    fun onClickMenuItem() {
        onClickMenuItem.invoke(product)
    }

}