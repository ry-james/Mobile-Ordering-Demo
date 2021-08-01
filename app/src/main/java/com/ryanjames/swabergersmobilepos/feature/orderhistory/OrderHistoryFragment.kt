package com.ryanjames.swabergersmobilepos.feature.orderhistory


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
import com.ryanjames.swabergersmobilepos.feature.venuedetail.VenueDetailActivity
import javax.inject.Inject

class OrderHistoryFragment : BaseFragment<FragmentOrderHistoryBinding>(R.layout.fragment_order_history) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: OrderHistoryViewModel by activityViewModels { viewModelFactory }

    private val orderHistoryAdapter = OrderHistoryAdapter(listOf(), object : OrderHistoryListener {
        override fun onClickViewOrder(orderId: String) {
            activity?.supportFragmentManager?.let { OrderDetailsDialogFragment.display(it, orderId) }
        }

        override fun onClickViewStore(storeId: String) {
            startActivity(VenueDetailActivity.createIntent(requireContext(), storeId))
        }
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
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

        binding.swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.colorAccent)
        }.setOnRefreshListener {
            viewModel.swipeToRefreshOrderHistory()
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

        viewModel.isRefreshing.observe(viewLifecycleOwner, Observer { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        })
    }

}
