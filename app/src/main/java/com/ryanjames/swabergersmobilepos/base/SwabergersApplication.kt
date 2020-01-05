package com.ryanjames.swabergersmobilepos.base

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.ryanjames.swabergersmobilepos.dagger.ApplicationComponent
import com.ryanjames.swabergersmobilepos.dagger.ApplicationModule
import com.uphyca.stetho_realm.RealmInspectorModulesProvider
import io.realm.Realm


class SwabergersApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
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
}