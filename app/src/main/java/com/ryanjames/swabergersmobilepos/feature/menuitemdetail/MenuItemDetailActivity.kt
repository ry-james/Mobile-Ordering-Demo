package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.base.BaseActivity
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuItemDetail2Binding
import com.ryanjames.swabergersmobilepos.domain.ModifierGroup
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.domain.ProductGroup
import java.util.*

private const val EXTRA_PRODUCT = "extra.product"
private const val ID_MEAL_OPTIONS = "id.meal.options"
private const val ID_MODIFIER_GROUP = "id.modifier.group"
private const val ID_PRODUCT_GROUP = "id.product.group"

class MenuItemDetailActivity : BaseActivity(), BottomSelectorFragment.BottomSelectorListener {

    private lateinit var binding: ActivityMenuItemDetail2Binding
    private lateinit var viewModel: MenuItemDetailViewModel
    private lateinit var product: Product
    private lateinit var adapter: MenuItemDetailAdapter
    private var selectedRowModifierGroup: ModifierGroup? = null
    private var selectedProductGroup: ProductGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = intent.getParcelableExtra(EXTRA_PRODUCT) as Product

        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_item_detail2)
        viewModel = ViewModelProviders.of(this, viewModelFactory { MenuItemDetailViewModel(product) }).get(MenuItemDetailViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupRecyclerView()
        addSubscriptions()
    }

    private fun addSubscriptions() {
        viewModel.onSelectBundleObservable.observe(this, Observer { bundle ->
            adapter.setBundle(bundle)
        })

        viewModel.onSelectProductModifier.observe(this, Observer { modifierSelections ->
            adapter.setModifierSelection(modifierSelections)
        })

        viewModel.onSelectProduct.observe(this, Observer { productSelections ->
            adapter.setProductSelection(productSelections)
        })
    }

    private fun setupRecyclerView() {

        adapter = MenuItemDetailAdapter(product, object : MenuItemDetailAdapter.OnClickRowListener {

            override fun onClickRowMealOptions() {
                showBottomFragmentForMealSelection()
            }

            override fun onClickRowProductModifierGroup(modifierGroup: ModifierGroup) {
                selectedRowModifierGroup = modifierGroup
                showBottomFragmentForModifierGroup(modifierGroup)
            }

            override fun onClickRowProductGroup(productGroup: ProductGroup) {
                selectedProductGroup = productGroup
                showBottomFragmentForProductGroup(productGroup)
            }
        })

        binding.rvMenuItem.apply {
            layoutManager = LinearLayoutManager(this@MenuItemDetailActivity)
            adapter = this@MenuItemDetailActivity.adapter
        }

    }

    private fun showBottomFragmentForModifierGroup(modifierGroup: ModifierGroup) {
        val options = arrayListOf<BottomSelectorAdapter.BottomSelectorItem>()
        for (option in modifierGroup.options) {
            val priceDelta = if (option.priceDelta != 0f) getString(R.string.price_delta, option.priceDelta) else null
            val item = BottomSelectorAdapter.BottomSelectorItem(option.modifierId, option.modifierName, priceDelta)
            options.add(item)
        }

        val selectedId = viewModel.onSelectProductModifier.value?.get(modifierGroup)?.modifierId ?: modifierGroup.defaultSelection.modifierId

        val bottomFragment = BottomSelectorFragment.createInstance(
            ID_MODIFIER_GROUP,
            getString(R.string.select_something, modifierGroup.modifierGroupName.toUpperCase(Locale.ENGLISH)),
            options,
            selectedId
        )
        bottomFragment.show(supportFragmentManager, "Modifier Group Selection")
    }

    private fun showBottomFragmentForMealSelection() {

        val options = arrayListOf(BottomSelectorAdapter.BottomSelectorItem(product.productId, getString(R.string.ala_carte), getString(R.string.php_price, product.price)))
        options.addAll(product.bundles.map { bundle ->
            BottomSelectorAdapter.BottomSelectorItem(
                bundle.bundleId,
                bundle.bundleName,
                getString(R.string.php_price, bundle.price)
            )
        })

        val selectedItemId = viewModel.onSelectBundleObservable.value?.bundleId ?: product.productId
        val bottomFragment = BottomSelectorFragment.createInstance(ID_MEAL_OPTIONS, getString(R.string.select_meal_option), options, selectedItemId)
        bottomFragment.show(supportFragmentManager, "Meal Selection")
    }

    private fun showBottomFragmentForProductGroup(productGroup: ProductGroup) {
        val options = arrayListOf<BottomSelectorAdapter.BottomSelectorItem>()
        for (option in productGroup.options) {
            val item = BottomSelectorAdapter.BottomSelectorItem(option.productId, option.productName)
            options.add(item)
        }

        val selectedId = viewModel.onSelectProduct.value?.get(productGroup)?.productId ?: productGroup.defaultProduct.productId

        val bottomFragment = BottomSelectorFragment.createInstance(
            ID_PRODUCT_GROUP,
            getString(R.string.select_something, productGroup.productGroupName.toUpperCase()),
            options,
            selectedId
        )
        bottomFragment.show(supportFragmentManager, "Product Group Selection")
    }

    override fun onSelectItem(requestId: String, selectedItemId: String) {
        when (requestId) {
            ID_MEAL_OPTIONS -> handleMealSelection(selectedItemId)
            ID_MODIFIER_GROUP -> handleModifierGroupSelection(selectedItemId)
            ID_PRODUCT_GROUP -> handleProductGroupSelection(selectedItemId)
        }
    }

    private fun handleMealSelection(selectedId: String) {
        if (product.productId == selectedId) {
            viewModel.setProductBundle(null)
        }

        for (bundle in product.bundles) {
            if (bundle.bundleId == selectedId) {
                viewModel.setProductBundle(bundle)
            }
        }
    }

    private fun handleModifierGroupSelection(modifierInfoId: String) {
        selectedRowModifierGroup?.let { viewModel.setModifierGroupSelection(it, modifierInfoId) }
    }


    private fun handleProductGroupSelection(productGroupId: String) {
        selectedProductGroup?.let { viewModel.setProductSelection(it, productGroupId) }
    }

    companion object {
        fun createIntent(context: Context?, product: Product): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT, product)
            }
        }
    }
}
