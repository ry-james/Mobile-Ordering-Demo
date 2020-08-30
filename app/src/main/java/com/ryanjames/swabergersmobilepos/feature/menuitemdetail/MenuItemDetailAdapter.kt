package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.databinding.RowItemProductGroupHeaderBinding
import com.ryanjames.swabergersmobilepos.databinding.RowItemSelectBinding
import com.ryanjames.swabergersmobilepos.domain.*

private const val ID_MEAL_OPTION = 1
private const val ID_PRODUCT_GROUP = 2
private const val ID_PRODUCT_GROUP_MODIFIER = 3
private const val ID_QUANTITY = 4
private const val ID_PRODUCT_GROUP_HEADER = 5

class MenuItemDetailAdapter(
    private val onClickRowListener: OnClickRowListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<RowDataHolder> = listOf()

    private var lineItem: LineItem? = null
        set(value) {
            field = value
        }

    fun update(lineItem: LineItem) {
        this.lineItem = lineItem.deepCopy()
        notifyChange()
    }

    private fun notifyChange() {
        data = createRowDataHolders()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            ID_MEAL_OPTION -> RowSelectMealViewHolder(binding, onClickRowListener)
            ID_PRODUCT_GROUP -> RowSelectProductGroupViewHolder(binding, onClickRowListener)
            ID_QUANTITY -> RowSelectQuantityViewHolder(binding, onClickRowListener)
            ID_PRODUCT_GROUP_HEADER -> {
                val headerBinding = RowItemProductGroupHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RowProductGroupHeaderViewHolder(headerBinding)
            }
            else -> RowSelectProductGroupModifierViewHolder(binding, onClickRowListener)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RowSelectMealViewHolder -> {
                val dataHolder = data[position] as RowDataHolder.RowSelectMealDataHolder
                holder.bind(
                    lineItem?.bundle,
                    dataHolder.hideLineSeparator
                )
            }
            is RowSelectProductGroupViewHolder -> {
                val dataHolder = data[position] as RowDataHolder.RowProductGroupDataHolder
                val productGroup = dataHolder.productGroup
                holder.bind(
                    productGroup,
                    lineItem?.productsInBundle?.get(productGroup) ?: listOf(),
                    dataHolder.hideLineSeparator
                )
            }
            is RowSelectProductGroupModifierViewHolder -> {
                val dataHolder = data[position] as RowDataHolder.RowProductGroupModifierDataHolder
                val product = dataHolder.product
                val modifierGroup = dataHolder.modifierGroup
                holder.bind(
                    product,
                    modifierGroup, lineItem?.modifiers?.get(ProductModifierGroupKey(product, modifierGroup)) ?: listOf(),
                    dataHolder.hideLineSeparator
                )
            }
            is RowSelectQuantityViewHolder -> {
                holder.bind(lineItem?.quantity ?: 1)
            }
            is RowProductGroupHeaderViewHolder -> {
                val dataHolder = data[position] as RowDataHolder.RowProductGroupHeaderDataHolder
                val productGroup = dataHolder.productGroup
                holder.bind(productGroup.productGroupName)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = data[position].itemViewType


    class RowSelectMealViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedBundle: ProductBundle?, hideLine: Boolean) {
            binding.tvHeader.setText(R.string.meal_options)
            if (selectedBundle == null) {
                binding.tvSubheader.setText(R.string.ala_carte)
            } else {
                binding.tvSubheader.text = selectedBundle.bundleName
            }

            binding.view.visibility = if (hideLine) View.GONE else View.VISIBLE
            binding.root.setOnClickListener {
                onClickRowListener.onClickRowMealOptions()
            }
        }

    }

    class RowSelectProductGroupViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(productGroup: ProductGroup, productList: List<Product>, hideLine: Boolean) {
            binding.tvHeader.text = "SELECT"
            if (productList.isEmpty()) {
                binding.tvSubheader.setText(R.string.none_selected)
            } else {
                binding.tvSubheader.text = productList.toText()
            }

            binding.view.visibility = if (hideLine) View.GONE else View.VISIBLE
            binding.root.setOnClickListener {
                onClickRowListener.onClickRowProductGroup(productGroup)
            }
        }

        private fun List<Product>.toText(): String {
            var text = ""
            this.forEachIndexed { index, product ->
                text += product.productName
                if (index != this.size - 1) text += ", "
            }
            return text
        }
    }

    class RowSelectProductGroupModifierViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, modifierGroup: ModifierGroup, modifierList: List<ModifierInfo>, hideLine: Boolean) {
            binding.tvHeader.text = "SELECT ${modifierGroup.modifierGroupName.toUpperCase()}"
            if (modifierList.isEmpty()) {
                binding.tvSubheader.setText(R.string.none_selected)
            } else {
                binding.tvSubheader.text = "${product.productName} - ${modifierList.toText()}"
            }

            binding.view.visibility = if (hideLine) View.GONE else View.VISIBLE
            binding.root.setOnClickListener {
                onClickRowListener.onClickRowProductGroupModifier(product, modifierGroup)
            }
        }

        private fun List<ModifierInfo>.toText(): String {
            var text = ""
            this.forEachIndexed { index, modifierInfo ->
                text += modifierInfo.modifierName
                if (index != this.size - 1) text += ", "
            }
            return text
        }

    }

    class RowSelectQuantityViewHolder(
        private val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quantity: Int) {
            binding.tvHeader.text = binding.root.context.getString(R.string.quantity)
            binding.ivMinus.visibility = View.VISIBLE
            binding.ivPlus.visibility = View.VISIBLE
            binding.tvQuantityValue.visibility = View.VISIBLE
            binding.tvSubheader.visibility = View.GONE
            binding.tvQuantityValue.text = quantity.toString()

            binding.ivMinus.setOnClickListener {
                onClickRowListener.onChangeQuantity(quantity - 1)
            }

            binding.ivPlus.setOnClickListener {
                onClickRowListener.onChangeQuantity(quantity + 1)
            }
        }

    }

    class RowProductGroupHeaderViewHolder(
        private val binding: RowItemProductGroupHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(headerText: String) {
            binding.tvHeader.text = headerText
        }

    }

    private fun createRowDataHolders(): List<RowDataHolder> {
        val list = mutableListOf<RowDataHolder>()
        list.add(RowDataHolder.RowQuantity())

        lineItem?.product?.let { product ->

            if (lineItem?.product?.bundles?.isNotEmpty() == true) {
                list.add(RowDataHolder.RowSelectMealDataHolder())
            }

            for (modifierGroup in product.modifierGroups) {
                list.add(RowDataHolder.RowProductGroupModifierDataHolder(product, modifierGroup))
            }

            lineItem?.bundle?.productGroups?.forEach { productGroup ->
                // Hide line separator for the row before the product group header
                val lastItemIndex = list.size - 1
                if (lastItemIndex >= 0) {
                    list[lastItemIndex].hideLineSeparator = true
                }

                list.add(RowDataHolder.RowProductGroupHeaderDataHolder(productGroup))
                list.add(RowDataHolder.RowProductGroupDataHolder(productGroup))

                val productSelection = lineItem?.productsInBundle?.get(productGroup)
                productSelection?.forEach {
                    it.modifierGroups.forEach { modifierGroup ->
                        list.add(RowDataHolder.RowProductGroupModifierDataHolder(it, modifierGroup))
                    }
                }
            }
        }

        return list
    }

    private sealed class RowDataHolder {

        abstract val itemViewType: Int
        var hideLineSeparator = false

        class RowSelectMealDataHolder : RowDataHolder() {
            override val itemViewType: Int = ID_MEAL_OPTION
        }

        class RowProductGroupDataHolder(val productGroup: ProductGroup) : RowDataHolder() {
            override val itemViewType: Int = ID_PRODUCT_GROUP
        }

        class RowProductGroupModifierDataHolder(val product: Product, val modifierGroup: ModifierGroup) : RowDataHolder() {
            override val itemViewType: Int = ID_PRODUCT_GROUP_MODIFIER
        }

        class RowQuantity : RowDataHolder() {
            override val itemViewType: Int = ID_QUANTITY
        }

        class RowProductGroupHeaderDataHolder(val productGroup: ProductGroup) : RowDataHolder() {
            override val itemViewType: Int = ID_PRODUCT_GROUP_HEADER
        }
    }


    interface OnClickRowListener {
        fun onClickRowMealOptions()

        fun onClickRowProductGroup(productGroup: ProductGroup)

        fun onClickRowProductGroupModifier(product: Product, modifierGroup: ModifierGroup)

        fun onChangeQuantity(quantity: Int)
    }


}