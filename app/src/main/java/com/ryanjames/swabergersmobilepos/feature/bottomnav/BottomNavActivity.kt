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
import com.google.firebase.messaging.FirebaseMessaging
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.NotificationService
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityBottomNavBinding
import com.ryanjames.swabergersmobilepos.feature.home.HomeFragment
import com.ryanjames.swabergersmobilepos.feature.login.LoginActivity
import com.ryanjames.swabergersmobilepos.feature.menu.MenuFragment
import com.ryanjames.swabergersmobilepos.feature.old.bagsummary.BagSummaryFragment
import com.ryanjames.swabergersmobilepos.feature.orderdetails.OrderDetailsDialogFragment
import com.ryanjames.swabergersmobilepos.feature.orderhistory.OrderHistoryFragment
import com.ryanjames.swabergersmobilepos.helper.FcmHelper
import com.ryanjames.swabergersmobilepos.helper.LoginManager
import com.ryanjames.swabergersmobilepos.helper.subscribeToBroadcastsOnLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private const val EXTRA_ORDER_ID = "order_id"

class BottomNavActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, MenuFragment.MenuFragmentCallback, BagSummaryFragment.BagSummaryFragmentCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var loginManager: LoginManager

    private lateinit var binding: ActivityBottomNavBinding
    private var noInternetSnackbar: Snackbar? = null
    private var currentSelectItemId = R.id.navigation_menu

    private var savedStateSparseArray = SparseArray<Fragment.SavedState>()
    private val compositeDisposable = CompositeDisposable()

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
        subscribeToBroadcastsOnLifecycle(ConnectivityManager.CONNECTIVITY_ACTION, this::onConnectivityChange)

        compositeDisposable.add(
            loginManager.forceLogout.observeOn(Schedulers.io()).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { logOutEvent ->
                    logOutEvent.handleEvent {
                        finish()
                        startActivity(LoginActivity.createIntentForceLogout(this))
                    }
                })

        FirebaseMessaging.getInstance().subscribeToTopic(FcmHelper.TOPIC)

        handleViewOrderDeeplinkIntent(intent)
        handleNotification()
    }

    private fun handleNotification() {
        val notification = NotificationService.getNotificationFromIntent(intent)
        if (notification is NotificationService.NotificationType.ProductDetailNotification) {
            navigateToMenuTab(MenuFragment.getNotificationBundle(notification.productId))
        } else if (notification is NotificationService.NotificationType.OrderDetailNotification) {
            navigateToOrderTab()
            OrderDetailsDialogFragment.display(supportFragmentManager, notification.orderId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_menu -> swapFragments(item.itemId, "NAVIGATION_MENU")
            R.id.navigation_bag -> swapFragments(item.itemId, "NAVIGATION_BAG")
            R.id.navigation_order -> swapFragments(item.itemId, "NAVIGATION_ORDER")
            else -> return false
        }
        return true
    }

    private fun handleViewOrderDeeplinkIntent(intent: Intent?) {
        val orderId = intent?.getStringExtra(EXTRA_ORDER_ID)
        if (orderId != null) {
            navigateToOrderTab()
            OrderDetailsDialogFragment.display(supportFragmentManager, orderId)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleViewOrderDeeplinkIntent(intent)
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

    private fun navigateToMenuTab(bundle: Bundle? = null) {
        swapFragments(R.id.navigation_menu, "NAVIGATION_MENU", bundle)
        binding.bottomNav.selectedItemId = R.id.navigation_menu
    }

    private fun navigateToOrderTab(bundle: Bundle? = null) {
        swapFragments(R.id.navigation_order, "NAVIGATION_BAG", bundle)
        binding.bottomNav.selectedItemId = R.id.navigation_order
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

    private fun swapFragments(@IdRes actionId: Int, key: String, bundle: Bundle? = null) {
        // Check if the tab clicked is not the current tab
        if (supportFragmentManager.findFragmentByTag(key) == null) {
            saveFragmentState(actionId)
            createFragment(key, actionId, bundle)
        }
    }

    private fun saveFragmentState(actionId: Int) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_content_bottom_nav)
        if (currentFragment != null) {
            savedStateSparseArray.put(
                currentSelectItemId,
                supportFragmentManager.saveFragmentInstanceState(currentFragment)
            )
        }
        currentSelectItemId = actionId
    }

    private fun createFragment(key: String, actionId: Int, bundle: Bundle? = null) {

        val fragment = when (actionId) {
            R.id.navigation_menu -> HomeFragment()
            R.id.navigation_bag -> BagSummaryFragment()
            R.id.navigation_order -> OrderHistoryFragment()
            else -> null
        }

        bundle?.let { fragment?.arguments = it }

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

        fun createIntentToOrderSummary(context: Context, orderId: String): Intent {
            return createIntent(context).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }

        const val SAVED_STATE_CONTAINER_KEY = "ContainerKey"
        const val SAVED_STATE_CURRENT_TAB_KEY = "CurrentTabKey"

    }


}
