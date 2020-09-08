package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.RowBagItemBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.BagSummary

class BagItemAdapter(
    lineItems: List<BagLineItem>,
    private val listener: BagItemAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewModels = lineItems.map { BagItemViewModel(it) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowBagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BagItemViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BagItemViewHolder) {
            holder.bind(viewModels[holder.adapterPosition], viewModels.size - 1 == holder.adapterPosition)
        }
    }

    fun updateBag(bagSummary: BagSummary) {
        viewModels = bagSummary.lineItems.map { BagItemViewModel(it) }
        notifyDataSetChanged()
    }

    fun clear() {
        viewModels = listOf()
        notifyDataSetChanged()
    }

    class BagItemViewHolder(
        private val binding: RowBagItemBinding,
        private val listener: BagItemAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: BagItemViewModel, isLastItem: Boolean) {
            binding.viewModel = viewModel
            viewModel.setup(isLastItem)
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                listener.onClickLineItem(viewModel.lineItem)
            }

        }

    }

    interface BagItemAdapterListener {
        fun onClickLineItem(lineItem: BagLineItem)
    }
}