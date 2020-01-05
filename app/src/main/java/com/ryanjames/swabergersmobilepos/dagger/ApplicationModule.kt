package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(private val application: Application) {

    @Provides
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    fun provideSharedPrefs(): SharedPreferences {
        return application.getSharedPreferences("Pref", Context.MODE_PRIVATE)
    }
}