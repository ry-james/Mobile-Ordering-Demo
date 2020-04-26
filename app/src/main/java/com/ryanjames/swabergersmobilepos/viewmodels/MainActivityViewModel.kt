package com.ryanjames.swabergersmobilepos.viewmodels

import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class MainActivityViewModel @Inject constructor(var menuRepository: MenuRepository) : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        return menuRepository.authenticate(username, password)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}