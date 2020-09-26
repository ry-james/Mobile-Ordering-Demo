package com.ryanjames.swabergersmobilepos.feature.bottomnav

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
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
    private var currentSelectItemId = R.id.navigation_menu

    private var savedStateSparseArray = SparseArray<Fragment.SavedState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_nav)
        binding.lifecycleOwner = this

        if (savedInstanceState != null) {
            savedStateSparseArray = savedInstanceState.getSparseParcelableArray(SAVED_STATE_CONTAINER_KEY)
                ?: savedStateSparseArray
            currentSelectItemId = savedInstanceState.getInt(SAVED_STATE_CURRENT_TAB_KEY)
        }

        // Set bottom margin of fragment to be above the bottom nav
        binding.bottomNav.post {
            val fragmentView = findViewById<View>(R.id.fragment_content_bottom_nav)
            fragmentView.layoutParams = (fragmentView.layoutParams as CoordinatorLayout.LayoutParams).apply {
                setMargins(leftMargin, topMargin, rightMargin, binding.bottomNav.measuredHeight)
            }
        }
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
        binding.bottomNav.selectedItemId = currentSelectItemId

        // Subscribe to Internet connectivity broadcast
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
        when (item.itemId) {
            R.id.navigation_menu -> swapFragments(item.itemId, "NAVIGATION_MENU")
            R.id.navigation_bag -> swapFragments(item.itemId, "NAVIGATION_BAG")
            R.id.navigation_order -> swapFragments(item.itemId, "NAVIGATION_ORDER")
            else -> return false
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putSparseParcelableArray(SAVED_STATE_CONTAINER_KEY, savedStateSparseArray)
        outState?.putInt(SAVED_STATE_CURRENT_TAB_KEY, currentSelectItemId)
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
            show()
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

    private fun swapFragments(@IdRes actionId: Int, key: String) {
        // Check if the tab clicked is not the current tab
        if (supportFragmentManager.findFragmentByTag(key) == null) {
            savedFragmentState(actionId)
            createFragment(key, actionId)
        }
    }

    private fun savedFragmentState(actionId: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_content_bottom_nav)
        if (currentFragment != null) {
            savedStateSparseArray.put(
                currentSelectItemId,
                supportFragmentManager.saveFragmentInstanceState(currentFragment)
            )
        }
        currentSelectItemId = actionId
    }

    private fun createFragment(key: String, actionId: Int) {

        val fragment = when (actionId) {
            R.id.navigation_menu -> MenuFragment()
            R.id.navigation_bag -> BagSummaryFragment()
            R.id.navigation_order -> OrderHistoryFragment()
            else -> null
        }

        fragment?.let {
            it.setInitialSavedState(savedStateSparseArray[actionId])
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_content_bottom_nav, fragment, key)
                .commit()
        }
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, BottomNavActivity::class.java)
        }

        const val SAVED_STATE_CONTAINER_KEY = "ContainerKey"
        const val SAVED_STATE_CURRENT_TAB_KEY = "CurrentTabKey"

    }


}
