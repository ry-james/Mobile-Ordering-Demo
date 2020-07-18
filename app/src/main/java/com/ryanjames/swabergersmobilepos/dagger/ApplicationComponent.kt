package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import com.ryanjames.swabergersmobilepos.activity.MainActivity
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryFragment
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.feature.login.LoginActivity
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragment
import com.ryanjames.swabergersmobilepos.feature.menu.MenuPagerFragment
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, NetworkModule::class, RealmModule::class])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }

    fun inject(mainActivity: MainActivity)

    fun inject(bagSummaryFragment: BagSummaryFragment)

    fun inject(menuItemDetailActivity: MenuItemDetailActivity)

    fun inject(loginActivity: LoginActivity)

    fun inject(menuFragment: MenuFragment)

    fun inject(menuPagerFragment: MenuPagerFragment)

    fun inject(bottomNavActivity: BottomNavActivity)
}