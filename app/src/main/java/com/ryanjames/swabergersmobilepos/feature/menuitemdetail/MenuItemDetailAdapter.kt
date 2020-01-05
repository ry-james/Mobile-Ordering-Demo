package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.databinding.RowItemSelectBinding
import com.ryanjames.swabergersmobilepos.domain.*

private const val ID_MEAL_OPTION = 1
private const val ID_PRODUCT_MODIFIER_GROUP = 2
private const val ID_PRODUCT_GROUP = 3

class MenuItemDetailAdapter(
    val product: Product,
    private val onClickRowListener: OnClickRowListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedBundle: ProductBundle? = null
    private var modifierGroupSelection = HashMap<ModifierGroup, ModifierInfo?>()
    private var productSelection = HashMap<ProductGroup, Product?>()
    private var productGroupModifierSelection = HashMap<Pair<ProductGroup, ModifierGroup>, ModifierInfo?>()

    fun setBundle(productBundle: ProductBundle?) {
        selectedBundle = productBundle
        notifyDataSetChanged()
    }

    fun setModifierSelection(selection: HashMap<ModifierGroup, ModifierInfo?>) {
        modifierGroupSelection = selection
        notifyDataSetChanged()
    }

    fun setProductSelection(selection: HashMap<ProductGroup, Product?>) {
        productSelection = selection
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (viewType == ID_MEAL_OPTION) {
            return RowSelectMealViewHolder(binding, onClickRowListener)
        } else if (viewType == ID_PRODUCT_GROUP) {
            return RowSelectProductGroupViewHolder(binding, onClickRowListener)
        }
        return RowSelectModifierViewHolder(binding, onClickRowListener)
    }

    override fun getItemCount(): Int {
        var count = product.modifierGroups.count()
        if (product.hasBundles()) {
            count++
        }

        count += (selectedBundle?.productGroups?.count()) ?: 0

        return count
    }

    private fun Product.hasBundles(): Boolean = this.bundles.isNotEmpty()

    private fun getModifierGroup(adapterPosition: Int): ModifierGroup {
        var modifierGroupIndex = adapterPosition
        if (product.hasBundles()) {
            modifierGroupIndex--
        }
        return product.modifierGroups[modifierGroupIndex]
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RowSelectMealViewHolder) {
            holder.bind(selectedBundle)
        } else if (holder is RowSelectModifierViewHolder) {
            val modifierGroup = getModifierGroup(position)
            holder.bind(modifierGroup, modifierGroupSelection[modifierGroup])
        } else if (holder is RowSelectProductGroupViewHolder) {
            selectedBundle?.productGroups?.get(getProductGroupPositions().indexOf(position))?.let {
                holder.bind(it, productSelection[it])
            }

        }
    }

    private fun countMealSelection(): Int = if (product.hasBundles()) 1 else 0

    private fun getProductGroupPositions(): List<Int> {
        val productGroupCount = selectedBundle?.productGroups?.count() ?: 0
        val modifierInfoCount = product.modifierGroups.count()
        val list = mutableListOf<Int>()
        for (i in 0 until productGroupCount) {
            list.add(countMealSelection() + modifierInfoCount + i)
        }
        return list
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && product.hasBundles()) return ID_MEAL_OPTION

        if (selectedBundle != null) {
            if (position in getProductGroupPositions()) {
                return ID_PRODUCT_GROUP
            }
        }

        return ID_PRODUCT_MODIFIER_GROUP
    }


    class RowSelectMealViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(selectedBundle: ProductBundle?) {
            binding.tvHeader.setText(R.string.meal_options)
            if (selectedBundle == null) {
                binding.tvSubheader.setText(R.string.ala_carte)
            } else {
                binding.tvSubheader.text = selectedBundle.bundleName
            }

            binding.root.setOnClickListener {
                onClickRowListener.onClickRowMealOptions()
            }
        }

    }

    class RowSelectModifierViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(modifierGroup: ModifierGroup, modifierInfo: ModifierInfo?) {
            binding.tvHeader.text = "SELECT ${modifierGroup.modifierGroupName.toUpperCase()}"
            if (modifierInfo == null) {
                binding.tvSubheader.setText(R.string.none_selected)
            } else {
                binding.tvSubheader.text = modifierInfo.modifierName
            }

            binding.root.setOnClickListener {
                onClickRowListener.onClickRowProductModifierGroup(modifierGroup)
            }
        }

    }

    class RowSelectProductGroupViewHolder(
        val binding: RowItemSelectBinding,
        private val onClickRowListener: OnClickRowListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(productGroup: ProductGroup, product: Product?) {
            binding.tvHeader.text = "SELECT ${productGroup.productGroupName.toUpperCase()}"
            if (product == null) {
                binding.tvSubheader.setText(R.string.none_selected)
            } else {
                binding.tvSubheader.text = product.productName
            }

            binding.root.setOnClickListener {
                onClickRowListener.onClickRowProductGroup(productGroup)
            }
        }

    }


    interface OnClickRowListener {
        fun onClickRowMealOptions()

        fun onClickRowProductModifierGroup(modifierGroup: ModifierGroup)

        fun onClickRowProductGroup(productGroup: ProductGroup)
    }


}