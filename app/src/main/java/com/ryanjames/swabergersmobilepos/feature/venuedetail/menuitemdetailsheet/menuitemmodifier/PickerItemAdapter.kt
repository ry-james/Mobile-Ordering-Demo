package com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.core.StringBinder
import com.ryanjames.swabergersmobilepos.databinding.ViewRowItemPickerBinding

class PickerItemAdapter(
    val clickListener: PickerItemClickListener,
    val singleSelection: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewModels: List<PickerItemViewModel> = listOf()

    var items: List<PickerItem> = listOf()
        set(value) {
            field = value
            viewModels = value.map {
                PickerItemViewModel(it, singleSelection) { selectedItem ->
                    clickListener.onSelectPickerRow(selectedItem.id)
                }
            }
            notifyDataSetChanged()
        }

    fun setSelectedRows(ids: List<String>) {
        for ((index, viewModel) in viewModels.withIndex()) {
            if (ids.contains(viewModel.item.id)) {
                viewModel.check()
                notifyItemChanged(index)
            } else if (viewModel.checked.value == true) {
                viewModel.uncheck()
                notifyItemChanged(index)
            }
        }
    }

    fun getSelectedRows(): List<String> {
        return viewModels.filter { it.checked.value == true }.map { it.item.id }
    }

    fun disableSelections() {
        viewModels.forEach { if (it.checked.value == false) it.disable() }
        notifyItemRangeChanged(0, viewModels.size)
    }

    fun enableSelections() {
        viewModels.forEach { it.enable() }
        notifyItemRangeChanged(0, viewModels.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ViewRowItemPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottomPickerItemViewHolder(binding)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BottomPickerItemViewHolder) {
            holder.bind(viewModels[position])
        }
    }

    class BottomPickerItemViewHolder(val binding: ViewRowItemPickerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: PickerItemViewModel) {
            binding.viewModel = viewModel
            viewModel.setupViewModel()
            binding.executePendingBindings()
        }

    }

    data class PickerItem(
        val id: String,
        val name: StringBinder,
        val desc: StringBinder? = null
    )

    interface PickerItemClickListener {
        fun onSelectPickerRow(id: String)
    }
}