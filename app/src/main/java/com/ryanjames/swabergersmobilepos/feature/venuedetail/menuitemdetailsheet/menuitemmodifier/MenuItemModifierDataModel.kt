package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.ryanjames.swabergersmobilepos.core.StringBinder
import com.ryanjames.swabergersmobilepos.helper.*
import kotlin.math.max

class MenuItemModifierDataModel(
    private var minSelection: Int,
    private var maxSelection: Int,
    private val defaultSelections: List<String>,
    private val titleText: StringBinder,
    private val subTitleText: StringBinder?,
    val options: List<PickerItemAdapter.PickerItem> = listOf()
) {

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

    private val _userSelectionsObservable = MutableLiveData<HashSet<String>>().apply {
        value = defaultSelections.toHashSet()
    }
    val userSelectionsObservable: LiveData<HashSet<String>>
        get() = _userSelectionsObservable

    init {
        enableDisableCheckboxes()
    }

    val saveButtonEnabled: LiveData<Boolean> = Transformations.map(_userSelectionsObservable) { userSelectionList ->
        userSelectionList.size in minSelection..maxSelection
    }

    private val _title = MutableLiveData<StringBinder>(titleText)
    val title: LiveData<StringBinder>
        get() = _title

    private val _subtitle = MutableLiveData<StringBinder>(subTitleText)
    val subtitle: LiveData<StringBinder>
        get() = _subtitle

    private val _subtitleVisibility = MutableLiveData<Int>().apply {
        value = if (subTitleText == null) View.GONE else View.VISIBLE
    }
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


}