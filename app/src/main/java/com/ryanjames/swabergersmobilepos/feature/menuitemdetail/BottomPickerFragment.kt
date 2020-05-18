package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.databinding.BottomSheetItemSelectorBinding
import com.ryanjames.swabergersmobilepos.helper.viewModelFactory


private const val EXTRA_REQUEST_ID = "extra.request.id"
private const val EXTRA_TITLE = "extra.title"
private const val EXTRA_SUBTITLE = "extra.subtitle"
private const val EXTRA_OPTIONS = "extra.options"
private const val EXTRA_SELECTED_ID = "extra.selected.id"
private const val EXTRA_MIN_SELECTION = "extra.min.selection"
private const val EXTRA_MAX_SELECTION = "extra.max.selection"
private const val DEFAULT_MIN_SELECTION = 1
private const val DEFAULT_MAX_SELECTION = 1

class BottomPickerFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetItemSelectorBinding
    private lateinit var options: List<BottomPickerAdapter.BottomPickerItem>
    private lateinit var listener: BottomPickerListener
    private lateinit var viewModel: BottomPickerFragmentViewModel
    private lateinit var pickerAdapter: BottomPickerAdapter
    private var minSelection: Int = 1
    private var maxSelection: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetItemSelectorBinding.inflate(inflater, container, false)

        val requestId = arguments?.getString(EXTRA_REQUEST_ID) ?: ""
        minSelection = arguments?.getInt(EXTRA_MIN_SELECTION) ?: DEFAULT_MIN_SELECTION
        maxSelection = arguments?.getInt(EXTRA_MAX_SELECTION) ?: DEFAULT_MAX_SELECTION
        val defaultSelections = arguments?.getStringArrayList(EXTRA_SELECTED_ID) ?: arrayListOf()
        viewModel = ViewModelProviders.of(this, viewModelFactory { BottomPickerFragmentViewModel(requestId, minSelection, maxSelection, defaultSelections, listener) })
            .get(BottomPickerFragmentViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setTitle(arguments?.getString(EXTRA_TITLE) ?: getString(R.string.select))
        arguments?.getString(EXTRA_SUBTITLE)?.let { viewModel.setSubtitle(it) }
        options = arguments?.getParcelableArrayList(EXTRA_OPTIONS) ?: listOf()
        setupRecyclerView()
        fullyExpandBottomSheet()
        addSubscriptions()
    }

    private fun fullyExpandBottomSheet() {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = dialog as BottomSheetDialog
            val frameLayout = bottomSheet.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            frameLayout?.height?.let { bottomSheetBehavior.peekHeight = it }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun addSubscriptions() {
        viewModel.onClickContinueButtonObservable.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                dismiss()
            }
        })

        viewModel.userSelectionsObservable.observe(this, Observer { selections ->
            pickerAdapter.setSelectedRows(selections.toList())
        })


        viewModel.enableCheckboxesObservable.observe(this, Observer { enable ->
            if (enable) pickerAdapter.enableSelections() else pickerAdapter.disableSelections()
        })


    }


    private fun setupRecyclerView() {
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(context)
            pickerAdapter = BottomPickerAdapter(options, object : BottomPickerAdapter.BottomSheetClickListener {
                override fun onSelectPickerRow(id: String) {
                    viewModel.selectOrRemove(id)
                }
            }, viewModel.isSingleSelection)
            adapter = pickerAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomPickerListener) {
            listener = context
        } else {
            throw Exception("Activity should implement ${BottomPickerListener::class.java.simpleName} interface")
        }
    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag)
        }
    }

    interface BottomPickerListener {
        fun onUpdatePickerSelections(requestId: String, selectedItemIds: List<String>)
    }

    companion object {

        fun createInstance(
            requestId: String,
            title: String,
            subtitle: String?,
            minSelection: Int,
            maxSelection: Int,
            options: ArrayList<BottomPickerAdapter.BottomPickerItem>,
            selectedId: ArrayList<String>
        ): BottomPickerFragment {
            val fragment = BottomPickerFragment()
            val bundle = Bundle().apply {
                putString(EXTRA_REQUEST_ID, requestId)
                putString(EXTRA_TITLE, title)
                putString(EXTRA_SUBTITLE, subtitle)
                putInt(EXTRA_MIN_SELECTION, minSelection)
                putInt(EXTRA_MAX_SELECTION, maxSelection)
                putParcelableArrayList(EXTRA_OPTIONS, options)
                putStringArrayList(EXTRA_SELECTED_ID, selectedId)
            }
            fragment.arguments = bundle
            return fragment
        }

    }


}