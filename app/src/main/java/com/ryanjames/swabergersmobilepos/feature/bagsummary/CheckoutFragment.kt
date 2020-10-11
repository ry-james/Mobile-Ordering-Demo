package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.ryanjames.swabergersmobilepos.core.FullyExpandedBottomSheetFragment
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentCheckoutBinding
import javax.inject.Inject

class CheckoutFragment : FullyExpandedBottomSheetFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentCheckoutBinding
    private val viewModel: BagSummaryViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        MobilePosDemoApplication.appComponent.inject(this)
        binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etCustomerName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnCheckout.isEnabled = !binding.etCustomerName.text.isNullOrBlank()
            }
        })

        binding.btnCheckout.setOnClickListener {
            viewModel.checkout(binding.etCustomerName.text.toString())
            dismiss()
        }

    }
}