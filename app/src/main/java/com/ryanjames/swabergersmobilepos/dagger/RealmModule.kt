package com.ryanjames.swabergersmobilepos.dagger

import com.ryanjames.swabergersmobilepos.database.realm.GlobalRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.MenuRealmDao
import com.ryanjames.swabergersmobilepos.database.realm.OrderRealmDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class RealmModule {

    @Singleton
    @Provides
    fun provideOrderRealmDao(): OrderRealmDao {
        return OrderRealmDao()
    }

    @Singleton
    @Provides
    fun provideGlobalRealmDao(): GlobalRealmDao {
        return GlobalRealmDao()
    }

    @Singleton
    @Provides
    fun provideMenuRealmDao(): MenuRealmDao {
        return MenuRealmDao()
    }
}