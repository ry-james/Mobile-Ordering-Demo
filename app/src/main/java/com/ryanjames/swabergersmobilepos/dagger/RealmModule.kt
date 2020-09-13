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
    open fun provideOrderRealmDao(): OrderRealmDao {
        return OrderRealmDao()
    }

    @Singleton
    @Provides
    open fun provideGlobalRealmDao(): GlobalRealmDao {
        return GlobalRealmDao()
    }

    @Singleton
    @Provides
    open fun provideMenuRealmDao(): MenuRealmDao {
        return MenuRealmDao()
    }
}