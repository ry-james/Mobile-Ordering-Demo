package com.ryanjames.swabergersmobilepos.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.domain.Order
import com.ryanjames.swabergersmobilepos.helper.Event
import com.ryanjames.swabergersmobilepos.helper.clearAndAddAll
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class MenuFragmentViewModel @Inject constructor(
    var menuRepository: MenuRepository,
    var orderRepository: OrderRepository
) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val _menuObservable = MutableLiveData<Menu>()
    val menuObservable: LiveData<Menu>
        get() = _menuObservable

    private val _errorLoadingMenu = MutableLiveData<Event<Boolean>>()
    val errorLoadingMenuObservable: LiveData<Event<Boolean>>
        get() = _errorLoadingMenu

    private val _bagCounter = MutableLiveData<String>().apply { value = "0" }
    val bagCounter: LiveData<String>
        get() = _bagCounter

    var selectedCategoryPosition = 0

    val orderDetails = Order(mutableListOf())

    fun retrieveMenu() {
        compositeDisposable.add(
            menuRepository.getMenu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { menu ->
                        _menuObservable.value = menu
                        Log.d("MENU", menu.toString())
                    },
                    { error ->
                        error.printStackTrace()
                        _errorLoadingMenu.value = Event(true)
                    })
        )
    }

    fun retrieveLocalBag() {
        compositeDisposable.add(
            orderRepository.getLocalBag()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ lineItems ->
                    orderDetails.lineItems.clearAndAddAll(lineItems)
                    _bagCounter.value = orderDetails.noOfItems.toString()
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    fun addLineItem(lineItem: LineItem) {
        orderRepository.insertLineItem(lineItem)
        orderDetails.lineItems.add(lineItem)
        _bagCounter.value = orderDetails.noOfItems.toString()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}