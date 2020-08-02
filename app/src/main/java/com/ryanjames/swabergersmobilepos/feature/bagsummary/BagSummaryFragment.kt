package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_ADD_OR_UPDATE
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_REMOVE
import com.ryanjames.swabergersmobilepos.helper.trimAllWhitespace
import javax.inject.Inject

class BagSummaryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentBagSummaryBinding
    private lateinit var viewModel: BagSummaryViewModel
    private lateinit var fragmentCallback: BagSummaryFragmentCallback
    private lateinit var adapter: BagItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bag_summary, container, false)
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(BagSummaryViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        setupRecyclerView()
        setupListeners()
        subscribe()
        viewModel.retrieveLocalBag()
        return binding.root
    }

    private fun subscribe() {
        viewModel.onOrderSucceeded.observe(this, Observer { event ->
            if (event.getContentIfNotHandled() == true) {
                AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.order_created_message))
                    .setPositiveButton(R.string.ok_cta) { dialogInterface, _ ->
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

        viewModel.onClearBag.observe(this, Observer {
            adapter.clear()
        })
    }

    private fun setupListeners() {
        binding.btnCheckout.setOnClickListener {
            showCustomerNameInputDialog()
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

    private fun showCustomerNameInputDialog() {
        val etCustomerName = EditText(activity).apply {
            setSingleLine(true)
            maxLines = 1
        }

        val dialog = AlertDialog.Builder(activity)
            .setMessage(getString(R.string.enter_customer_name_message))
            .setPositiveButton(getString(R.string.cta_set)) { dialogInterface, _ ->
                val inputText = etCustomerName.text.toString()
                if (!inputText.isBlank()) {
                    viewModel.customerInput = etCustomerName.text.toString().trimAllWhitespace()
                    viewModel.postOrder()
                }
                dialogInterface.dismiss()

            }
            .setView(etCustomerName)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !etCustomerName.text.isNullOrBlank()

        etCustomerName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !etCustomerName.text.isNullOrBlank()
            }
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
        if (requestCode == REQUEST_LINE_ITEM && resultCode == RESULT_ADD_OR_UPDATE) {
            data?.let {
                val lineItem = MenuItemDetailActivity.getExtraLineItem(data)
                viewModel.putLineItem(lineItem)
                fragmentCallback.onUpdateLineItem()
            }
        } else if (requestCode == REQUEST_LINE_ITEM && resultCode == RESULT_REMOVE) {
            data?.let {
                val lineItem = MenuItemDetailActivity.getExtraLineItem(data)
                viewModel.removeLineItem(lineItem)
                fragmentCallback.onRemoveLineItem()
            }
        }
    }

    interface BagSummaryFragmentCallback {
        fun onUpdateLineItem()
        fun onRemoveLineItem()
    }

}
