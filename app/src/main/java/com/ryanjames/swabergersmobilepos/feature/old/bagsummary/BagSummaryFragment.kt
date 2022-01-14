package com.ryanjames.swabergersmobilepos.feature.old.bagsummary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.checkout.CheckoutFragment
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.REQUEST_LINEITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_ADD_OR_UPDATE_ITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_REMOVE_ITEM
import com.ryanjames.swabergersmobilepos.feature.venuefinder.VenueFinderActivity
import javax.inject.Inject

private const val REQUEST_VENUE = 0

class BagSummaryFragment : BaseFragment<FragmentBagSummaryBinding>(R.layout.fragment_bag_summary) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BagSummaryViewModel by activityViewModels { viewModelFactory }
    private lateinit var fragmentCallback: BagSummaryFragmentCallback
    private lateinit var adapter: BagItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        subscribe()
        viewModel.retrieveLocalBag()

        binding.layoutSelectedLocation.container.setOnClickListener {
            startVenueFinderActivity()
        }
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

        viewModel.onOrderNotFound.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent {
                dialogManager.showDismissableDialog(getString(R.string.error_not_found))
            }
        })

        viewModel.removeModeToggle.observe(viewLifecycleOwner, Observer { isRemoving ->
            adapter.setRemovingMode(isRemoving)
        })

        viewModel.onRemovingItems.observe(viewLifecycleOwner, Observer { resource ->

            when (resource) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog(getString(R.string.removing_loading))
                }
                is Resource.Success -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.removing_item_success))
                    }
                }
                is Resource.Error -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.removing_item_fail))
                    }
                }
            }
        })
    }

    private fun startVenueFinderActivity() {
        startActivityForResult(VenueFinderActivity.createIntent(activity, viewModel.getSelectedVenue()), REQUEST_VENUE)
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
        adapter = BagItemAdapter(listOf(), viewModel.itemsForRemovalList, object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: BagLineItem) {
                startActivityForResult(MenuItemDetailActivity.createIntent(activity, lineItem), REQUEST_LINEITEM)
            }

            override fun onRemoveCbCheckedChanged(bagLineItem: BagLineItem, checked: Boolean) {
                if (checked) {
                    viewModel.addItemForRemoval(bagLineItem)
                } else {
                    viewModel.removeItemForRemoval(bagLineItem)
                }
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
        } else if (requestCode == REQUEST_VENUE && resultCode == Activity.RESULT_OK) {
            VenueFinderActivity.getSelectedVenueFromIntent(data)?.let { venue ->
                viewModel.setSelectedVenue(venue)
                viewModel.retrieveLocalBag()
            }
        }
    }

    interface BagSummaryFragmentCallback {
        fun onUpdateLineItem()
        fun onRemoveLineItem()
    }

}
