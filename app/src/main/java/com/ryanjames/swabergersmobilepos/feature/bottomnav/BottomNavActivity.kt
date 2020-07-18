package com.ryanjames.swabergersmobilepos.feature.bottomnav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.viewmodels.MenuFragmentViewModel
import javax.inject.Inject

class BottomNavActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var menuViewModel: MenuFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        setContentView(R.layout.activity_bottom_nav)
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        menuViewModel = ViewModelProviders.of(this, viewModelFactory).get(MenuFragmentViewModel::class.java)
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, BottomNavActivity::class.java)
        }

    }
}
