package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuItemDetailBinding
import com.ryanjames.swabergersmobilepos.domain.*
import javax.inject.Inject

private const val EXTRA_PRODUCT = "extra.product"
private const val ID_MEAL_OPTIONS = "id.meal.options"
private const val ID_PRODUCT_GROUP = "id.product.group"
private const val ID_PRODUCT_GROUP_MODIFIER = "id.product.group.modifier"
const val REQUEST_LINE_ITEM = 0
private const val EXTRA_LINE_ITEM = "extra.line.item"

class MenuItemDetailActivity : BaseActivity(), BottomPickerFragment.BottomPickerListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMenuItemDetailBinding
    private lateinit var viewModel: MenuItemDetailViewModel
    private lateinit var product: Product
    private lateinit var adapter: MenuItemDetailAdapter
    private var lineItem: LineItem? = null
    private var selectedProductGroup: ProductGroup? = null
    private var selectedProductGroupModifierGroup: Pair<Product, ModifierGroup>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MenuItemDetailViewModel::class.java)

        lineItem = (intent.getParcelableExtra(EXTRA_LINE_ITEM) as? LineItem)?.also { lineItem ->
            product = lineItem.product
            viewModel.setupWithLineItem(lineItem)
        }

        if (lineItem == null) {
            product = intent.getParcelableExtra(EXTRA_PRODUCT) as Product
            viewModel.setupWithProduct(product)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_item_detail)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.ivBack.setOnClickListener {
            onUpPressed()
        }

        setupRecyclerView()
        addSubscriptions()

    }

    private fun showCancelChangesDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.cancel_changes_dialog_message))
            .setPositiveButton(getString(R.string.cta_yes)) { dialogInterface, _ ->
                super.onUpPressed()
                dialogInterface.dismiss()
            }
            .setNegativeButton(getString(R.string.cta_no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


    override fun onBackPressed() {
        if (viewModel.shouldShowDiscardChanges()) {
            showCancelChangesDialog()
            return
        }
        super.onBackPressed()
    }

    override fun onUpPressed() {
        if (viewModel.shouldShowDiscardChanges()) {
            showCancelChangesDialog()
            return
        }
        super.onUpPressed()
    }

    private fun addSubscriptions() {
        viewModel.lineItemObservable.observe(this, Observer { lineItem ->
            adapter.lineItem = lineItem
        })
    }

    private fun setupRecyclerView() {

        adapter = MenuItemDetailAdapter(product, object : MenuItemDetailAdapter.OnClickRowListener {

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
                viewModel.setQuantity(quantity)
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

        val selectedItemId = arrayListOf(viewModel.lineItemObservable.value?.bundle?.bundleId ?: product.productId)
        val bottomFragment = BottomPickerFragment.createInstance(ID_MEAL_OPTIONS, getString(R.string.select_meal_option), null, 1, 1, options, selectedItemId)
        bottomFragment.show(supportFragmentManager, "Meal Selection")
    }

    private fun showBottomFragmentForProductGroup(productGroup: ProductGroup) {
        val options = arrayListOf<BottomPickerAdapter.BottomPickerItem>()
        for (option in productGroup.options) {
            val item = BottomPickerAdapter.BottomPickerItem(option.productId, option.productName)
            options.add(item)
        }

        val selectedId = viewModel.lineItemObservable.value?.productsInBundle?.get(productGroup)?.map { it.productId } ?: listOf(productGroup.defaultProduct.productId)
        val subtitle = if (productGroup.min >= 1) getString(R.string.subtitle_required, productGroup.max) else getString(R.string.subtitle_optional, productGroup.max)

        val bottomFragment = BottomPickerFragment.createInstance(
            ID_PRODUCT_GROUP,
            getString(R.string.select_something, productGroup.productGroupName.toUpperCase()),
            subtitle,
            productGroup.min,
            productGroup.max,
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

        val modifierId = viewModel.lineItemObservable.value?.modifiers?.get(ProductModifierGroupKey(product, modifierGroup))?.map { it.modifierId }

        val selectedId = when {
            modifierId != null -> modifierId
            modifierGroup.defaultSelection?.modifierId != null -> listOf(modifierGroup.defaultSelection.modifierId)
            else -> listOf()
        }

        val bottomFragment = BottomPickerFragment.createInstance(
            ID_PRODUCT_GROUP_MODIFIER,
            getString(R.string.select_something, modifierGroup.modifierGroupName.toUpperCase()),
            product.productName,
            modifierGroup.min,
            modifierGroup.max,
            options,
            ArrayList(selectedId)
        )
        bottomFragment.show(supportFragmentManager, "Product Group Modifier Group Selection")
    }


    override fun onUpdatePickerSelections(requestId: String, selectedItemIds: List<String>) {
        when (requestId) {
            ID_MEAL_OPTIONS -> if (selectedItemIds.isNotEmpty()) handleMealSelection(selectedItemIds[0])
            ID_PRODUCT_GROUP -> handleProductGroupSelections(selectedItemIds)
            ID_PRODUCT_GROUP_MODIFIER -> handleProductGroupModifierSelections(selectedItemIds)
        }
    }

    private fun handleMealSelection(selectedId: String) {
        if (product.productId == selectedId) {
            viewModel.setProductBundle(null)
            return
        }
        product.bundles.find { it.bundleId == selectedId }.let { viewModel.setProductBundle(it) }
    }

    private fun handleProductGroupSelections(productGroupIds: List<String>) {
        selectedProductGroup?.let { viewModel.setProductSelectionsForProductGroupByIds(it, productGroupIds) }
    }

    private fun handleProductGroupModifierSelections(selectedIds: List<String>) {
        selectedProductGroupModifierGroup?.let { viewModel.setProductModifiersByIds(it.first, it.second, selectedIds) }
    }

    fun onClickAddToBag(view: View) {
        val intent = Intent().apply {
            putExtra(EXTRA_LINE_ITEM, viewModel.lineItemObservable.value)
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

        fun createIntent(context: Context?, lineItem: LineItem): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_LINE_ITEM, lineItem)
            }
        }

        fun getExtraLineItem(intent: Intent): LineItem {
            return intent.getParcelableExtra(EXTRA_LINE_ITEM)
        }
    }
}
