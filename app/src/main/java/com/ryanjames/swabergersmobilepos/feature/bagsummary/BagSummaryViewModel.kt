package com.ryanjames.swabergersmobilepos.feature.bagsummary

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.*
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BagSummaryViewModel @Inject constructor(var orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _nonEmptyBagVisibility = MutableLiveData<Int>()
    val nonEmptyBagVisibility: LiveData<Int>
        get() = _nonEmptyBagVisibility

    private val _errorViewBinding = MutableLiveData<ErrorViewBinding>()
    val errorViewBinding: LiveData<ErrorViewBinding>
        get() = _errorViewBinding

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal

    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

    private val _localBag = MutableLiveData<Resource<BagSummary>>()
    val getLocalBag: LiveData<Resource<BagSummary>>
        get() = _localBag

    private val _onClearBag = MutableLiveData<Boolean>()
    val onClearBag: LiveData<Boolean>
        get() = _onClearBag

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _nonEmptyBagVisibility.value = View.GONE
                    setErrorViewVisibility(View.GONE)
                    setEmptyBagViewVisibility(View.GONE)
                    setLoadingViewVisibility(View.VISIBLE)
                }
                .doFinally {
                    setLoadingViewVisibility(View.GONE)
                }
                .subscribe({ bagSummary ->
                    if (bagSummary == BagSummary.emptyBag) {
                        updateBagVisibility()
                    } else {
                        setLocalBag(bagSummary)
                        updateBagVisibility()
                        updatePrices()
                    }
                },
                    { error ->
                        error.printStackTrace()
                        setEmptyBagViewVisibility(View.GONE)
                        _nonEmptyBagVisibility.value = View.GONE
                        setErrorViewVisibility(View.VISIBLE)
                    })
        )
    }

    private fun localBag(): BagSummary? {
        val resource = getLocalBag.value
        if (resource is Resource.Success) {
            return resource.data
        }
        return null
    }

    private fun setLocalBag(bagSummary: BagSummary) {
        _localBag.value = Resource.Success(bagSummary)
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.fetching_bag,
            textColor = R.color.colorWhite
        )
    }

    private fun setEmptyBagViewVisibility(visibility: Int) {
        _errorViewBinding.value = ErrorViewBinding(
            visibility = visibility,
            image = R.drawable.ic_empty_bag,
            title = R.string.empty_bag_title,
            message = R.string.empty_bag_subtitle
        )
    }

    private fun setErrorViewVisibility(visibility: Int) {
        _errorViewBinding.value = ErrorViewBinding(
            visibility = visibility,
            image = R.drawable.ic_error,
            title = R.string.us_not_you,
            message = R.string.error_fetching_bag
        )
    }

    private fun updateBagVisibility() {
        if (localBag()?.lineItems?.isNotEmpty() == true) {
            setEmptyBagViewVisibility(View.GONE)
            _nonEmptyBagVisibility.value = View.VISIBLE
        } else {
            setEmptyBagViewVisibility(View.VISIBLE)
            _nonEmptyBagVisibility.value = View.GONE
        }
    }

    private fun updatePrices() {
        _tax.value = localBag()?.tax()?.toTwoDigitString() ?: "0.00"
        _subtotal.value = localBag()?.subtotal()?.toTwoDigitString() ?: "0.00"
        _total.value = localBag()?.price?.toTwoDigitString() ?: "0.00"
    }

    fun setBagSummary(bagSummary: BagSummary) {
        setLocalBag(bagSummary)
        updatePrices()
        updateBagVisibility()
    }

    fun clearBag() {
        setLocalBag(BagSummary(emptyList(), 0f, OrderStatus.UNKNOWN, ""))
        updatePrices()
        updateBagVisibility()
    }

    fun notifyCheckoutSuccess() {
        clearBag()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}