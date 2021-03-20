package com.ryanjames.swabergersmobilepos.feature.venuefinder

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityVenueFinderBinding
import com.ryanjames.swabergersmobilepos.databinding.CardMapTileBinding
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.domain.getLatLngBounds
import com.ryanjames.swabergersmobilepos.helper.bitmapDescriptorFromVector
import javax.inject.Inject

private const val EXTRA_VENUE = "extra.venue"

class VenueFinderActivity : BaseActivity(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityVenueFinderBinding
    private var googleMap: GoogleMap? = null
    private val mapAdapter = MapCardItemAdapter()

    private val viewModel: VenueFinderViewModel by viewModels { viewModelFactory }

    private val markerOptions = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_venue_finder)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setupMap()
        setupRecyclerView()
        subscribe()
        viewModel.getStores()
    }

    private fun setupRecyclerView() {
        binding.vpMapCards.apply {
            adapter = mapAdapter

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    viewModel.setFocusedCard(position)
                }
            })
        }
    }

    private fun subscribe() {
        viewModel.onLoadVenues.observe(this, Observer { venueMarkers ->
            googleMap?.clear()
            markerOptions.clear()

            venueMarkers.forEach { venueMarker ->
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(venueMarker.venue.latLng).title(venueMarker.venue.name)
                        .icon(bitmapDescriptorFromVector(venueMarker.icon))
                )?.apply {
                    tag = venueMarker.venue.id
                    markerOptions.add(this)
                }
            }
            mapAdapter.setVenueList(venueMarkers.map { it.venue })

            // Set default position of view pager
            val selectedVenue = getSelectedVenueFromIntent(intent)
            val index = venueMarkers.indexOfFirst { it.venue.id == selectedVenue?.id }
            if (selectedVenue != null && index != -1) {
                binding.vpMapCards.setCurrentItem(index, false)
            } else {
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(venueMarkers.getLatLngBounds(), 128)
                googleMap?.animateCamera(cameraUpdate)
            }
        })

        viewModel.onFocusedVenueChange.observe(this, Observer { (oldVenueMarker, newVenueMarker) ->

            // Set icon of new venue
            markerOptions.find { it.tag == newVenueMarker.venue.id }?.apply {
                this.setIcon(bitmapDescriptorFromVector(newVenueMarker.icon))
            }

            // Set position of view pager
            markerOptions.indexOfFirst { it.tag == newVenueMarker.venue.id }.also { index ->
                if (index != -1) {
                    binding.vpMapCards.currentItem = index
                }
            }

            // Set icon of old venue
            markerOptions.find { it.tag == oldVenueMarker?.venue?.id }?.apply {
                if (oldVenueMarker != null) {
                    this.setIcon(bitmapDescriptorFromVector(oldVenueMarker.icon))
                }
            }

            // Update camera
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(newVenueMarker.venue.latLng, 15f)
            googleMap?.animateCamera(cameraUpdate)
        })

        viewModel.onShowVenueChangeConfirmation.observe(this, Observer { event ->
            event.handleEvent {
                AlertDialog.Builder(this).setMessage(getString(R.string.changing_venue_message))
                    .setPositiveButton(R.string.cta_yes) { dialog, _ ->
                        dialog.dismiss()
                        viewModel.confirmVenueChange()
                    }
                    .setNegativeButton(R.string.cta_no) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        })

        viewModel.onSelectedVenueChange.observe(this, Observer { event ->
            event.handleEvent { venue ->
                if (venue != null) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(EXTRA_VENUE, venue)
                    })
                }
                finish()
            }
        })
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        this.googleMap?.setOnMarkerClickListener { marker ->
            viewModel.setSelectedMarker(marker.tag.toString())
            true
        }
    }


    class MapCardItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var venueList = listOf<Venue>()

        fun setVenueList(venueList: List<Venue>) {
            this.venueList = venueList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = CardMapTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CardMapViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return venueList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is CardMapViewHolder) {
                holder.bind(venueList[position])
            }
        }

        class CardMapViewHolder(private val binding: CardMapTileBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(venue: Venue) {
                binding.tvTitle.text = venue.name
                binding.tvAddress.text = venue.address
            }

        }
    }

    companion object {

        fun createIntent(context: Context?, venue: Venue?): Intent {
            return Intent(context, VenueFinderActivity::class.java).apply {
                putExtra(EXTRA_VENUE, venue)
            }
        }

        fun getSelectedVenueFromIntent(intent: Intent?): Venue? {
            return intent?.getParcelableExtra(EXTRA_VENUE)
        }
    }
}