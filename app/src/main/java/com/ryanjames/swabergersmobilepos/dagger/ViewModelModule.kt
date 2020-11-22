package com.ryanjames.swabergersmobilepos.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryViewModel
import com.ryanjames.swabergersmobilepos.feature.checkout.CheckoutViewModel
import com.ryanjames.swabergersmobilepos.feature.login.LoginViewModel
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragmentViewModel
import com.ryanjames.swabergersmobilepos.feature.menuitemdetail.MenuItemDetailViewModel
import com.ryanjames.swabergersmobilepos.feature.orderdetails.OrderDetailsViewModel
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryViewModel
import com.ryanjames.swabergersmobilepos.viewmodels.MainActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(BagSummaryViewModel::class)
    internal abstract fun bindBagSummaryViewModel(viewModel: BagSummaryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MenuFragmentViewModel::class)
    internal abstract fun bindMenuActivityViewModel(viewModel: MenuFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    internal abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrderHistoryViewModel::class)
    internal abstract fun bindOrderHistoryViewModel(viewModel: OrderHistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MenuItemDetailViewModel::class)
    internal abstract fun bindMenuItemDetailViewModel(viewModel: MenuItemDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrderDetailsViewModel::class)
    internal abstract fun bindOrderDetailsViewModel(viewModel: OrderDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckoutViewModel::class)
    internal abstract fun bindCheckoutViewModel(viewModel: CheckoutViewModel): ViewModel

    //Add more ViewModels here
}