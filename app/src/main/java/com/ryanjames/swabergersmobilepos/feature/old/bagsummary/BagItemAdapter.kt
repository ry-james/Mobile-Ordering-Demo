package com.ryanjames.swabergersmobilepos.feature.old.bagsummary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.RowBagItemBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.BagSummary

class BagItemAdapter(
    lineItems: List<BagLineItem>,
    private val itemsForRemoval: List<BagLineItem>,
    private val listener: BagItemAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewModels = lineItems.map { BagItemViewModel(it) }
    private var isRemoving = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowBagItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BagItemViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BagItemViewHolder) {
            val viewModel = viewModels[holder.adapterPosition]
            val checked = itemsForRemoval.any { viewModel.lineItem.lineItemId == it.lineItemId }
            holder.bind(viewModel, isLastItem = viewModels.size - 1 == holder.adapterPosition, isRemoving = isRemoving, checked = checked)
        }
    }

    fun setRemovingMode(enabled: Boolean) {
        isRemoving = enabled
        notifyDataSetChanged()
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

        fun bind(viewModel: BagItemViewModel, isLastItem: Boolean, isRemoving: Boolean, checked: Boolean) {
            binding.viewModel = viewModel
            viewModel.setup(isLastItem, isRemoving, checked)
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                listener.onClickLineItem(viewModel.lineItem)
            }

            binding.cbRemove.setOnClickListener {
                listener.onRemoveCbCheckedChanged(bagLineItem = viewModel.lineItem, checked = binding.cbRemove.isChecked)
            }
        }

    }

    interface BagItemAdapterListener {
        fun onClickLineItem(lineItem: BagLineItem)
        fun onRemoveCbCheckedChanged(bagLineItem: BagLineItem, checked: Boolean)
    }
}