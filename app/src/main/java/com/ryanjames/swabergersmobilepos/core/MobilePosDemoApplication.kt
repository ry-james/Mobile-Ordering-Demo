package com.ryanjames.swabergersmobilepos.core

import android.app.Application
import com.facebook.stetho.Stetho
import com.ryanjames.swabergersmobilepos.dagger.ApplicationComponent
import com.ryanjames.swabergersmobilepos.dagger.DaggerApplicationComponent

import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm


class MobilePosDemoApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        Realm.init(this)

        val realmInspector = RealmInspectorModulesProvider.builder(this)
            .build()


        Stetho.initialize(
            Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(realmInspector)
                .build()
        )
    }

    private fun initAppComponent() {
        appComponent = DaggerApplicationComponent.builder()
            .application(this)
            .build()
    }

    companion object {
        lateinit var appComponent: ApplicationComponent
    }


}