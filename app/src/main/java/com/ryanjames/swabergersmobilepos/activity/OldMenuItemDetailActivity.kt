package com.ryanjames.swabergersmobilepos.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.base.BaseActivity
import com.ryanjames.swabergersmobilepos.customview.OptionViewGroup
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuItemDetailBinding
import com.ryanjames.swabergersmobilepos.domain.ModifierGroup
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.domain.ProductGroup
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailViewModel
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString

private const val EXTRA_PRODUCT = "extra.product"
private val ID_MEAL_OPTION = "id.meal.option"

class MenuItemDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityMenuItemDetailBinding
    private lateinit var viewModel: MenuItemDetailViewModel
    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = intent.getParcelableExtra(EXTRA_PRODUCT) as Product

        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_item_detail)
        viewModel = ViewModelProviders.of(this, viewModelFactory { MenuItemDetailViewModel(product) }).get(MenuItemDetailViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvMenuItem.apply {
            layoutManager = LinearLayoutManager(this@MenuItemDetailActivity)
            adapter = MenuItemEditorAdapter(product)
        }

    }

    class MenuItemEditorAdapter(val product: Product) : RecyclerView.Adapter<MenuItemEditorAdapter.SelectionViewHolder>() {

        private val TYPE_MODIFIER_INFO = 0
        private val TYPE_PRODUCT_SELECTION = 1
        private val TYPE_PRODUCT_GROUP = 2


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_selector_view, parent, false)

            if (viewType == TYPE_PRODUCT_SELECTION) {
                return ProductBundleSelectionViewHolder(view)
            } else if (viewType == TYPE_MODIFIER_INFO) {
                return ModifierGroupViewHolder(view)
            } else {
                return ProductGroupSelectionViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            var count = product.modifierGroups.count()
            if (product.bundles.isNotEmpty()) {
                count++
            }
            count += product.bundles.flatMap { it.productGroups }.count()
            return count
        }

        private fun getProductGroupStartingPosition(rvPosition: Int): Int {
            var position = rvPosition - product.modifierGroups.count()
            if (product.bundles.isNotEmpty()) {
                position--
            }
            return position
        }

        override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
            if (holder is ModifierGroupViewHolder) {
                holder.bind(product.modifierGroups[position])
            } else if (holder is ProductBundleSelectionViewHolder) {
                holder.bind(product)
            } else if (holder is ProductGroupSelectionViewHolder) {
                holder.bind(product.bundles.flatMap { it.productGroups }[getProductGroupStartingPosition(position)])
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position < product.modifierGroups.size) {
                return TYPE_MODIFIER_INFO
            } else if (position == product.modifierGroups.size && product.bundles.isNotEmpty()) {
                return TYPE_PRODUCT_SELECTION
            }
            return TYPE_PRODUCT_GROUP
        }

        private class ModifierGroupViewHolder(view: View) : SelectionViewHolder(view) {

            fun bind(modifierGroup: ModifierGroup) {
                optionViewGroup.setId(modifierGroup.modifierGroupId)
                optionViewGroup.setSectionHeader("SELECT ${modifierGroup.modifierGroupName.toUpperCase()}")
                optionViewGroup.setOptions(modifierGroup.options.map { modifierInfo ->
                    OptionViewGroup.OptionViewItem(
                        modifierInfo.modifierId,
                        modifierInfo.modifierName,
                        if (modifierInfo.priceDelta > 0) "+${modifierInfo.priceDelta.toTwoDigitString()}" else ""
                    )
                })
                optionViewGroup.select(modifierGroup.defaultSelection.modifierId)
            }

        }

        private class ProductBundleSelectionViewHolder(view: View) : SelectionViewHolder(view) {

            fun bind(product: Product) {
                optionViewGroup.setSectionHeader("MEAL OPTIONS")
                optionViewGroup.setId(ID_MEAL_OPTION)
                val alaCarte = OptionViewGroup.OptionViewItem(product.productId, "Ala Carte")
                val options = mutableListOf(alaCarte)
                product.bundles.map { bundle ->
                    options.add(OptionViewGroup.OptionViewItem(bundle.bundleId, bundle.bundleName))
                }
                optionViewGroup.setOptions(options)
                optionViewGroup.select(product.productId)
            }

        }

        private class ProductGroupSelectionViewHolder(view: View) : SelectionViewHolder(view) {

            fun bind(productGroup: ProductGroup) {
                optionViewGroup.setSectionHeader("SELECT ${productGroup.productGroupName.toUpperCase()}")
                optionViewGroup.setId(productGroup.productGroupId)

                val options = productGroup.options.map { product ->
                    val suboptions = product.modifierGroups.getOrNull(0)?.options?.map {
                        OptionViewGroup.OptionViewItem(
                            it.modifierId,
                            it.modifierName,
                            if (it.priceDelta > 0) "+${it.priceDelta.toTwoDigitString()}" else ""
                        )
                    }.orEmpty()
                    OptionViewGroup.OptionViewItem(product.productId, product.productName, subOptions = suboptions)
                }

                optionViewGroup.setOptions(options)
                optionViewGroup.select(productGroup.defaultProduct.productId)
            }

        }

        abstract class SelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            protected val optionViewGroup = view.findViewById<OptionViewGroup>(R.id.viewItemSelector)

        }
    }

    companion object {
        fun createIntent(context: Context?, product: Product): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT, product)
            }
        }
    }
}
