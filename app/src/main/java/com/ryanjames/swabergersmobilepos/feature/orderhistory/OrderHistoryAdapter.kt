package com.ryanjames.swabergersmobilepos.feature.orderhistory

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.databinding.RowOrderCardBinding
import com.ryanjames.swabergersmobilepos.domain.Order

class OrderHistoryAdapter(orderList: List<Order>, val listener: OrderHistoryListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewModels = orderList.map { OrderItemViewModel(it) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowOrderCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderCardViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = viewModels.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OrderCardViewHolder) {
            holder.bind(viewModels[position])
        }
    }

    fun updateOrderHistory(orderList: List<Order>) {
        viewModels = orderList.map { OrderItemViewModel(it) }
        notifyDataSetChanged()
    }

    class OrderCardViewHolder(val binding: RowOrderCardBinding, val listener: OrderHistoryListener) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: OrderItemViewModel) {
            binding.viewModel = viewModel
            viewModel.setup()
            binding.executePendingBindings()

            val spaceForIcon = "  "
            val buttonLabel = SpannableString(spaceForIcon + binding.root.context.getString(R.string.view_order))
            val icon = binding.root.context.getDrawable(R.drawable.eye)
            icon?.let {
                it.setBounds(0, 0, binding.tvViewOrder.lineHeight, binding.tvViewOrder.lineHeight)
                val imageSpan = ImageSpan(it)
                buttonLabel.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.tvViewOrder.text = buttonLabel
            binding.tvViewOrder.setOnClickListener {
                listener.onClickViewOrder(orderId = viewModel.order.orderId)
            }
        }

    }

    interface OrderHistoryListener {
        fun onClickViewOrder(orderId: String)
    }
}