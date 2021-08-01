package com.ryanjames.swabergersmobilepos.feature.orderdetails

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentOrderDetailsBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.old.bagsummary.BagItemAdapter
import com.ryanjames.swabergersmobilepos.helper.DialogManager
import kotlinx.android.synthetic.main.fragment_order_details.*
import javax.inject.Inject

private const val EXTRA_ORDER_ID = "extra.order.id"

class OrderDetailsDialogFragment : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentOrderDetailsBinding

    private val dialogManager by lazy { DialogManager(viewLifecycleOwner.lifecycle, activity) }

    private val viewModel: OrderDetailsViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_details, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = getString(R.string.order_details)
        toolbar.setNavigationOnClickListener { dismiss() }
        subscribe()
        arguments?.getString(EXTRA_ORDER_ID)?.let { orderId ->
            viewModel.retrieveOrder(orderId)
        }

        binding.btnCancel.setOnClickListener {
            AlertDialog.Builder(activity)
                .setMessage(getString(R.string.confirm_cancel))
                .setPositiveButton(R.string.cta_yes) { _, _ ->
                    viewModel.cancelOrder()
                }.setNegativeButton(R.string.cta_no) { dialog, _ ->
                    dialog.dismiss()
                }.setCancelable(false).show()
        }
    }

    private fun subscribe() {
        viewModel.getOrderSummary.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> setupRecyclerView(resource.data.lineItems)
            }
        })

        viewModel.onOrderCancelled.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.InProgress -> dialogManager.showLoadingDialog(getString(R.string.cancelling_order))
                is Resource.Error -> {
                    dialogManager.hideLoadingDialog()
                    dialogManager.showDismissableDialog(getString(R.string.error_cancelling_order))
                }
                is Resource.Success -> {
                    dialogManager.hideLoadingDialog()
                    AlertDialog.Builder(activity)
                        .setMessage(getString(R.string.order_cancelled))
                        .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                            dialog.dismiss()
                            dismiss()
                        }.setCancelable(false).show()
                }
            }
        })
    }

    private fun setupRecyclerView(orderItems: List<BagLineItem>) {
        val adapter = BagItemAdapter(orderItems, listOf(), object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: BagLineItem) {

            }

            override fun onRemoveCbCheckedChanged(bagLineItem: BagLineItem, checked: Boolean) {

            }
        })
        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(activity)
            it.adapter = adapter
        }
    }

    companion object {

        private const val FULL_SCREEN_DIALOG_TAG = "Order Details Dialog"

        fun display(supportFragmentManager: FragmentManager, orderId: String): OrderDetailsDialogFragment {

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val previous = supportFragmentManager.findFragmentByTag(FULL_SCREEN_DIALOG_TAG)
            if (previous != null) {
                fragmentTransaction.remove(previous)
            }
            fragmentTransaction.addToBackStack(null)

            val fullScreenDialog = OrderDetailsDialogFragment()
            fullScreenDialog.arguments = Bundle().apply {
                putString(EXTRA_ORDER_ID, orderId)
            }

            if (supportFragmentManager.findFragmentByTag(FULL_SCREEN_DIALOG_TAG)?.isVisible != true) {
                fullScreenDialog.show(supportFragmentManager, FULL_SCREEN_DIALOG_TAG)
            }
            return fullScreenDialog
        }

    }

}