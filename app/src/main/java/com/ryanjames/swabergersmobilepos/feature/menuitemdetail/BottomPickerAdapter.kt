package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.RowBottomSheetItemBinding
import kotlinx.android.parcel.Parcelize

class BottomPickerAdapter(
    bottomPickerItems: List<BottomPickerItem>,
    clickListener: BottomSheetClickListener,
    singleSelection: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModels = bottomPickerItems.map {
        BottomPickerItemViewModel(it, singleSelection) { selectedItem ->
            clickListener.onSelectPickerRow(selectedItem.id)
        }
    }

    fun setSelectedRows(id: List<String>) {
        for (viewModel in viewModels) {
            if (id.contains(viewModel.item.id)) {
                viewModel.check()
            } else {
                viewModel.uncheck()
            }
        }
        notifyDataSetChanged()
    }


    fun disableSelections() {
        viewModels.forEach { if (it.checked.value == false) it.disable() }
    }

    fun enableSelections() {
        viewModels.forEach { it.enable() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowBottomSheetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottomPickerItemViewHolder(binding)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BottomPickerItemViewHolder) {
            holder.bind(viewModels[position])
        }
    }

    class BottomPickerItemViewHolder(val binding: RowBottomSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: BottomPickerItemViewModel) {
            binding.viewModel = viewModel
            viewModel.setupViewModel()
            binding.executePendingBindings()
        }

    }

    @Parcelize
    data class BottomPickerItem(
        val id: String,
        val name: String,
        val desc: String? = null
    ) : Parcelable

    interface BottomSheetClickListener {
        fun onSelectPickerRow(id: String)
    }
}