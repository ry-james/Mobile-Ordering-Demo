package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class ApplicationModule {


    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences("Preference File", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideOrderRepository(sharedPreferences: SharedPreferences): OrderRepository {
        return OrderRepository(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideMenuRepository(sharedPreferences: SharedPreferences): MenuRepository {
        return MenuRepository(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideResources(application: Application): Resources {
        return application.resources
    }

}