package com.ryanjames.swabergersmobilepos.feature.bottomnav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityBottomNavBinding
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryFragment
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragment
import javax.inject.Inject

class BottomNavActivity : BaseActivity(), MenuFragment.MenuFragmentCallback, BagSummaryFragment.BagSummaryFragmentCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityBottomNavBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_nav)
        binding.lifecycleOwner = this

        val navController = findNavController(R.id.nav_host_fragment)


        // Set bottom margin of fragment to be above the bottom nav
        binding.bottomNav.post {
            val fragmentView = findViewById<View>(R.id.nav_host_fragment)
            fragmentView.layoutParams = (fragmentView.layoutParams as CoordinatorLayout.LayoutParams).apply {
                setMargins(leftMargin, topMargin, rightMargin, binding.bottomNav.measuredHeight)
            }
        }


        binding.bottomNav.setupWithNavController(navController)
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_SHORT).apply {
            anchorView = binding.bottomNav
            show()
        }
    }

    private fun showItemAddedSnackbar() {
        showSnackbar("Item is added successfully.")
    }

    private fun showItemUpdatedSnackbar() {
        showSnackbar("Item is updated successfully.")
    }

    override fun onUpdateLineItem() {
        showItemUpdatedSnackbar()
    }

    override fun onAddLineItem() {
        showItemAddedSnackbar()
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, BottomNavActivity::class.java)
        }

    }

}
