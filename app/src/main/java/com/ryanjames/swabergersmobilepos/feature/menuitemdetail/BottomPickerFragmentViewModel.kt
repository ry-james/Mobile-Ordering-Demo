package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.helper.*
import kotlin.math.max

class BottomPickerFragmentViewModel(
    private val requestId: String,
    private var minSelection: Int,
    private var maxSelection: Int,
    private val defaultSelections: List<String>,
    val listener: BottomPickerFragment.BottomPickerListener
) : ViewModel() {

    init {
        if (minSelection < 0)
            minSelection = 0
        maxSelection = max(minSelection, maxSelection)

    }

    val isSingleSelection: Boolean
        get() = minSelection == 1 && maxSelection == 1

    private val _userSelectionsObservable = MutableLiveData<HashSet<String>>().apply { value = defaultSelections.toHashSet() }

    val userSelectionsObservable: LiveData<HashSet<String>>
        get() = _userSelectionsObservable


    val enableCheckboxesObservable: LiveData<Boolean> = Transformations.map(_userSelectionsObservable) { set -> set.size < maxSelection }

    val continueButtonEnabled: LiveData<Boolean> = Transformations.map(_userSelectionsObservable) { userList ->
        userList.size in minSelection..maxSelection
    }

    var title = MutableLiveData<String>()

    private val _onClickContinueButton = MutableLiveData<EventWrapper<Boolean>>()

    val onClickContinueButtonObservable: LiveData<EventWrapper<Boolean>>
        get() = _onClickContinueButton

    fun selectOrRemove(id: String) {

        if (isSingleSelection) {
            _userSelectionsObservable.value?.clear()
            _userSelectionsObservable.add(id)
            return
        }

        if (_userSelectionsObservable.value().contains(id)) {
            _userSelectionsObservable.remove(id)
        } else if (_userSelectionsObservable.size() < maxSelection) {
            _userSelectionsObservable.add(id)
        }
    }

    fun onClickContinueButton(view: View) {
        _userSelectionsObservable.value?.let { listener.onUpdatePickerSelections(requestId, it.toList()) }
        _onClickContinueButton.value = EventWrapper(true)
    }

    fun setTitle(text: String) {
        title.value = text
    }

}