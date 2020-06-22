package com.ryanjames.swabergersmobilepos.repository

import android.content.SharedPreferences
import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.mappers.MenuMapper
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

class MenuRepository(sharedPreferences: SharedPreferences) {

    private val menuRealmDao = MenuRealmDao()

    private val swabergersService: SwabergersService =
        SwabergersService(sharedPreferences)

    private val menuMapper = MenuMapper()

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        return swabergersService.authenticate(username, password)
    }

    private fun databaseObservable(): Maybe<Menu> {
        return menuRealmDao.getMenu().map { menuRealm ->
//            Disabling cache for now
//            val cacheLifeInMinutes = menu.createdAt.toLocalDateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES)
//            if (cacheLifeInMinutes >= 60 || cacheLifeInMinutes < 0) {
//                menuRealmDao.deleteMenu()
//                Menu.EMPTY
//            } else {
            menuMapper.mapToDomain(menuRealm)
//            }
        }.filter { menu ->
            menu.categories.isNotEmpty()
        }
    }

    private fun apiObservable(): Single<Menu> {
        return swabergersService.getMenu()
            .doOnSuccess { menuResponse -> menuRealmDao.saveMenu(menuMapper.mapToEntity(menuResponse)) }
            .map { menu ->
                menuMapper.mapToDomain(menuMapper.mapToEntity(menu))
            }
    }

    fun getMenu(): Single<Menu> {
        return Observable.concat(databaseObservable().toObservable(), apiObservable().toObservable())
            .firstElement().toSingle()
    }


}