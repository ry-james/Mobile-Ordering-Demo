package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.core.StringBinder

class PickerItemViewModel(
    val item: PickerItemAdapter.PickerItem,
    private val singleSelection: Boolean,
    private val onClickListener: (PickerItemAdapter.PickerItem) -> Unit
) : ViewModel() {

    val name = MutableLiveData<StringBinder>()
    val descriptionVisibility = MutableLiveData<Int>()
    val description = MutableLiveData<StringBinder>()
    val checked = MutableLiveData<Boolean>().apply { value = false }
    val enabled = MutableLiveData<Boolean>().apply { value = true }
    val radioButtonVisibility = MutableLiveData<Int>()
    val checkboxVisibility = MutableLiveData<Int>()

    fun setupViewModel() {
        name.value = item.name
        if (item.desc == null) {
            descriptionVisibility.value = View.GONE
        } else {
            description.value = item.desc
            descriptionVisibility.value = View.VISIBLE
        }

        if (singleSelection) {
            radioButtonVisibility.value = View.VISIBLE
            checkboxVisibility.value = View.GONE
        } else {
            radioButtonVisibility.value = View.GONE
            checkboxVisibility.value = View.VISIBLE
        }
    }

    fun check() {
        checked.value = true
    }

    fun uncheck() {
        checked.value = false
    }

    fun enable() {
        enabled.value = true
    }

    fun disable() {
        enabled.value = false
    }

    fun onClickItem(view: View) {
        onClickListener.invoke(item)
    }


}
