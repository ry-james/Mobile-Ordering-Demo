package com.ryanjames.swabergersmobilepos.feature.menuitemdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityMenuItemDetailBinding
import com.ryanjames.swabergersmobilepos.domain.*
import javax.inject.Inject

private const val EXTRA_PRODUCT = "extra.product"
private const val ID_MEAL_OPTIONS = "id.meal.options"
private const val ID_PRODUCT_GROUP = "id.product.group"
private const val ID_PRODUCT_GROUP_MODIFIER = "id.product.group.modifier"
const val REQUEST_LINEITEM = 0
private const val EXTRA_LINE_ITEM = "extra.line.item"
private const val EXTRA_BAG_SUMMARY = "extra.bag.summary"
private const val EXTRA_BAG_LINE_ITEM = "extra.bag.line.item"
const val RESULT_ADD_OR_UPDATE_ITEM = 1000
const val RESULT_REMOVE_ITEM = 1001

class MenuItemDetailActivity : BaseActivity(), BottomPickerFragment.BottomPickerListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMenuItemDetailBinding
    private lateinit var adapter: MenuItemDetailAdapter
    private var lineItem: LineItem? = null
    private var selectedProductGroup: ProductGroup? = null
    private var selectedProductGroupModifierGroup: Pair<Product, ModifierGroup>? = null

    private val viewModel: MenuItemDetailViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right)

        MobilePosDemoApplication.appComponent.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu_item_detail)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        addSubscriptions()

        val bagLineItem = (intent.getParcelableExtra(EXTRA_BAG_LINE_ITEM) as? BagLineItem)?.also { lineItem ->
            viewModel.setupWithBagLineItem(lineItem)
        }

        if (bagLineItem == null) {
            val productId = intent.getStringExtra(EXTRA_PRODUCT)
            viewModel.setupWithProductId(productId)
        }

        binding.ivBack.setOnClickListener {
            onUpPressed()
        }

        setupRecyclerView()
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
        viewModel.lineItemObservable.observe(this, Observer { result ->
            when (result) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog()
                }
                is Resource.Success -> {
                    dialogManager.hideLoadingDialog()
                    lineItem = result.data
                    adapter.update(result.data)
                }
                is Resource.Error -> {
                    dialogManager.hideLoadingDialog()
                    dialogManager.showActivityFinishingDialog("Sorry, we're having trouble loading the product. Try again later.")
                }
            }
        })

        viewModel.onAddItem.observe(this, Observer { result ->
            when (result) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog("Adding to bag...")
                }
                is Resource.Success -> {
                    dialogManager.hideLoadingDialog()
                    val intent = Intent().apply {
                        putExtra(EXTRA_BAG_SUMMARY, result.data)
                    }
                    setResult(RESULT_ADD_OR_UPDATE_ITEM, intent)
                    finish()
                }
                is Resource.Error -> {
                    dialogManager.hideLoadingDialog()
                    showErrorDialog(getString(R.string.error_add_item))
                }
            }
        })

        viewModel.onUpdateItem.observe(this, Observer { result ->
            when (result) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog("Updating item...")
                }
                is Resource.Success -> {
                    dialogManager.hideLoadingDialog()
                    val intent = Intent().apply {
                        putExtra(EXTRA_BAG_SUMMARY, result.data)
                    }
                    setResult(RESULT_ADD_OR_UPDATE_ITEM, intent)
                    finish()
                }
                is Resource.Error -> {
                    dialogManager.hideLoadingDialog()
                    showErrorDialog(getString(R.string.error_modify_item))
                }
            }
        })

        viewModel.onRemoveItem.observe(this, Observer { result ->
            when (result) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog(getString(R.string.removing_from_bag))
                }
                is Resource.Success -> {
                    dialogManager.hideLoadingDialog()
                    val intent = Intent().apply {
                        putExtra(EXTRA_BAG_SUMMARY, result.data)
                    }
                    setResult(RESULT_REMOVE_ITEM, intent)
                    finish()
                }
                is Resource.Error -> {
                    dialogManager.hideLoadingDialog()
                    showErrorDialog(getString(R.string.error_remove_item))
                }
            }

        })
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun setupRecyclerView() {

        adapter = MenuItemDetailAdapter(object : MenuItemDetailAdapter.OnClickRowListener {

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
        viewModel.getLineItem()?.product?.let { product ->
            val options = arrayListOf(BottomPickerAdapter.BottomPickerItem(product.productId, getString(R.string.ala_carte), getString(R.string.usd_price, product.price)))
            options.addAll(product.bundles.map { bundle ->
                BottomPickerAdapter.BottomPickerItem(bundle.bundleId, bundle.bundleName, getString(R.string.usd_price, bundle.price))
            })

            val selectedItemId = arrayListOf(lineItem?.bundle?.bundleId ?: product.productId)
            val bottomFragment = BottomPickerFragment.createInstance(ID_MEAL_OPTIONS, getString(R.string.select_meal_option), null, 1, 1, options, selectedItemId)
            bottomFragment.show(supportFragmentManager, "Meal Selection")
        }
    }

    private fun showBottomFragmentForProductGroup(productGroup: ProductGroup) {
        val options = arrayListOf<BottomPickerAdapter.BottomPickerItem>()
        for (option in productGroup.options) {
            val item = BottomPickerAdapter.BottomPickerItem(option.productId, option.productName)
            options.add(item)
        }

        val selectedId = lineItem?.productsInBundle?.get(productGroup)?.map { it.productId } ?: listOf(productGroup.defaultProduct.productId)
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

        val modifierId = lineItem?.modifiers?.get(ProductModifierGroupKey(product, modifierGroup))?.map { it.modifierId }

        val selectedId = when {
            modifierId != null -> modifierId
            modifierGroup.defaultSelection?.modifierId != null -> listOf(modifierGroup.defaultSelection.modifierId)
            else -> listOf()
        }

        val bottomFragment = BottomPickerFragment.createInstance(
            ID_PRODUCT_GROUP_MODIFIER,
            modifierGroup.modifierGroupName.toUpperCase(),
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
        viewModel.handleMealSelection(selectedId)
    }

    private fun handleProductGroupSelections(productGroupIds: List<String>) {
        selectedProductGroup?.let { viewModel.setProductSelectionsForProductGroupByIds(it, productGroupIds) }
    }

    private fun handleProductGroupModifierSelections(selectedIds: List<String>) {
        selectedProductGroupModifierGroup?.let { viewModel.setProductModifiersByIds(it.first, it.second, selectedIds) }
    }

    fun onClickAddToBag(view: View) {
        viewModel.addOrUpdateItem()
    }

    fun onClickRemove(view: View) {
        viewModel.removeFromBag()
    }

    companion object {
        fun createIntent(context: Context?, productId: String): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_PRODUCT, productId)
            }
        }

        fun createIntent(context: Context?, bagLineItem: BagLineItem): Intent {
            return Intent(context, MenuItemDetailActivity::class.java).apply {
                putExtra(EXTRA_BAG_LINE_ITEM, bagLineItem)
            }
        }

        fun getBagSummaryExtra(intent: Intent): BagSummary {
            return intent.getParcelableExtra(EXTRA_BAG_SUMMARY)
        }
    }
}
