package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.databinding.BottomSheetItemSelectorBinding
import com.ryanjames.swabergersmobilepos.helper.viewModelFactory
import java.lang.Exception

private const val EXTRA_REQUEST_ID = "extra.request.id"
private const val EXTRA_TITLE = "extra.title"
private const val EXTRA_OPTIONS = "extra.options"
private const val EXTRA_SELECTED_ID = "extra.selected.id"

class BottomSelectorFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetItemSelectorBinding
    private lateinit var options: List<BottomSelectorAdapter.BottomSelectorItem>
    private lateinit var listener: BottomSelectorListener
    private lateinit var viewModel: BottomSelectorFragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetItemSelectorBinding.inflate(inflater, container, false)

        val requestId = arguments?.getString(EXTRA_REQUEST_ID) ?: ""
        viewModel = ViewModelProviders.of(this, viewModelFactory { BottomSelectorFragmentViewModel(requestId, listener) })
            .get(BottomSelectorFragmentViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setTitle(arguments?.getString(EXTRA_TITLE) ?: getString(R.string.select))
        options = arguments?.getParcelableArrayList(EXTRA_OPTIONS) ?: listOf()
        setupRecyclerView()

        addSubscriptions()
    }

    private fun addSubscriptions() {
        viewModel.onClickContinueButtonObservable.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                dismiss()
            }
        })
    }


    private fun setupRecyclerView() {
        binding.rvOptions.also {
            it.layoutManager = LinearLayoutManager(context)
            val adapter = BottomSelectorAdapter(options, object : BottomSelectorAdapter.BottomSheetClickListener {
                override fun onSelectRow(id: String) {
                    viewModel.userSelectedId = id
                }
            })
            it.adapter = adapter

            // If user has already selected an item, select that item. Otherwise, the passed id in the bundle is selected
            if (viewModel.userSelectedId != null) {
                adapter.selectRow(viewModel.userSelectedId)
            } else {
                adapter.selectRow(arguments?.getString(EXTRA_SELECTED_ID))
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomSelectorListener) {
            listener = context
        } else {
            throw Exception("Activity should implement ${BottomSelectorListener::class.java.simpleName} interface")
        }
    }


    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag)
        }
    }

    interface BottomSelectorListener {
        fun onSelectItem(requestId: String, selectedItemId: String)
    }

    companion object {

        fun createInstance(
            requestId: String,
            title: String, options:
            ArrayList<BottomSelectorAdapter.BottomSelectorItem>,
            selectedId: String
        ): BottomSelectorFragment {
            val fragment = BottomSelectorFragment()
            val bundle = Bundle().apply {
                putString(EXTRA_REQUEST_ID, requestId)
                putString(EXTRA_TITLE, title)
                putParcelableArrayList(EXTRA_OPTIONS, options)
                putString(EXTRA_SELECTED_ID, selectedId)
            }
            fragment.arguments = bundle
            return fragment
        }

    }


}