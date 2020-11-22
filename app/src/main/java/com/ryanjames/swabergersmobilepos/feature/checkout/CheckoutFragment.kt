package com.ryanjames.swabergersmobilepos.feature.checkout

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.FullyExpandedBottomSheetFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentCheckoutBinding
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryViewModel
import com.ryanjames.swabergersmobilepos.helper.DialogManager
import javax.inject.Inject

class CheckoutFragment : FullyExpandedBottomSheetFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val dialogManager by lazy { DialogManager(viewLifecycleOwner.lifecycle, activity) }

    private lateinit var binding: FragmentCheckoutBinding
    private val viewModel: BagSummaryViewModel by activityViewModels { viewModelFactory }
    private val checkoutViewModel: CheckoutViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MobilePosDemoApplication.appComponent.inject(this)
        binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        binding.checkoutViewModel = checkoutViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe()
    }

    private fun subscribe() {
        checkoutViewModel.checkoutObservable.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.event.handleEvent {

                        dialogManager.hideLoadingDialog()
                        AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setMessage(getString(R.string.checkout_successful))
                            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                                dialog.dismiss()
                                dismiss()
                                viewModel.notifyCheckoutSuccess()
                            }.show()
                    }
                }
                is Resource.Error -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        AlertDialog.Builder(activity)
                            .setCancelable(false)
                            .setMessage(getString(R.string.checkout_failure))
                            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    }
                }
                Resource.InProgress -> dialogManager.showLoadingDialog("Checking out")
            }
        })
    }
}