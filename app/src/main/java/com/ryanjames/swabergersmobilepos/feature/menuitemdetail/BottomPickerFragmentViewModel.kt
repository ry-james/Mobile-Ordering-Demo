package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.helper.EventWrapper

class BottomPickerFragmentViewModel(
    private val requestId: String,
    val listener: BottomPickerFragment.BottomPickerListener
) : ViewModel() {

    var userSelectedId: String? = null
    var title = MutableLiveData<String>()

    private val _onClickContinueButton = MutableLiveData<EventWrapper<Boolean>>()

    val onClickContinueButtonObservable: LiveData<EventWrapper<Boolean>>
        get() = _onClickContinueButton

    fun onClickContinueButton(view: View) {
        userSelectedId?.let { listener.onSelectPickerItem(requestId, it) }
        _onClickContinueButton.value = EventWrapper(true)
    }

    fun setTitle(text: String) {
        title.value = text
    }

}