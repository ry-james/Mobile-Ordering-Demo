package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentMenuItemModifierBinding
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent.MenuItemBottomSheetViewModel
import javax.inject.Inject

class MenuItemModifierFragment : BaseFragment<FragmentMenuItemModifierBinding>(R.layout.fragment_menu_item_modifier) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var pickerAdapter: PickerItemAdapter
    private val viewModel: MenuItemBottomSheetViewModel by viewModels(ownerProducer = { requireParentFragment() }, factoryProducer = { viewModelFactory })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MobilePosDemoApplication.appComponent.inject(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecylerView()
        subscribe()

        binding.btnCancel.setOnClickListener {
            viewModel.setViewPagerPosition(0)
        }

        binding.btnContinue.setOnClickListener {
            viewModel.saveSelection(pickerAdapter.getSelectedRows())
            viewModel.setViewPagerPosition(0)
        }
    }

    private fun setupRecylerView() {
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(context)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            refreshAdapter()
        }
    }

    private fun refreshAdapter() {
        pickerAdapter = PickerItemAdapter(object : PickerItemAdapter.PickerItemClickListener {
            override fun onSelectPickerRow(id: String) {
                viewModel.getMenuItemModifierDataModel()?.selectOrRemove(id)
            }
        }, viewModel.getMenuItemModifierDataModel()?.isSingleSelection ?: true)
        binding.rvOptions.adapter = pickerAdapter
    }

    private fun subscribe() {
        viewModel.onBindMenuItemModifierDataModel.observe(viewLifecycleOwner, Observer {
            refreshAdapter()
            binding.dataModel = it
            pickerAdapter.items = it.options

            it.userSelectionsObservable.observe(viewLifecycleOwner, Observer { selections ->
                pickerAdapter.setSelectedRows(selections.toList())
            })

            it.enableCheckboxesObservable.observe(viewLifecycleOwner, Observer { enable ->
                if (enable) pickerAdapter.enableSelections() else pickerAdapter.disableSelections()
            })
        })
    }

    companion object {
        fun createInstance(): MenuItemModifierFragment {
            return MenuItemModifierFragment()
        }
    }

}