package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.base.BaseActivity
import com.ryanjames.swabergersmobilepos.databinding.ActivityBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.OrderDetails

private const val EXTRA_ORDER = "extra.order"

class BagSummaryActivity : BaseActivity() {

    private lateinit var binding: ActivityBagSummaryBinding
    private lateinit var viewModel: BagSummaryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderDetails = intent.getParcelableExtra<OrderDetails>(EXTRA_ORDER)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bag_summary)
        viewModel = ViewModelProviders.of(this, viewModelFactory { BagSummaryViewModel(orderDetails) }).get(BagSummaryViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    companion object {

        fun createIntent(context: Context, orderDetails: OrderDetails): Intent {
            val intent = Intent(context, BagSummaryActivity::class.java)
            intent.putExtra(EXTRA_ORDER, orderDetails)
            return intent
        }

    }
}
