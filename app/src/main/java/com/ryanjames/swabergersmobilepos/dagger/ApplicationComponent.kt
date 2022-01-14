package com.ryanjames.swabergersmobilepos.dagger

import android.app.Application
import com.ryanjames.swabergersmobilepos.activity.MainActivity
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryActivity
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity2
import com.ryanjames.swabergersmobilepos.feature.checkout.CheckoutFragment
import com.ryanjames.swabergersmobilepos.feature.home.HomeFragment
import com.ryanjames.swabergersmobilepos.feature.login.LoginActivity
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragment
import com.ryanjames.swabergersmobilepos.feature.menu.MenuPagerFragment
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailActivity
import com.ryanjames.swabergersmobilepos.feature.old.bagsummary.BagSummaryFragment
import com.ryanjames.swabergersmobilepos.feature.orderdetails.OrderDetailsDialogFragment
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryFragment
import com.ryanjames.swabergersmobilepos.feature.splash.SplashScreenActivity
import com.ryanjames.swabergersmobilepos.feature.venuedetail.VenueDetailActivity
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemdetail.MenuItemDetailFragment
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.menuitemmodifier.MenuItemModifierFragment
import com.ryanjames.swabergersmobilepos.feature.venuedetail.menuitemdetailsheet.parent.MenuItemBottomSheetFragment
import com.ryanjames.swabergersmobilepos.feature.venuefinder.VenueFinderActivity
import com.ryanjames.swabergersmobilepos.fragments.DeliveryAddressBottomSheetFragment
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

    fun inject(orderHistoryFragment: OrderHistoryFragment)

    fun inject(orderDetailsDialogFragment: OrderDetailsDialogFragment)

    fun inject(checkoutFragment: CheckoutFragment)

    fun inject(splashScreenActivity: SplashScreenActivity)

    fun inject(venueFinderActivity: VenueFinderActivity)

    fun inject(homeFragment: HomeFragment)

    fun inject(bottomNavActivity2: BottomNavActivity2)

    fun inject(venueDetailActivity: VenueDetailActivity)

    fun inject(menuItemDetailFragment: MenuItemDetailFragment)

    fun inject(menuItemModifierFragment: MenuItemModifierFragment)

    fun inject(menuItemBottomSheetFragment: MenuItemBottomSheetFragment)

    fun inject(bagSummaryActivity: BagSummaryActivity)

    fun inject(deliveryAddressBottomSheetFragment: DeliveryAddressBottomSheetFragment)
}