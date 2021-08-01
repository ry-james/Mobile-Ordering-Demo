package com.ryanjames.swabergersmobilepos.feature.orderdetails

import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    private val _orderSummary = MutableLiveData<Resource<BagSummary>>()
    val getOrderSummary: LiveData<Resource<BagSummary>>
        get() = _orderSummary

    val tax: LiveData<String> = Transformations.map(getOrderSummary) { resource ->
        resource.mapIfSuccess { bagSummary ->
            "$".plus(bagSummary.tax().toTwoDigitString())
        } ?: 0f.toTwoDigitString()
    }

    val subtotal: LiveData<String> = Transformations.map(getOrderSummary) { resource ->
        resource.mapIfSuccess { bagSummary ->
            "$".plus(bagSummary.subtotal().toTwoDigitString())
        } ?: 0f.toTwoDigitString()
    }

    val total: LiveData<String> = Transformations.map(getOrderSummary) { resource ->
        resource.mapIfSuccess { bagSummary ->
            "$".plus(bagSummary.price.toTwoDigitString())
        } ?: 0f.toTwoDigitString()
    }

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
                        _orderSummary.value = Resource.Success(bagSummary)
                        setLoadingViewVisibility(View.GONE)
                        _orderDetailsVisibility.value = View.VISIBLE

                        _btnCancelOrderVisibility.value = if (bagSummary.status == OrderStatus.CHECKOUT) View.VISIBLE else View.GONE

                        if (bagSummary.status == OrderStatus.CREATED && bagSummary.orderId == currentOrderId) {
                            setBannerMessage(R.string.current_order_message)
                        } else if (bagSummary.status == OrderStatus.CANCELLED) {
                            setBannerMessage(R.string.you_have_cancelled_this_order)
                        } else if (bagSummary.status == OrderStatus.CHECKOUT) {
                            setBannerMessage(R.string.cancellable_order)
                        } else if (bagSummary.status == OrderStatus.PREPARING) {
                            setBannerMessage(R.string.preparing_order)
                        } else if (bagSummary.status == OrderStatus.PICKED_UP) {
                            setBannerMessage(R.string.picked_up)
                        } else if (bagSummary.status == OrderStatus.READY_FOR_PICKUP) {
                            setBannerMessage(R.string.ready_for_pickup)
                        } else if (bagSummary.status == OrderStatus.DELIVERED) {
                            setBannerMessage(R.string.delivered)
                        } else if (bagSummary.status == OrderStatus.DELIVERING) {
                            setBannerMessage(R.string.delivering_order)
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

    private fun setBannerMessage(@StringRes stringId: Int) {
        _orderBannerVisibility.value = View.VISIBLE
        _bannerMessage.value = stringId
    }

    fun cancelOrder() {
        _orderSummary.value?.takeIfSuccess {
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
            textColor = R.color.textColorBlack
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}