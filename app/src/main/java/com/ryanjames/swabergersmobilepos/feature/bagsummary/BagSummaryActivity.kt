package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityBagSummaryBinding
import com.ryanjames.swabergersmobilepos.domain.BagLineItem
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.feature.old.bagsummary.BagItemAdapter
import com.ryanjames.swabergersmobilepos.feature.old.bagsummary.BagSummaryViewModel
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent.MenuItemBottomSheetFragment
import com.ryanjames.swabergersmobilepos.fragments.DeliveryAddressBottomSheetFragment
import com.ryanjames.swabergersmobilepos.helper.TAG
import com.ryanjames.swabergersmobilepos.helper.display
import javax.inject.Inject

const val EXTRA_LOCAL_BAG = "extra.local.bag"

class BagSummaryActivity : BaseActivity(), OnMapReadyCallback, MenuItemBottomSheetFragment.Listener {

    private lateinit var binding: ActivityBagSummaryBinding
    private lateinit var adapter: BagItemAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BagSummaryViewModel by viewModels { viewModelFactory }
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        MobilePosDemoApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bag_summary)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.toolbar.isSelected = binding.scrollView.canScrollVertically(-1)
        }
        setupRecyclerView()
        setupMap()
        subscribe()
        viewModel.retrieveLocalBag()
    }

    private fun setupRecyclerView() {
        adapter = BagItemAdapter(listOf(), viewModel.itemsForRemovalList, object : BagItemAdapter.BagItemAdapterListener {
            override fun onClickLineItem(lineItem: BagLineItem) {
                viewModel.getSelectedVenue()?.let { venue ->
                    supportFragmentManager.display(TAG, MenuItemBottomSheetFragment.createInstance(lineItem, venue))
                }
            }

            override fun onRemoveCbCheckedChanged(bagLineItem: BagLineItem, checked: Boolean) {
                if (checked) {
                    viewModel.addItemForRemoval(bagLineItem)
                } else {
                    viewModel.removeItemForRemoval(bagLineItem)
                }
            }
        })
        binding.rvItems.also {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = this.adapter
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap

        this.googleMap?.uiSettings?.apply {
            isScrollGesturesEnabled = false
            isZoomGesturesEnabled = false
            isZoomControlsEnabled = false
        }
        viewModel.getSelectedVenue()?.let {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it.latLng, 13f)
            googleMap?.animateCamera(cameraUpdate)
            googleMap?.addMarker(
                MarkerOptions().position(it.latLng).title(it.name)
            )
        }
    }

    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(binding.container, message, duration).also {
            it.show()
        }
    }

    override fun onAddLineItem(bagSummary: BagSummary) {

    }

    fun onClickUpBtn(view: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        val returnedValue = Intent().apply {
            putExtra(EXTRA_LOCAL_BAG, viewModel.localBagOrNull())
        }
        setResult(Activity.RESULT_OK, returnedValue)
        finish()
    }

    override fun onUpdateLineItem(bagSummary: BagSummary) {
        showSnackbar(getString(R.string.item_updated_snackbar))
        viewModel.setLocalBag(bagSummary)
    }

    fun onClickChangeAddress(view: View) {
        supportFragmentManager.display(TAG, DeliveryAddressBottomSheetFragment.createInstance())
    }

    private fun subscribe() {

        viewModel.getLocalBag.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.event.handleEvent { bagSummary ->
                        adapter.updateBag(bagSummary)
                    }
                }
            }

        })

        viewModel.removeModeToggle.observe(this, Observer { isRemoving ->
            adapter.setRemovingMode(isRemoving)
        })

        viewModel.onRemovingItems.observe(this, Observer { resource ->

            when (resource) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog(getString(R.string.removing_loading))
                }
                is Resource.Success -> {
                    resource.event.handleEvent { bagSummary ->
                        dialogManager.hideLoadingDialog()

                        if (bagSummary.lineItems.isEmpty()) {
                            // Dismiss activity when all items are removed
                            dialogManager.showDismissableDialog(getString(R.string.removing_item_success), onDismiss = this::onBackPressed)
                        } else {
                            dialogManager.showDismissableDialog(getString(R.string.removing_item_success))
                        }
                    }
                }
                is Resource.Error -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.removing_item_fail))
                    }
                }
            }
        })

        viewModel.checkoutObservable.observe(this, Observer { resource ->
            when (resource) {
                is Resource.InProgress -> {
                    dialogManager.showLoadingDialog(getString(R.string.placing_order))
                }
                is Resource.Success -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        val intent = BottomNavActivity.createIntentToOrderSummary(this, it.orderId)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                    }
                }
                is Resource.Error -> {
                    resource.event.handleEvent {
                        dialogManager.hideLoadingDialog()
                        dialogManager.showDismissableDialog(getString(R.string.checkout_failure))
                    }
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        googleMap = null
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, BagSummaryActivity::class.java)
        }

    }
}