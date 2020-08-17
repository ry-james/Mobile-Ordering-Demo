package com.ryanjames.swabergersmobilepos.feature.bottomnav

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityBottomNavBinding
import com.ryanjames.swabergersmobilepos.feature.bagsummary.BagSummaryFragment
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragment
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryFragment
import com.ryanjames.swabergersmobilepos.helper.observeBroadcasts
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class BottomNavActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, MenuFragment.MenuFragmentCallback, BagSummaryFragment.BagSummaryFragmentCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityBottomNavBinding
    private var internetConnectivitySubscription: Disposable? = null
    private var noInternetSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_nav)
        binding.lifecycleOwner = this


        // Set bottom margin of fragment to be above the bottom nav
        binding.bottomNav.post {
            val fragmentView = findViewById<View>(R.id.fragment_content_bottom_nav)
            fragmentView.layoutParams = (fragmentView.layoutParams as CoordinatorLayout.LayoutParams).apply {
                setMargins(leftMargin, topMargin, rightMargin, binding.bottomNav.measuredHeight)
            }
        }
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
        navigateToMenuTab()
        internetConnectivitySubscription = observeBroadcasts(ConnectivityManager.CONNECTIVITY_ACTION)
            .subscribe(this::onConnectivityChange)

    }

    private fun onConnectivityChange(intent: Intent) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectivityManager.activeNetworkInfo?.isConnected ?: false
        Log.i("MainActivity", "isConnected = $isConnected")
        if (isConnected) {
            hideNoInternetSnackbar()
        } else {
            showNoInternetSnackbar()
        }
    }

    private fun showNoInternetSnackbar() {
        noInternetSnackbar = showSnackbar("No internet connection", Snackbar.LENGTH_INDEFINITE)
    }

    private fun hideNoInternetSnackbar() {
        noInternetSnackbar?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        internetConnectivitySubscription?.dispose()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val selectedFragment = when (item.itemId) {
            R.id.navigation_menu -> MenuFragment()
            R.id.navigation_bag -> BagSummaryFragment()
            R.id.navigation_order -> OrderHistoryFragment()
            else -> return false
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_content_bottom_nav, selectedFragment, selectedFragment::class.qualifiedName)
            .commit()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag(MenuFragment::class.qualifiedName)?.isVisible == true) {
            finish()
        } else {
            navigateToMenuTab()
        }
    }

    private fun navigateToMenuTab() {
        binding.bottomNav.selectedItemId = R.id.navigation_menu
    }

    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT): Snackbar {
        return Snackbar.make(binding.coordinatorLayout, message, duration).apply {
            anchorView = binding.bottomNav
        }
    }

    override fun onUpdateLineItem() {
        showSnackbar(getString(R.string.item_updated_snackbar))
    }

    override fun onAddLineItem() {
        showSnackbar(getString(R.string.item_added_snackbar))
    }

    override fun onRemoveLineItem() {
        showSnackbar(getString(R.string.item_removed_snackbar))
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, BottomNavActivity::class.java)
        }

    }


}
