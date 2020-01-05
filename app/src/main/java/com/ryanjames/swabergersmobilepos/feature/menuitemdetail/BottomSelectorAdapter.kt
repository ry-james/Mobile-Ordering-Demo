package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.databinding.RowBottomSheetItemBinding
import kotlinx.android.parcel.Parcelize

class BottomSelectorAdapter(
    bottomSelectorItems: List<BottomSelectorItem>,
    clickListener: BottomSheetClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModels = bottomSelectorItems.map {
        BottomSelectorItemViewModel(it) { selectedItem ->
            this.selectedId = selectedItem.id
            clickListener.onSelectRow(selectedItem.id)
        }
    }


    private var selectedId: String? = null
        set(value) {
            field = value
            updateViewSelections()
        }

    fun selectRow(id: String?) {
        selectedId = id
    }


    private fun updateViewSelections() {
        viewModels.forEach {
            if (it.item.id != selectedId) {
                it.uncheck()
            } else {
                it.check()
            }
            notifyItemRangeChanged(0, viewModels.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowBottomSheetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottomSelectorViewHolder(binding)
    }

    override fun getItemCount(): Int = viewModels.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BottomSelectorViewHolder) {
            holder.bind(viewModels[position])
        }
    }

    class BottomSelectorViewHolder(val binding: RowBottomSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: BottomSelectorItemViewModel) {
            binding.viewModel = viewModel
            viewModel.setupViewModel()
            binding.executePendingBindings()
        }

    }

    @Parcelize
    data class BottomSelectorItem(
        val id: String,
        val name: String,
        val desc: String? = null
    ) : Parcelable

    interface BottomSheetClickListener {
        fun onSelectRow(id: String)
    }
}