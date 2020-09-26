package com.ryanjames.swabergersmobilepos.feature.orderhistory


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.MarginItemDecoration
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentOrderHistoryBinding
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.orderdetails.OrderDetailsDialogFragment
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryAdapter.OrderHistoryListener
import javax.inject.Inject

class OrderHistoryFragment : BaseFragment<FragmentOrderHistoryBinding>(R.layout.fragment_order_history) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: OrderHistoryViewModel

    private val orderHistoryAdapter = OrderHistoryAdapter(listOf(), object : OrderHistoryListener {
        override fun onClickViewOrder(orderId: String) {
            activity?.supportFragmentManager?.let { OrderDetailsDialogFragment.display(it, orderId) }
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(OrderHistoryViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe()
        setupRecyclerView()
        viewModel.retrieveOrderHistory()
    }

    private fun setupRecyclerView() {
        binding.rvOrderHistory.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryFragment.activity)
            adapter = orderHistoryAdapter
            addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_rv_item_margin).toInt()))
        }
    }

    private fun subscribe() {
        viewModel.onRetrieveOrderHistory.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    val orderList = result.data
                    orderHistoryAdapter.updateOrderHistory(orderList)
                }
            }

        })
    }

}
