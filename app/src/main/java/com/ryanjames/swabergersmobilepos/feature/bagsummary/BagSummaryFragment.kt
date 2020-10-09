package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.REQUEST_LINEITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_ADD_OR_UPDATE_ITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_REMOVE_ITEM
import javax.inject.Inject

class BagSummaryFragment : BaseFragment<FragmentBagSummaryBinding>(R.layout.fragment_bag_summary) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: BagSummaryViewModel
    private lateinit var fragmentCallback: BagSummaryFragmentCallback
    private lateinit var adapter: BagItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(BagSummaryViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        subscribe()
        viewModel.retrieveLocalBag()
    }

    private fun subscribe() {

        viewModel.getLocalBag.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.event.handleEvent { bagSummary ->
                        adapter.updateBag(bagSummary)
                    }
                }
            }

        })

        viewModel.onClearBag.observe(viewLifecycleOwner, Observer {
            adapter.clear()
        })

        viewModel.checkoutObservable.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.event.handleEvent {
                        (activity as BaseActivity).hideLoadingDialog()
                        AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setMessage(getString(R.string.checkout_successful))
                            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                                viewModel.clearBag()
                                dialog.dismiss()
                            }.show()
                    }
                }
                is Resource.Error -> {
                    resource.event.handleEvent {
                        (activity as BaseActivity).hideLoadingDialog()
                        AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setMessage(getString(R.string.checkout_failure))
                            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    }
                }
                Resource.InProgress -> (activity as BaseActivity).showLoadingDialog("Checking out")
            }

        })
    }

    private fun setupListeners() {
        binding.btnCheckout.setOnClickListener {
            CheckoutFragment().show(childFragmentManager, CheckoutFragment::class.simpleName)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            fragmentCallback = context as BagSummaryFragmentCallback
        } catch (e: ClassCastException) {
            throw ClassCastException("Host activity should implement ${BagSummaryFragmentCallback::class.java.simpleName}")
        }
    }

    private fun setupRecyclerView() {
        adapter = BagItemAdapter(listOf(), object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: BagLineItem) {
                startActivityForResult(MenuItemDetailActivity.createIntent(activity, lineItem), REQUEST_LINEITEM)
            }
        })
        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = this.adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LINEITEM && resultCode == RESULT_ADD_OR_UPDATE_ITEM) {
            data?.let {
                val bagSummary = MenuItemDetailActivity.getBagSummaryExtra(data)
                viewModel.setBagSummary(bagSummary)
                fragmentCallback.onUpdateLineItem()
            }
        } else if (requestCode == REQUEST_LINEITEM && resultCode == RESULT_REMOVE_ITEM) {
            data?.let {
                val bagSummary = MenuItemDetailActivity.getBagSummaryExtra(data)
                viewModel.setBagSummary(bagSummary)
                fragmentCallback.onRemoveLineItem()
            }
        }
    }

    interface BagSummaryFragmentCallback {
        fun onUpdateLineItem()
        fun onRemoveLineItem()
    }

    companion object {
        private val outStateBundle = Bundle()
    }

}
