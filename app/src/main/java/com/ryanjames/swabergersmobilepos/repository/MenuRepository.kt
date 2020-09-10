package com.ryanjames.swabergersmobilepos.repository

import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.domain.Menu
import com.ryanjames.swabergersmobilepos.domain.Product
import com.ryanjames.swabergersmobilepos.mappers.BasicMenuMapper
import com.ryanjames.swabergersmobilepos.mappers.ProductMapper
import com.ryanjames.swabergersmobilepos.mappers.toDomain
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

    private val basicMenuMapper = BasicMenuMapper()
    private val productMapper = ProductMapper()

    fun authenticate(username: String, password: String): Single<LoginResponse> {
        return swabergersService.authenticate(username, password)
    }

    private fun basicMenuDatabaseObservable(): Maybe<Menu> {
        return menuRealmDao.getBasicMenu().map { basicMenuRealm ->
            // Disabling cache for now
            val dateCreated = basicMenuRealm.createdAt
//            val diffInMillies = Math.abs(Date(System.currentTimeMillis()).time - dateCreated.time)
//            val cacheLifeInSeconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS)
//            if (cacheLifeInSeconds >= 60 || cacheLifeInSeconds < 0) {
//                menuRealmDao.deleteMenu()
//                Menu.EMPTY
//            } else {
            basicMenuMapper.mapLocalDbToDomain(basicMenuRealm)
//            }
        }.filter { menu ->
            menu.categories.isNotEmpty()
        }
    }

    private fun basicMenuApiObservable(): Single<Menu> {
        return swabergersService.getBasicMenu()
            .doOnSuccess { menuResponse ->
                menuRealmDao.saveBasicMenu(basicMenuMapper.mapRemoteToLocalDb(menuResponse))
            }
            .map { menuResponse ->
                basicMenuMapper.mapLocalDbToDomain(basicMenuMapper.mapRemoteToLocalDb(menuResponse))
            }
    }

    fun getBasicMenu(): Observable<Menu> {
        return Observable.concat(basicMenuDatabaseObservable().toObservable(), basicMenuApiObservable().toObservable())
            .firstElement().toObservable()
    }

    fun getProductDetails(productId: String): Single<Product> {
        return Observable.concat(
            menuRealmDao.getProductDetailsById(productId).map {
                productMapper.mapLocalDbToDomain(it)
            }.toObservable(),
            swabergersService.getProductDetails(productId)
                .map { it.toDomain() }
                .doOnSuccess {
                    val productRealm = productMapper.mapDomainToLocalDb(it)
                    menuRealmDao.saveProductDetail(productRealm)
                }
                .toObservable()
        ).firstElement().toSingle()

    }


}