package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.mappers.MenuMapper
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class MenuRepository @Inject constructor(
    val swabergersService: SwabergersService,
    val menuRealmDao: MenuRealmDao
) {

    private val menuMapper = MenuMapper()

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        return swabergersService.authenticate(username, password)
    }

    private fun databaseObservable(): Maybe<Menu> {
        return menuRealmDao.getMenu().map { menuRealm ->
            // Disabling cache for now
            val dateCreated = menuRealm.createdAt
//            val diffInMillies = Math.abs(Date(System.currentTimeMillis()).time - dateCreated.time)
//            val cacheLifeInSeconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS)
//            if (cacheLifeInSeconds >= 60 || cacheLifeInSeconds < 0) {
//                menuRealmDao.deleteMenu()
//                Menu.EMPTY
//            } else {
            menuMapper.mapLocalToDomain(menuRealm)
//            }
        }.filter { menu ->
            menu.categories.isNotEmpty()
        }
    }

    private fun apiObservable(): Single<Menu> {
        return swabergersService.getMenu()
            .doOnSuccess { menuResponse -> menuRealmDao.saveMenu(menuMapper.mapRemoteToLocal(menuResponse)) }
            .map { menu ->
                menuMapper.mapLocalToDomain(menuMapper.mapRemoteToLocal(menu))
            }
    }

    fun getMenu(): Observable<Menu> {
        return Observable.concat(databaseObservable().toObservable(), apiObservable().toObservable())
            .firstElement().toObservable()
    }


}