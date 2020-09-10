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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.REQUEST_LINEITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_ADD_OR_UPDATE_ITEM
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.RESULT_REMOVE_ITEM
import com.ryanjames.swabergersmobilepos.helper.trimAllWhitespace
import javax.inject.Inject

private const val EXTRA_RV_STATE = "rv.state"

class BagSummaryFragment : BaseFragment<FragmentBagSummaryBinding>(R.layout.fragment_bag_summary) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: BagSummaryViewModel
    private lateinit var fragmentCallback: BagSummaryFragmentCallback
    private lateinit var adapter: BagItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

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
        viewModel.onOrderSucceeded.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent {
                AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.order_created_message))
                    .setPositiveButton(R.string.ok_cta) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }.setCancelable(false)
                    .show()
            }
        })

        viewModel.orderFailed.observe(viewLifecycleOwner, Observer { event ->
            event.handleEvent {
                AlertDialog.Builder(activity)
                    .setMessage(getString(R.string.something_went_wrong))
                    .setPositiveButton(getString(R.string.try_again_cta)) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(getString(R.string.later_cta)) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .show()
            }
        })

        viewModel.getLocalBag.observe(viewLifecycleOwner, Observer { bagSummary ->
            adapter.updateBag(bagSummary)
        })

        viewModel.onClearBag.observe(viewLifecycleOwner, Observer {
            adapter.clear()
        })

        viewModel.checkoutObservable.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data.handleEvent {
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
                    it.exception.handleEvent {
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
                    viewModel.checkout(etCustomerName.text.toString().trimAllWhitespace())
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
