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
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentOrderHistoryBinding
import com.ryanjames.swabergersmobilepos.feature.orderdetails.OrderDetailsDialogFragment
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryAdapter.OrderHistoryListener
import javax.inject.Inject

private const val EXTRA_RV_STATE = "rv.state"

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
        SwabergersApplication.appComponent.inject(this)

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(OrderHistoryViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.rvOrderHistory.layoutManager?.onRestoreInstanceState(outStateBundle.getParcelable(EXTRA_RV_STATE))
    }

    override fun onPause() {
        super.onPause()
        outStateBundle.putParcelable(EXTRA_RV_STATE, binding.rvOrderHistory.layoutManager?.onSaveInstanceState())
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
        viewModel.onRetrieveOrderHistory.observe(viewLifecycleOwner, Observer {
            orderHistoryAdapter.updateOrderHistory(it)
        })
    }

    companion object {
        private val outStateBundle = Bundle()
    }

}
