package com.ryanjames.swabergersmobilepos.feature.home

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseFragment
import com.ryanjames.swabergersmobilepos.core.HorizontalMarginItemDecoration
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.FragmentHomeBinding
import com.ryanjames.swabergersmobilepos.databinding.ViewFeaturedCardBinding
import com.ryanjames.swabergersmobilepos.databinding.ViewRestaurantCardBinding
import com.ryanjames.swabergersmobilepos.domain.HomeVenues
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.feature.venuedetail.VenueDetailActivity
import com.ryanjames.swabergersmobilepos.fragments.DeliveryAddressBottomSheetFragment
import com.ryanjames.swabergersmobilepos.helper.TAG
import com.ryanjames.swabergersmobilepos.helper.display
import javax.inject.Inject

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: HomeViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribe()

        binding.viewpagerFeatured.apply {
            adapter = FeaturedPagerAdapter(viewModel.featuredVenuesObservable, viewLifecycleOwner) { venue ->
                startActivity(VenueDetailActivity.createIntent(context, venue))
            }
            TabLayoutMediator(binding.tabLayout, this) { tab, position -> }.attach()
        }

        binding.rvRestaurants.apply {
            adapter = RestaurantAdapter(viewModel.featuredVenuesObservable, viewLifecycleOwner) {venue ->
                startActivity(VenueDetailActivity.createIntent(context, venue))
            }
            layoutManager = LinearLayoutManager(this@HomeFragment.context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(HorizontalMarginItemDecoration(resources.getDimension(R.dimen.default_rv_item_margin).toInt()))
        }

        binding.ivEdit.setOnClickListener {
            parentFragmentManager.display(TAG, DeliveryAddressBottomSheetFragment.createInstance())
        }

        binding.tvDeliveringTo.setOnClickListener {
            parentFragmentManager.display(TAG, DeliveryAddressBottomSheetFragment.createInstance())
        }
    }

    private fun subscribe() {
        viewModel.featuredVenuesObservable.observe(viewLifecycleOwner, Observer {
            if (it is Resource.InProgress) {
                binding.shimmerView.startShimmer()
            } else {
                fadeOutShimmer()
            }
        })

        viewModel.deliveryAddress.observe(viewLifecycleOwner, Observer { deliveryAddress ->
            context?.let {
                if (deliveryAddress != null) {
                    val deliveringTo = getString(R.string.delivering_to)
                    val spannableString = SpannableString("$deliveringTo\n$deliveryAddress")
                    spannableString.setSpan(RelativeSizeSpan(1.25f), 0, deliveringTo.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorWhite, activity?.theme)), 0, deliveringTo.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    binding.tvDeliveringTo.text = spannableString
                } else {
                    val setDeliveryAddress = getString(R.string.set_delivery_address)
                    val spannableString = SpannableString(setDeliveryAddress)
                    spannableString.setSpan(RelativeSizeSpan(1.25f), 0, setDeliveryAddress.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    spannableString.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorWhite, activity?.theme)), 0, setDeliveryAddress.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    binding.tvDeliveringTo.text = spannableString
                }

            }
        })
    }

    private fun fadeOutShimmer() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        binding.shimmerView.startAnimation(animation)
        binding.viewShimmerCurtain.startAnimation(animation)
        binding.shimmerView.stopShimmer()
    }

    class RestaurantAdapter(venuesObservable: LiveData<Resource<HomeVenues>>,
                            private val lifecycleOwner: LifecycleOwner,
                            val listener: (Venue) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var venueList = listOf<Venue>()

        init {
            venuesObservable.observe(lifecycleOwner, Observer {
                if (it is Resource.Success) {
                    this.venueList = it.data.restaurants
                    notifyDataSetChanged()
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ViewRestaurantCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.lifecycleOwner = lifecycleOwner
            return FeaturedCardViewHolder(binding)
        }

        override fun getItemCount(): Int = venueList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FeaturedCardViewHolder) {
                holder.bind(venueList[position], listener)
            }
        }

        class FeaturedCardViewHolder(private val binding: ViewRestaurantCardBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(venue: Venue, listener: (Venue) -> Unit) {
                binding.root.setOnClickListener {
                    listener.invoke(venue)
                }

                val dataModel = FeaturedCardDataModel(venue)
                binding.dataModel = dataModel
                binding.executePendingBindings()
            }
        }
    }


    class FeaturedPagerAdapter(
        venuesObservable: LiveData<Resource<HomeVenues>>,
        private val lifecycleOwner: LifecycleOwner,
        val listener: (Venue) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var venueList = listOf<Venue>()

        init {
            venuesObservable.observe(lifecycleOwner, Observer {
                if (it is Resource.Success) {
                    this.venueList = it.data.featuredVenues
                    notifyDataSetChanged()
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ViewFeaturedCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.lifecycleOwner = lifecycleOwner
            return FeaturedCardViewHolder(binding)
        }

        override fun getItemCount(): Int = venueList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FeaturedCardViewHolder) {
                holder.bind(venueList[position], listener)
            }
        }

        class FeaturedCardViewHolder(private val binding: ViewFeaturedCardBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(venue: Venue, listener: (Venue) -> Unit) {
                binding.root.setOnClickListener {
                    listener.invoke(venue)
                }

                val dataModel = FeaturedCardDataModel(venue)
                binding.dataModel = dataModel
                binding.executePendingBindings()
            }
        }
    }
}