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

    private val _enableCheckboxesObservable = MutableLiveData<Boolean>()
    val enableCheckboxesObservable: LiveData<Boolean>
        get() = _enableCheckboxesObservable

    private val _userSelectionsObservable = MutableLiveData<HashSet<String>>().apply { value = defaultSelections.toHashSet() }
    val userSelectionsObservable: LiveData<HashSet<String>>
        get() = _userSelectionsObservable

    init {
        enableDisableCheckboxes()
    }

    val continueButtonEnabled: LiveData<Boolean> = Transformations.map(_userSelectionsObservable) { userList ->
        userList.size in minSelection..maxSelection
    }

    private val _title = MutableLiveData<String>()
    val title: LiveData<String>
        get() = _title

    private val _subtitle = MutableLiveData<String>()
    val subtitle: LiveData<String>
        get() = _subtitle

    private val _subtitleVisibility = MutableLiveData<Int>().apply { value = View.GONE }
    val subtitleVisibility: LiveData<Int>
        get() = _subtitleVisibility

    private val _onClickContinueButton = MutableLiveData<Event<Boolean>>()
    val onClickContinueButtonObservable: LiveData<Event<Boolean>>
        get() = _onClickContinueButton

    fun selectOrRemove(id: String) {

        if (isSingleSelection) {
            _userSelectionsObservable.value?.clear()
            _userSelectionsObservable.add(id)
            return
        }

        // If multiple selections are allowed
        if (_userSelectionsObservable.value().contains(id)) {
            _userSelectionsObservable.remove(id)
            enableDisableCheckboxes()
        } else if (_userSelectionsObservable.size() < maxSelection) {
            _userSelectionsObservable.add(id)
            enableDisableCheckboxes()
        }

    }

    private fun enableDisableCheckboxes() {
        if (_userSelectionsObservable.size() >= maxSelection) {
            _enableCheckboxesObservable.value = false
        } else if (_enableCheckboxesObservable.value == false) {
            _enableCheckboxesObservable.value = true
        }
    }

    fun onClickContinueButton(view: View) {
        _userSelectionsObservable.value?.let { listener.onUpdatePickerSelections(requestId, it.toList()) }
        _onClickContinueButton.value = Event(true)
    }

    fun setTitle(text: String) {
        _title.value = text
    }

    fun setSubtitle(text: String) {
        _subtitle.value = text
        if (text.isBlank()) {
            _subtitleVisibility.value = View.GONE
        } else {
            _subtitleVisibility.value = View.VISIBLE
        }
    }

}