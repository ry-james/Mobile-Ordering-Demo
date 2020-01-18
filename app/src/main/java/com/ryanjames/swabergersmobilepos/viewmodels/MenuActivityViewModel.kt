package com.ryanjames.swabergersmobilepos.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ryanjames.swabergersmobilepos.domain.LineItem
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.domain.OrderDetails
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class MenuActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val menuRepository = MenuRepository(application)

    private val _menuObservable = MutableLiveData<Menu>()
    val menuObservable: LiveData<Menu>
        get() = _menuObservable

    private val _bagCounter = MutableLiveData<String>().apply { value = "0" }
    val bagCounter: LiveData<String>
        get() = _bagCounter

    val orderDetails = OrderDetails(mutableListOf())

    fun retrieveMenu() {
        compositeDisposable.add(
            menuRepository.getMenu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ menu ->
                    _menuObservable.value = menu
                    Log.d("MENU", menu.toString())
                },
                    { error -> error.printStackTrace() })
        )

    }

    fun addLineItem(lineItem: LineItem) {
        orderDetails.lineItems.add(lineItem)
        _bagCounter.value = orderDetails.noOfItems.toString()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}