package com.ryanjames.swabergersmobilepos.feature.venuedetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.updateMargins
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.google.android.material.tabs.TabLayout
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MarginItemDecoration
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityVenueDetailBinding
import com.ryanjames.swabergersmobilepos.databinding.ViewDisplayChipsBinding
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryActivity
import com.ryanjames.swabergersmobilepos.feature.bagsummary.EXTRA_LOCAL_BAG
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent.MenuItemBottomSheetFragment
import com.ryanjames.swabergersmobilepos.helper.TAG
import com.ryanjames.swabergersmobilepos.helper.display
import kotlinx.android.synthetic.main.activity_venue_detail.*
import javax.inject.Inject

private const val EXTRA_VENUE = "extra.venue"
private const val EXTRA_VENUE_ID = "extra.venue.id"

class VenueDetailActivity : BaseActivity(), MenuItemBottomSheetFragment.Listener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: VenueDetailViewModel by viewModels { viewModelFactory }

    lateinit var binding: ActivityVenueDetailBinding

    private val adapter = MenuListAdapter {
        viewModel.venue?.let { venue ->
            supportFragmentManager.display(TAG, MenuItemBottomSheetFragment.createInstance(it.productId, venue))
        }
    }

    private val launchBagSummary = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getParcelableExtra<BagSummary?>(EXTRA_LOCAL_BAG)?.let { bagSummary -> viewModel.updateBag(bagSummary) }
        }
    }

    private val layoutManager = LinearLayoutManager(this@VenueDetailActivity)

    private var isUserScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_venue_detail)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Blur image
        // Glide.with(this).asBitmap().load(R.drawable.placeholder3).transform(BlurTransformation(5, 3)).into(binding.ivImage)

        setupRecyclerView()
        setupTabLayout()
        setupViewCardButton()
        subscribe()

        intent.getParcelableExtra<Venue>(EXTRA_VENUE)?.let { venue ->
            viewModel.getMenu(venue)
        } ?: intent.getStringExtra(EXTRA_VENUE_ID)?.let { venueId ->
            viewModel.getVenueAndMenuById(venueId)
        }

    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (!isUserScrolling) {
                    val tabPosition = tab?.position ?: 0

                    layoutManager.startSmoothScroll(object : LinearSmoothScroller(this@VenueDetailActivity) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }.apply {
                        targetPosition = adapter.getCategoryPosition(tabPosition)
                    })
                }
            }

        })

    }

    private fun setupViewCardButton() {
        binding.viewCart.container.setOnClickListener {
            launchBagSummary.launch(BagSummaryActivity.createIntent(this))
        }
    }

    private fun setupRecyclerView() {
        binding.rvItems.apply {
            layoutManager = this@VenueDetailActivity.layoutManager
            adapter = this@VenueDetailActivity.adapter
            setHasFixedSize(true)
            addItemDecoration(
                MarginItemDecoration(
                    spaceHeight = resources.getDimension(R.dimen.rv_menu_item_margin).toInt(),
                    start = resources.getDimension(R.dimen.rv_menu_start_end_margin).toInt(),
                    end = resources.getDimension(R.dimen.rv_menu_start_end_margin).toInt()
                )
            )

            addOnScrollListener(object : OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isUserScrolling = true
                    } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        isUserScrolling = false
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (isUserScrolling) {
                        val firstVisibleItemPosition = this@VenueDetailActivity.layoutManager.findFirstCompletelyVisibleItemPosition()
                        val tabIndex = this@VenueDetailActivity.adapter.getCategoryIndexByPosition(firstVisibleItemPosition)
                        if (binding.tabLayout.selectedTabPosition != tabIndex) {
                            binding.tabLayout.getTabAt(tabIndex)?.select()
                        }
                    }
                }
            })
        }
    }

    private fun updateTags(tags: List<String>) {
        tags.forEach { tag ->
            val view = ViewDisplayChipsBinding.inflate(layoutInflater)
            view.tvLabel.text = tag
            binding.layoutChips.addView(view.root)

            (view.root.layoutParams as? ViewGroup.MarginLayoutParams)?.updateMargins(
                bottom = resources.getDimensionPixelSize(R.dimen.tag_spacing),
                right = resources.getDimensionPixelSize(R.dimen.tag_spacing)
            )
        }

    }

    fun onClickUpBtn(view: View) {
        finish()
    }

    private fun subscribe() {
        viewModel.menuObservable.observe(this, Observer { resource ->
            if (resource is Resource.Success) {
                binding.tabLayout.removeAllTabs()
                val menu = resource.data
                menu.categories.map { it.categoryName }.forEach { categoryName ->
                    binding.tabLayout.addTab(tabLayout.newTab().setText(categoryName))
                }
                adapter.setCategories(menu.categories)
            } else if (resource is Resource.InProgress) {
                dialogManager.showLoadingDialog(getString(R.string.loading_menu))
            }

            if (resource !is Resource.InProgress) {
                dialogManager.hideLoadingDialog()
            }
        })

        viewModel.venueObservable.observe(this, Observer { resource ->
            if (resource is Resource.InProgress) {
                dialogManager.showLoadingDialog(getString(R.string.loading_menu))
            } else if (resource is Resource.Error) {
                dialogManager.hideLoadingDialog()
            } else if (resource is Resource.Success) {
                updateTags(resource.data.categories)
            }
        })

        viewModel.goToGoogleMap.observe(this, Observer { event ->
            event.handleEvent { uri ->
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        })

        viewModel.goToDialer.observe(this, Observer { event ->
            event.handleEvent { uri ->
                val callIntent = Intent(Intent.ACTION_DIAL, uri)
                startActivity(callIntent)
            }
        })

        viewModel.goToEmail.observe(this, Observer { event ->
            event.handleEvent { email ->
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    data = Uri.parse("mailto:")
                }
                startActivity(
                    Intent.createChooser(
                        emailIntent,
                        "Send Email Using: "
                    )
                );
            }
        })
    }


    override fun onAddLineItem(bagSummary: BagSummary) {
        viewModel.updateBag(bagSummary)
    }

    override fun onUpdateLineItem(bagSummary: BagSummary) {
        viewModel.updateBag(bagSummary)
    }

    companion object {
        fun createIntent(context: Context, venue: Venue): Intent {
            return Intent(context, VenueDetailActivity::class.java).apply {
                putExtra(EXTRA_VENUE, venue)
            }
        }

        fun createIntent(context: Context, venueId: String): Intent {
            return Intent(context, VenueDetailActivity::class.java).apply {
                putExtra(EXTRA_VENUE_ID, venueId)
            }
        }

    }
}