package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.helper.EventWrapper

class BottomSelectorFragmentViewModel(
    val requestId: String,
    val listener: BottomSelectorFragment.BottomSelectorListener
) : ViewModel() {

    var userSelectedId: String? = null
    var title = MutableLiveData<String>()

    private val _onClickContinueButton = MutableLiveData<EventWrapper<Boolean>>()

    val onClickContinueButtonObservable: LiveData<EventWrapper<Boolean>>
        get() = _onClickContinueButton

    fun onClickContinueButton(view: View) {
        userSelectedId?.let { listener.onSelectItem(requestId, it) }
        _onClickContinueButton.value = EventWrapper(true)
    }

    fun setTitle(text: String) {
        title.value = text
    }

}