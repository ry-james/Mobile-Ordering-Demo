package com.ryanjames.swabergersmobilepos.dagger

import com.ryanjames.swabergersmobilepos.base.SwabergersApplication
import com.ryanjames.swabergersmobilepos.repository.MenuRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(application: SwabergersApplication)

    fun inject(menuRepository: MenuRepository)
}