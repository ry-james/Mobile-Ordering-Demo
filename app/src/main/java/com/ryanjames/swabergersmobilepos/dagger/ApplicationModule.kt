package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class ApplicationModule {

    @Singleton
    @Provides
    open fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("Preference File", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    open fun provideOrderRepository(swabergersService: SwabergersService, orderRealmDao: OrderRealmDao, globalRealmDao: GlobalRealmDao): OrderRepository {
        return OrderRepository(swabergersService, orderRealmDao, globalRealmDao)
    }

    @Singleton
    @Provides
    open fun provideMenuRepository(swabergersService: SwabergersService, menuRealmDao: MenuRealmDao): MenuRepository {
        return MenuRepository(swabergersService, menuRealmDao)
    }

    @Singleton
    @Provides
    open fun provideResources(application: Application): Resources {
        return application.resources
    }

}