package com.ryanjames.swabergersmobilepos.feature.orderdetails

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.BagSummary
import com.ryanjames.swabergersmobilepos.domain.LoadingDialogBinding
import com.ryanjames.swabergersmobilepos.domain.OrderStatus
import com.ryanjames.swabergersmobilepos.domain.Resource
import com.ryanjames.swabergersmobilepos.helper.toTwoDigitString
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class OrderDetailsViewModel @Inject constructor(val orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _orderSummary = MutableLiveData<BagSummary>()
    val getOrderSummary: LiveData<BagSummary>
        get() = _orderSummary

    private val _tax = MutableLiveData<String>()
    val tax: LiveData<String>
        get() = _tax

    private val _subtotal = MutableLiveData<String>()
    val subtotal: LiveData<String>
        get() = _subtotal

    private val _total = MutableLiveData<String>()
    val total: LiveData<String>
        get() = _total

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _orderDetailsVisibility = MutableLiveData<Int>()
    val orderDetailsVisibility: LiveData<Int>
        get() = _orderDetailsVisibility

    private val _btnCancelOrderVisibility = MutableLiveData<Int>(View.GONE)
    val btnCancelOrderVisibility: LiveData<Int>
        get() = _btnCancelOrderVisibility

    private val _orderBannerVisibility = MutableLiveData<Int>(View.GONE)
    val orderBannerVisibility: LiveData<Int>
        get() = _orderBannerVisibility

    private val _onCancelOrder = MutableLiveData<Resource<Boolean>>()
    val onOrderCancelled: LiveData<Resource<Boolean>>
        get() = _onCancelOrder

    private val _bannerMessage = MutableLiveData<Int>(R.string.current_order_message)
    val bannerMessage: LiveData<Int>
        get() = _bannerMessage

    fun retrieveOrder(orderId: String) {

        var currentOrderId = ""
        compositeDisposable.add(
            orderRepository.getCurrentOrder()
                .flatMap {
                    currentOrderId = it.orderId
                    orderRepository.getOrderById(orderId)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    setLoadingViewVisibility(View.VISIBLE)
                    _orderDetailsVisibility.value = View.GONE
                }
                .subscribe(
                    { bagSummary ->
                        _orderSummary.value = bagSummary
                        setLoadingViewVisibility(View.GONE)
                        _orderDetailsVisibility.value = View.VISIBLE
                        updatePrices()

                        _btnCancelOrderVisibility.value = if (bagSummary.status == OrderStatus.CHECKOUT) View.VISIBLE else View.GONE

                        if (bagSummary.status == OrderStatus.CREATED && bagSummary.orderId == currentOrderId) {
                            _orderBannerVisibility.value = View.VISIBLE
                            _bannerMessage.value = R.string.current_order_message
                        } else if (bagSummary.status == OrderStatus.CANCELLED) {
                            _orderBannerVisibility.value = View.VISIBLE
                            _bannerMessage.value = R.string.you_have_cancelled_this_order
                        } else if (bagSummary.status == OrderStatus.CHECKOUT) {
                            _orderBannerVisibility.value = View.VISIBLE
                            _bannerMessage.value = R.string.cancellable_order
                        } else {
                            _orderBannerVisibility.value = View.GONE
                        }

                    }, { error ->
                        setLoadingViewVisibility(View.GONE)
                        error.printStackTrace()
                    }
                )
        )
    }

    fun cancelOrder() {
        _orderSummary.value?.let {
            compositeDisposable.add(
                orderRepository.cancelOrder(it.orderId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        _onCancelOrder.value = Resource.InProgress
                    }
                    .subscribe({
                        _onCancelOrder.value = Resource.Success(true)
                    }, { error ->
                        _onCancelOrder.value = Resource.Error(error)
                    })
            )
        }
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = R.string.fetching_order,
            textColor = R.color.colorWhite
        )
    }

    private fun updatePrices() {
        _tax.value = getOrderSummary.value?.tax()?.toTwoDigitString() ?: "0.00"
        _subtotal.value = getOrderSummary.value?.subtotal()?.toTwoDigitString() ?: "0.00"
        _total.value = getOrderSummary.value?.price?.toTwoDigitString() ?: "0.00"
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}