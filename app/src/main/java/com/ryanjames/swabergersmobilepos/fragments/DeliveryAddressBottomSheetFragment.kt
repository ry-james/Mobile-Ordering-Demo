package com.ryanjames.swabergersmobilepos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.databinding.FragmentDeliveryAddressBinding
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import javax.inject.Inject

class DeliveryAddressBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentDeliveryAddressBinding

    @Inject
    lateinit var orderRepository: OrderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        MobilePosDemoApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDeliveryAddressBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderRepository.getDeliveryAddressObservable().observe(viewLifecycleOwner, Observer {
            binding.etAddress.setText(it ?: "")
        })

        binding.btnSave.setOnClickListener(this::onClickSave)
    }

    private fun onClickSave(view: View) {
        orderRepository.setDeliveryAddress(binding.etAddress.text.toString())
        dismiss()
    }



    companion object {
        fun createInstance(): DeliveryAddressBottomSheetFragment {
            return DeliveryAddressBottomSheetFragment()
        }
    }
}