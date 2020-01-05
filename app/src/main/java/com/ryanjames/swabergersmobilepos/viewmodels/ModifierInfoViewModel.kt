package com.ryanjames.swabergersmobilepos.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ryanjames.swabergersmobilepos.database.entity.ModifierInfoEntity
import com.ryanjames.swabergersmobilepos.domain.ModifierGroup
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.network.responses.ModifierGroupResponse
import com.ryanjames.swabergersmobilepos.network.responses.ModifierInfosResponse
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class ModifierInfoViewModel(application: Application) : AndroidViewModel(application) {


    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val modifierInfoRepository = MenuRepository(application)
    private val menuRepository = MenuRepository(application)
    val getModifierInfosObservable = PublishSubject.create<List<ModifierInfoEntity>>()
    val insertErrorObservable = PublishSubject.create<Throwable>()


    fun authenticate(username: String, password: String): Single<LoginResponse> {
        return modifierInfoRepository.authenticate(username, password)
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}