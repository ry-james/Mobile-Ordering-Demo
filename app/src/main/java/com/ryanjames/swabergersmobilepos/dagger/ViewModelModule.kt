package com.ryanjames.swabergersmobilepos.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryViewModel
import com.ryanjames.swabergersmobilepos.viewmodels.MainActivityViewModel
import com.ryanjames.swabergersmobilepos.viewmodels.MenuActivityViewModel
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
    @ViewModelKey(MenuActivityViewModel::class)
    internal abstract fun bindMenuActivityViewModel(viewModel: MenuActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    internal abstract fun bindMainActivityViewModel(viewModel: MainActivityViewModel): ViewModel

    //Add more ViewModels here
}