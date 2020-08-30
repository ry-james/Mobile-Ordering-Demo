package com.ryanjames.swabergersmobilepos.feature.menu

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.helper.Event
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

    var selectedCategoryPosition = 0

    fun retrieveMenu() {
        compositeDisposable.add(
            menuRepository.getBasicMenu()
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

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}