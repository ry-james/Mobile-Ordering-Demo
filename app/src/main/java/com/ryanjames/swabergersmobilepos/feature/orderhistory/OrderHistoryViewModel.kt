package com.ryanjames.swabergersmobilepos.feature.orderhistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class OrderHistoryViewModel @Inject constructor(val orderRepository: OrderRepository) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val _onRetrieveOrderHistory = MutableLiveData<List<Order>>()
    val onRetrieveOrderHistory: LiveData<List<Order>>
        get() = _onRetrieveOrderHistory

    fun retrieveOrderHistory() {
        compositeDisposable.add(
            orderRepository.getOrderHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ orderList ->
                    _onRetrieveOrderHistory.value = orderList
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}