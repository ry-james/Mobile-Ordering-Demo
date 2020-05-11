package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import com.ryanjames.swabergersmobilepos.activity.MainActivity
import com.ryanjames.swabergersmobilepos.activity.MenuActivity
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryActivity
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    fun inject(menuActivity: MenuActivity)

    fun inject(mainActivity: MainActivity)

    fun inject(bagSummaryActivity: BagSummaryActivity)

    fun inject(menuItemDetailActivity: MenuItemDetailActivity)
}