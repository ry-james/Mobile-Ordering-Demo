package com.ryanjames.swabergersmobilepos.dagger

import android.content.SharedPreferences
import com.ryanjames.swabergersmobilepos.network.retrofit.SwabergersService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class NetworkModule {

    @Singleton
    @Provides
    open fun provideSwabergersService(sharedPreferences: SharedPreferences): SwabergersService {
        return SwabergersService(sharedPreferences)
    }



}