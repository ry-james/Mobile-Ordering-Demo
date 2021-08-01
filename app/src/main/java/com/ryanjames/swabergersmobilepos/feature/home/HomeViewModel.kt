package com.ryanjames.swabergersmobilepos.feature.home

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.HomeVenues
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.helper.disposedBy
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    val venueRepository: VenueRepository,
    val orderRepository: OrderRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var _featuredVenues = MutableLiveData<Resource<HomeVenues>>()
    val featuredVenuesObservable: LiveData<Resource<HomeVenues>>
        get() = _featuredVenues

    private var _shimmerVisibility = MutableLiveData<Int>()
    val shimmerVisibility: LiveData<Int>
        get() = _shimmerVisibility

    private var _restaurantsVisibility = MutableLiveData<Int>()
    val restaurantsVisibility: LiveData<Int>
        get() = _restaurantsVisibility

    private var _errorVisibility = MutableLiveData<Int>()
    val errorVisibility: LiveData<Int>
        get() = _errorVisibility

    val deliveryAddress: LiveData<String?>
        get() = orderRepository.getDeliveryAddressObservable()

    init {
        getFeaturedStores()
    }


    private fun getFeaturedStores() {

        if (isLoadingVenues()) return

        venueRepository.getHomeVenues().toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _restaurantsVisibility.value = View.GONE
                _errorVisibility.value = View.GONE
                _shimmerVisibility.value = View.VISIBLE
                _featuredVenues.value = Resource.InProgress
            }
            .subscribe({
                _restaurantsVisibility.value = View.VISIBLE
                _errorVisibility.value = View.GONE
                _shimmerVisibility.value = View.GONE
                _featuredVenues.value = Resource.Success(it)
            }, { error ->
                _restaurantsVisibility.value = View.GONE
                _errorVisibility.value = View.VISIBLE
                _featuredVenues.value = Resource.Error(error)
                _shimmerVisibility.value = View.GONE
            })
            .disposedBy(compositeDisposable)
    }

    private fun isLoadingVenues(): Boolean {
        return _featuredVenues.value is Resource.InProgress
    }

    fun onClickRetry() {
        getFeaturedStores()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}