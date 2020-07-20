package com.ryanjames.swabergersmobilepos.feature.orderhistory


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
import com.ryanjames.swabergersmobilepos.core.MarginItemDecoration
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentOrderHistoryBinding
import javax.inject.Inject

private const val EXTRA_RV_STATE = "rv.state"

class OrderHistoryFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: FragmentOrderHistoryBinding
    private lateinit var viewModel: OrderHistoryViewModel

    private val orderHistoryAdapter = OrderHistoryAdapter(listOf())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_history, container, false)
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(OrderHistoryViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        subscribe()
        setupRecyclerView()

        viewModel.retrieveOrderHistory()

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

    private fun setupRecyclerView() {
        binding.rvOrderHistory.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryFragment.activity)
            adapter = orderHistoryAdapter
            addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.default_rv_item_margin).toInt()))
        }
    }

    private fun subscribe() {
        viewModel.onRetrieveOrderHistory.observe(this, Observer {
            orderHistoryAdapter.updateOrderHistory(it)
        })
    }

    companion object {
        private val outStateBundle = Bundle()
    }

}
