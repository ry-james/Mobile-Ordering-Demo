package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.REQUEST_LINE_ITEM
import javax.inject.Inject

class BagSummaryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentBagSummaryBinding
    private lateinit var viewModel: BagSummaryViewModel

    private lateinit var adapter: BagItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bag_summary, container, false)
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(BagSummaryViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        setupRecyclerView()
        viewModel.retrieveLocalBag()
        subscribe()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveLocalBag()
    }

    private fun subscribe() {
        viewModel.onOrderSucceeded.observe(this, Observer { event ->
            if (event.getContentIfNotHandled() == true) {
                AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.order_created_message))
                    .setPositiveButton(R.string.ok_cta) { dialogInterface, _ ->
                        viewModel.clearBag()
                        dialogInterface.dismiss()
                    }.setCancelable(false)
                    .show()
            }
        })

        viewModel.orderFailed.observe(this, Observer { event ->
            if (event.getContentIfNotHandled() == true) {
                AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.something_went_wrong))
                    .setPositiveButton(getString(R.string.try_again_cta)) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        viewModel.postOrder()
                    }
                    .setNegativeButton(getString(R.string.later_cta)) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .show()
            }
        })

        viewModel.getLocalBag.observe(this, Observer { order ->
            adapter.updateLineItems(order.lineItems)
        })
    }


    private fun setupRecyclerView() {
        adapter = BagItemAdapter(listOf(), object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: LineItem) {
                startActivityForResult(MenuItemDetailActivity.createIntent(activity, lineItem), REQUEST_LINE_ITEM)
            }

        })
        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = this.adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LINE_ITEM && resultCode == Activity.RESULT_OK) {
            data?.let {
                val lineItem = MenuItemDetailActivity.getExtraLineItem(data)
                viewModel.putLineItem(lineItem)
            }
        }
    }

}
