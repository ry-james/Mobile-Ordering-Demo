package com.ryanjames.swabergersmobilepos.feature.menu

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.domain.ErrorViewBinding
import com.ryanjames.swabergersmobilepos.domain.LoadingDialogBinding
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.domain.Resource
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
    private val _menuObservable = MutableLiveData<Resource<Menu>>()
    val menuObservable: LiveData<Resource<Menu>>
        get() = _menuObservable

    private val _loadingViewBinding = MutableLiveData<LoadingDialogBinding>()
    val loadingViewBinding: LiveData<LoadingDialogBinding>
        get() = _loadingViewBinding

    private val _menuVisibility = MutableLiveData<Int>()
    val menuVisibility: LiveData<Int>
        get() = _menuVisibility

    private val _errorViewBinding = MutableLiveData<ErrorViewBinding>()
    val errorViewBinding: LiveData<ErrorViewBinding>
        get() = _errorViewBinding

    var selectedCategoryPosition = 0
    private var isFetchingMenu = false

    fun retrieveMenu() {
        if (isFetchingMenu) return

        compositeDisposable.add(
            menuRepository.getBasicMenu()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    setLoadingViewVisibility(View.VISIBLE)
                    setErrorViewVisibility(View.GONE)
                    _menuVisibility.value = View.GONE
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
        )
    }

    private fun setErrorViewVisibility(visibility: Int) {
        _errorViewBinding.value = ErrorViewBinding(
            visibility = visibility,
            image = R.drawable.ic_menu,
            title = "Oh, snap!",
            message = "We can't load the menu at the moment. Please try again later."
        )
    }

    private fun setLoadingViewVisibility(visibility: Int) {
        _loadingViewBinding.value = LoadingDialogBinding(
            visibility = visibility,
            loadingText = "Fetching menu...",
            textColor = R.color.colorWhite
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}