package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.VenueRealmDao
import com.ryanjames.swabergersmobilepos.helper.LoginManager
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.repository.VenueRepository
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
    open fun provideLoginManager(sharedPreferences: SharedPreferences): LoginManager {
        return LoginManager(sharedPreferences)
    }

    @Singleton
    @Provides
    open fun provideOrderRepository(apiService: ApiService, orderRealmDao: OrderRealmDao, globalRealmDao: GlobalRealmDao): OrderRepository {
        return OrderRepository(apiService, orderRealmDao, globalRealmDao)
    }

    @Singleton
    @Provides
    open fun provideMenuRepository(apiService: ApiService, menuRealmDao: MenuRealmDao): MenuRepository {
        return MenuRepository(apiService, menuRealmDao)
    }

    @Singleton
    @Provides
    open fun provideStoreRepository(apiService: ApiService, globalRealmDao: GlobalRealmDao, venueRealmDao: VenueRealmDao): VenueRepository {
        return VenueRepository(apiService, globalRealmDao, venueRealmDao)
    }

    @Singleton
    @Provides
    open fun provideResources(application: Application): Resources {
        return application.resources
    }
}