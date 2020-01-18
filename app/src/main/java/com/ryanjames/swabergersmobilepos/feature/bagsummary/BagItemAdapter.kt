package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.RowBagItemBinding
import com.ryanjames.swabergersmobilepos.domain.LineItem

class BagItemAdapter(lineItems: List<LineItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var viewModels = lineItems.map { BagItemViewModel(it) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowBagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BagItemViewHolder(binding)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BagItemViewHolder) {
            holder.bind(viewModels[holder.adapterPosition], viewModels.size - 1 == holder.adapterPosition)
        }
    }

    class BagItemViewHolder(val binding: RowBagItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: BagItemViewModel, isLastItem: Boolean) {
            binding.viewModel = viewModel
            viewModel.setup(isLastItem)
            binding.executePendingBindings()
        }

    }
}