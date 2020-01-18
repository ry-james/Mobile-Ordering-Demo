package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.base.BaseActivity
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuItemDetail2Binding
import com.ryanjames.swabergersmobilepos.domain.*

private const val EXTRA_PRODUCT = "extra.product"
private const val ID_MEAL_OPTIONS = "id.meal.options"
private const val ID_PRODUCT_GROUP = "id.product.group"
private const val ID_PRODUCT_GROUP_MODIFIER = "id.product.group.modifier"
const val REQUEST_LINE_ITEM = 0
private const val EXTRA_LINE_ITEM = "extra.line.item"

class MenuItemDetailActivity : BaseActivity(), BottomPickerFragment.BottomPickerListener {

    private lateinit var binding: ActivityMenuItemDetail2Binding
    private lateinit var viewModel: MenuItemDetailViewModel
    private lateinit var product: Product
    private lateinit var adapter: MenuItemDetailAdapter
    private var selectedProductGroup: ProductGroup? = null
    private var selectedProductGroupModifierGroup: Pair<Product, ModifierGroup>? = null

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

        viewModel.onSelectProduct.observe(this, Observer { productSelections ->
            adapter.setProductSelection(productSelections)
        })

        viewModel.onSelectProductGroupModifier.observe(this, Observer { selections ->
            adapter.setProductGroupModifierSelection(selections)
        })
    }

    private fun setupRecyclerView() {

        adapter = MenuItemDetailAdapter(product, viewModel.quantity, object : MenuItemDetailAdapter.OnClickRowListener {

            override fun onClickRowMealOptions() {
                showBottomFragmentForMealSelection()
            }

            override fun onClickRowProductGroup(productGroup: ProductGroup) {
                selectedProductGroup = productGroup
                showBottomFragmentForProductGroup(productGroup)
            }

            override fun onClickRowProductGroupModifier(product: Product, modifierGroup: ModifierGroup) {
                selectedProductGroupModifierGroup = Pair(product, modifierGroup)
                showBottomFragmentForProductGroupModifierGroup(product, modifierGroup)
            }

            override fun onChangeQuantity(quantity: Int) {
                viewModel.quantity = quantity
                adapter.setQuantity(viewModel.quantity)
            }
        })

        binding.rvMenuItem.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager = LinearLayoutManager(this@MenuItemDetailActivity)
            adapter = this@MenuItemDetailActivity.adapter
        }

    }

    private fun showBottomFragmentForMealSelection() {

        val options = arrayListOf(BottomPickerAdapter.BottomPickerItem(product.productId, getString(R.string.ala_carte), getString(R.string.php_price, product.price)))
        options.addAll(product.bundles.map { bundle ->
            BottomPickerAdapter.BottomPickerItem(bundle.bundleId, bundle.bundleName, getString(R.string.php_price, bundle.price))
        })

        val selectedItemId = arrayListOf(viewModel.onSelectBundleObservable.value?.bundleId ?: product.productId)
        val bottomFragment = BottomPickerFragment.createInstance(ID_MEAL_OPTIONS, getString(R.string.select_meal_option), null, 1, 1, options, selectedItemId)
        bottomFragment.show(supportFragmentManager, "Meal Selection")
    }

    private fun showBottomFragmentForProductGroup(productGroup: ProductGroup) {
        val options = arrayListOf<BottomPickerAdapter.BottomPickerItem>()
        for (option in productGroup.options) {
            val item = BottomPickerAdapter.BottomPickerItem(option.productId, option.productName)
            options.add(item)
        }

        val selectedId = viewModel.onSelectProduct.value?.get(productGroup)?.map { it?.productId } ?: listOf(productGroup.defaultProduct.productId)
        val subtitle = "Required - 1, Max - 2"

        val bottomFragment = BottomPickerFragment.createInstance(
            ID_PRODUCT_GROUP,
            getString(R.string.select_something, productGroup.productGroupName.toUpperCase()),
            subtitle,
            1,
            2,
            options,
            ArrayList(selectedId)
        )
        bottomFragment.show(supportFragmentManager, "Product Group Selection")
    }

    private fun showBottomFragmentForProductGroupModifierGroup(product: Product, modifierGroup: ModifierGroup) {
        val options = arrayListOf<BottomPickerAdapter.BottomPickerItem>()
        for (option in modifierGroup.options) {
            val priceDelta = if (option.priceDelta != 0f) getString(R.string.price_delta, option.priceDelta) else null
            val item = BottomPickerAdapter.BottomPickerItem(option.modifierId, option.modifierName, priceDelta)
            options.add(item)
        }

        val selectedId = viewModel.onSelectProductGroupModifier.value?.get(Pair(product, modifierGroup))?.map { it?.modifierId } ?: listOf(modifierGroup.defaultSelection.modifierId)

        val bottomFragment = BottomPickerFragment.createInstance(
            ID_PRODUCT_GROUP_MODIFIER,
            getString(R.string.select_something, "${modifierGroup.modifierGroupName.toUpperCase()}"),
            product.productName,
            1,
            1,
            options,
            ArrayList(selectedId)
        )
        bottomFragment.show(supportFragmentManager, "Product Group Modifier Group Selection")
    }


    override fun onUpdatePickerSelections(requestId: String, selectedItemIds: List<String>) {
        when (requestId) {
            ID_MEAL_OPTIONS -> handleMealSelection(selectedItemIds[0])
            ID_PRODUCT_GROUP -> handleProductGroupSelections(selectedItemIds)
            ID_PRODUCT_GROUP_MODIFIER -> handleProductGroupModifierSelections(selectedItemIds)
        }
    }

    private fun handleMealSelection(selectedId: String) {
        if (product.productId == selectedId) {
            viewModel.setProductBundle(null)
        }
        product.bundles.find { it.bundleId == selectedId }?.let { viewModel.setProductBundle(it) }
    }

    private fun handleProductGroupSelections(productGroupIds: List<String>) {
        selectedProductGroup?.let { viewModel.setProductSelection(it, productGroupIds) }
    }

    private fun handleProductGroupModifierSelections(selectedIds: List<String>) {
        selectedProductGroupModifierGroup?.let { viewModel.setProductGroupModifiers(it.first, it.second, selectedIds) }
    }

    fun onClickAddToBag(view: View) {
        val intent = Intent().apply {
            putExtra(EXTRA_LINE_ITEM, viewModel.createLineItem())
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun createIntent(context: Context?, product: Product): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT, product)
            }
        }

        fun getExtraLineItem(intent: Intent): LineItem {
            return intent.getParcelableExtra(EXTRA_LINE_ITEM)
        }
    }
}
