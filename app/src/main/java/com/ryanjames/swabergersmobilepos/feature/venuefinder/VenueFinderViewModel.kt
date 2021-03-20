package com.ryanjames.swabergersmobilepos.feature.venuefinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.domain.VenueMarker
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VenueFinderViewModel @Inject constructor(
    val venueRepository: VenueRepository,
    val orderRepository: OrderRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val venueMarkers = mutableListOf<VenueMarker>()
    private var selectedVenueMarker: VenueMarker? = null

    private val _onLoadVenues = MutableLiveData<List<VenueMarker>>()
    val onLoadVenues: LiveData<List<VenueMarker>>
        get() = _onLoadVenues

    private val _onShowVenueChangeConfirmation = MutableLiveData<Event<Boolean>>()
    val onShowVenueChangeConfirmation: LiveData<Event<Boolean>>
        get() = _onShowVenueChangeConfirmation

    private val _onSelectedVenueChange = MutableLiveData<Event<Venue?>>()
    val onSelectedVenueChange: LiveData<Event<Venue?>>
        get() = _onSelectedVenueChange

    // Pair of the previous selected venue and newly selected venue
    private val _onFocusedVenueChange = MutableLiveData<Pair<VenueMarker?, VenueMarker>>()
    val onFocusedVenueChange: LiveData<Pair<VenueMarker?, VenueMarker>>
        get() = _onFocusedVenueChange


    fun getStores() {

        // Check stores in memory first. If empty, get stores from repository.
        Observable.fromArray(venueMarkers.toList())
            .concatWith(venueRepository.getVenues().map { list ->
                list.map { VenueMarker(it, R.drawable.ic_pin_black) }
            })
            .filter { it.isNotEmpty() }
            .firstElement()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ venueMarkers ->
                this.venueMarkers.clear()
                this.venueMarkers.addAll(venueMarkers)
                _onLoadVenues.value = this.venueMarkers
                setSelectedMarker(selectedVenueMarker?.venue?.id ?: venueMarkers[0].venue.id)
            }, { error ->
                error.printStackTrace()
            }).disposedBy(compositeDisposable)
    }

    fun getSelectedVenue(): Venue? {
        return selectedVenueMarker?.venue
    }

    fun onClickSelect() {
        if (!orderRepository.hasItemsInBag()) {
            _onSelectedVenueChange.value = Event(getSelectedVenue())
            return
        }

        if (getSelectedVenue()?.id != venueRepository.getSelectedVenue()?.id) {
            _onShowVenueChangeConfirmation.value = Event(true)
        } else if (getSelectedVenue()?.id == venueRepository.getSelectedVenue()?.id) {
            _onSelectedVenueChange.value = Event(null)
        }
    }

    fun confirmVenueChange() {
        orderRepository.clearLocalBag()
        _onSelectedVenueChange.value = Event(getSelectedVenue())
    }

    fun setSelectedMarker(markerId: String) {
        if (markerId == selectedVenueMarker?.venue?.id) return

        val newVenueMarker = venueMarkers.find { it.venue.id == markerId }
        val oldVenueMarker = venueMarkers.find { it.venue.id == selectedVenueMarker?.venue?.id }

        if (newVenueMarker != null) {
            newVenueMarker.icon = R.drawable.ic_pin_red
            oldVenueMarker?.icon = R.drawable.ic_pin_black
            selectedVenueMarker = newVenueMarker
            _onFocusedVenueChange.value = Pair(oldVenueMarker, newVenueMarker)
        }
    }

    // Invoked when a venue card is swiped
    fun setFocusedCard(index: Int) {
        venueMarkers.getOrNull(index)?.let { venueMarker ->
            setSelectedMarker(venueMarker.venue.id)
        }
    }

}
