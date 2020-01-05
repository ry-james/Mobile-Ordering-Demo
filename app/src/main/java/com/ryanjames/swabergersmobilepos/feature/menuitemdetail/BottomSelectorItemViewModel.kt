package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BottomSelectorItemViewModel(
    val item: BottomSelectorAdapter.BottomSelectorItem,
    private val onClickListener: (BottomSelectorAdapter.BottomSelectorItem) -> Unit
) : ViewModel() {

    val name = MutableLiveData<String>()
    val descriptionVisibility = MutableLiveData<Int>()
    val description = MutableLiveData<String>()
    val checked = MutableLiveData<Boolean>()

    fun setupViewModel() {
        name.value = item.name
        if (item.desc == null) {
            descriptionVisibility.value = View.GONE
        } else {
            description.value = item.desc
            descriptionVisibility.value = View.VISIBLE
        }
    }

    fun check() {
        checked.value = true
    }

    fun uncheck() {
        checked.value = false
    }

    fun onClickItem(view: View) {
        onClickListener.invoke(item)
    }


}