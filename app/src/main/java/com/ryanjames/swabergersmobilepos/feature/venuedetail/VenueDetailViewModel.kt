package com.ryanjames.swabergersmobilepos.feature.venuedetail

import android.net.Uri
import android.view.View
import androidx.lifecycle.*
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.commonbinding.ViewCartBindingModel
import com.ryanjames.swabergersmobilepos.core.StringResourceArgs
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.domain.Venue
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class VenueDetailViewModel @Inject constructor(
    val menuRepository: MenuRepository,
    val orderRepository: OrderRepository,
    val venueRepository: VenueRepository
) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val _menuObservable = MutableLiveData<Resource<Menu>>()
    val menuObservable: LiveData<Resource<Menu>>
        get() = _menuObservable

    private val _venueObservable = MutableLiveData<Resource<Venue>>()
    val venueObservable: LiveData<Resource<Venue>>
        get() = _venueObservable

    private val _viewCartBinding = MutableLiveData<ViewCartBindingModel>(ViewCartBindingModel(View.GONE, "", View.GONE))
    val viewCartBinding: LiveData<ViewCartBindingModel>
        get() = _viewCartBinding

    private val _venueBinding = MutableLiveData<VenueDataModel>()
    val venueBinding: LiveData<VenueDataModel>
        get() = _venueBinding

    private val _goToGoogleMap = MutableLiveData<Event<Uri>>()
    val goToGoogleMap: LiveData<Event<Uri>>
        get() = _goToGoogleMap

    private val _goToDialer = MutableLiveData<Event<Uri>>()
    val goToDialer: LiveData<Event<Uri>>
        get() = _goToDialer

    private val _goToEmail = MutableLiveData<Event<String>>()
    val goToEmail: LiveData<Event<String>>
        get() = _goToEmail

    val directionsTileBinding = Transformations.map(venueBinding) {
        ViewCircularTileBindingModel(
            label = StringResourceArgs(R.string.directions),
            icon = R.drawable.signboard,
            onClickListener = {
                venue?.let {
                    val gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=${it.latLng.latitude},${it.latLng.longitude}")
                    _goToGoogleMap.value = Event(gmmIntentUri)
                }
            },
            visibility = View.VISIBLE
        )
    }

    val emailTileBinding = Transformations.map(venueBinding) {
        ViewCircularTileBindingModel(
            label = StringResourceArgs(R.string.email),
            icon = R.drawable.email,
            onClickListener = {
                venue?.let {
                    _goToEmail.value = Event("sample@mailinator.com")
                }
            },
            visibility = View.VISIBLE
        )
    }

    val callTileBinding = Transformations.map(venueBinding) {
        ViewCircularTileBindingModel(
            label = StringResourceArgs(R.string.call),
            icon = R.drawable.phone,
            onClickListener = {
                venue?.let {
                    _goToDialer.value = Event((Uri.parse("tel:5000000000")))
                }
            },
            visibility = View.VISIBLE
        )
    }

    val noMenuVisibility = MediatorLiveData<Int>().apply {
        value = View.GONE
        addSource(menuObservable) {
            if (it is Resource.Success && it.data.categories.isNullOrEmpty()) {
                postValue(View.VISIBLE)
            } else {
                postValue(View.GONE)
            }
        }
    }

    val errorMenuVisibility = MediatorLiveData<Int>().apply {
        value = View.GONE
        addSource(menuObservable) {
            if (it is Resource.Error) {
                postValue(View.VISIBLE)
            } else {
                postValue(View.GONE)
            }
        }
    }


    val menuVisibility = MediatorLiveData<Int>().apply {
        value = View.GONE
        addSource(menuObservable) {
            if (it is Resource.Success && it.data.categories.isNotEmpty()) {
                postValue(View.VISIBLE)
            } else {
                postValue(View.GONE)
            }
        }
    }

    var venue: Venue? = null
        private set

    private var venueId: String? = null

    init {
        getLocalBag()
    }

    private fun getLocalBag() {

        if (orderRepository.hasItemsInBag()) {
            _viewCartBinding.value = ViewCartBindingModel(View.VISIBLE, "", View.VISIBLE)

            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ localBag ->
                    updateBag(localBag)
                }, { error -> error.printStackTrace() }
                ).disposedBy(compositeDisposable)

        } else {
            _viewCartBinding.value = ViewCartBindingModel(View.GONE, "", View.VISIBLE)
        }
    }

    fun updateBag(bagSummary: BagSummary) {
        if (bagSummary.lineItems.isNotEmpty()) {
            _viewCartBinding.value = ViewCartBindingModel(View.VISIBLE, "$".plus(bagSummary.price.toTwoDigitString()), View.GONE)
        } else {
            _viewCartBinding.value = ViewCartBindingModel(View.GONE, "", View.GONE)
        }
    }

    fun getVenueAndMenuById(venueId: String) {
        this.venueId = venueId

        venueRepository.getVenueById(venueId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _venueObservable.value = Resource.InProgress
            }
            .subscribe({ venue ->
                getMenu(venue)
            }, { error ->
                error.printStackTrace()
                _menuObservable.value = Resource.Error(error)
            })
            .disposedBy(compositeDisposable)
    }

    fun getMenu(venue: Venue) {
        if (_menuObservable.value is Resource.InProgress) return

        this.venue = venue
        menuRepository.getBasicMenu(venue.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _menuObservable.value = Resource.InProgress
                _venueBinding.value = VenueDataModel(venue)
                _venueObservable.value = Resource.Success(venue)
            }
            .subscribe(
                { menu ->
                    _menuObservable.value = Resource.Success(menu)
                },
                { error ->
                    error.printStackTrace()
                    _menuObservable.value = Resource.Error(error)
                }).disposedBy(compositeDisposable)
    }

    fun onClickRetry() {
        venue?.let { getMenu(it) } ?: venueId?.let { getVenueAndMenuById(it) }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}