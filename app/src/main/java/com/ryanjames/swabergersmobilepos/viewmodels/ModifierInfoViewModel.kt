package com.ryanjames.swabergersmobilepos.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.database.entity.ModifierInfoEntity
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject


class ModifierInfoViewModel(sharedPreferences: SharedPreferences) : ViewModel() {


    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val modifierInfoRepository = MenuRepository(sharedPreferences)
    private val menuRepository = MenuRepository(sharedPreferences)
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