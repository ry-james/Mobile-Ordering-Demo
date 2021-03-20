package com.ryanjames.swabergersmobilepos.feature.menu

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class MenuFragmentViewModel @Inject constructor(
    val menuRepository: MenuRepository,
    val venueRepository: VenueRepository
) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val _menuObservable = MutableLiveData<Resource<Menu>>()
    val menuObservable: LiveData<Resource<Menu>>
        get() = _menuObservable

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _menuVisibility = MutableLiveData<Int>()
    val menuVisibility: LiveData<Int>
        get() = _menuVisibility

    private val _errorViewBinding = MutableLiveData<MesssageViewBinding>()
    val messsageViewBinding: LiveData<MesssageViewBinding>
        get() = _errorViewBinding

    private val _noRestaurantViewBinding = MutableLiveData<MesssageViewBinding>()
    val noRestaurantViewBinding: LiveData<MesssageViewBinding>
        get() = _noRestaurantViewBinding

    private val _locationBannerVisibility = MutableLiveData<Int>(View.GONE)
    val locationBannerVisibility: LiveData<Int>
        get() = _locationBannerVisibility

    private val _selectedVenue = MutableLiveData<Venue>(getSelectedVenue())
    val selectedVenue: LiveData<Venue>
        get() = _selectedVenue

    private val _selectRestaurantVisibility = MutableLiveData<Int>()
    val selectRestaurantVisibility: LiveData<Int>
        get() = _selectRestaurantVisibility

    var selectedCategoryPosition = 0
        set(value) {
            field = value
        }

    private var isFetchingMenu = false
    private var fetchMenuDisposable: Disposable? = null

    fun refreshMenu() {
        if (isFetchingMenu) return

        val selectedVenue = venueRepository.getSelectedVenue()
        _selectedVenue.value = selectedVenue

        if (selectedVenue == null) {
            setErrorViewVisibility(View.GONE)
            setLoadingViewVisibility(View.GONE)
            setNoRestaurantViewVisibility(View.VISIBLE)
            _locationBannerVisibility.value = View.GONE
        } else {
            setNoRestaurantViewVisibility(View.GONE)
            loadMenu(selectedVenue.id)
        }
    }

    fun setSelectedVenue(venue: Venue) {
        venueRepository.setSelectedVenue(venue)
        _selectedVenue.value = venue
        loadMenu(venue.id)
    }

    fun getSelectedVenue(): Venue? {
        return venueRepository.getSelectedVenue()
    }

    private fun loadMenu(venueId: String) {
        fetchMenuDisposable?.dispose()
        fetchMenuDisposable = menuRepository.getBasicMenu(venueId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                setLoadingViewVisibility(View.VISIBLE)
                setErrorViewVisibility(View.GONE)
                setNoRestaurantViewVisibility(View.GONE)
                _menuVisibility.value = View.GONE
                _locationBannerVisibility.value = View.VISIBLE
                isFetchingMenu = true
            }
            .doFinally {
                setLoadingViewVisibility(View.GONE)
                isFetchingMenu = false
            }
            .subscribe(
                { menu ->
                    if (menu.categories.isNullOrEmpty()) {
                        setErrorViewVisibility(View.VISIBLE)
                    } else {
                        _menuVisibility.value = View.VISIBLE
                        _menuObservable.value = Resource.Success(menu)
                    }
                    Log.d("MENU", menu.toString())
                },
                { error ->
                    setErrorViewVisibility(View.VISIBLE)
                    error.printStackTrace()
                    _menuObservable.value = Resource.Error(error)
                })
        fetchMenuDisposable?.disposedBy(compositeDisposable)
    }

    private fun setErrorViewVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) _selectRestaurantVisibility.value = View.GONE
        _errorViewBinding.value = MesssageViewBinding(
            visibility = visibility,
            image = R.drawable.ic_shop,
            title = R.string.error_loading_menu_title,
            message = R.string.error_loading_menu_message
        )
    }

    private fun setNoRestaurantViewVisibility(visibility: Int) {
        _selectRestaurantVisibility.value = visibility
        _noRestaurantViewBinding.value = MesssageViewBinding(
            visibility = visibility,
            image = R.drawable.ic_shop,
            title = R.string.no_restaurant,
            message = R.string.no_restaurant_message
        )
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.fetching_menu,
            textColor = R.color.colorWhite
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}