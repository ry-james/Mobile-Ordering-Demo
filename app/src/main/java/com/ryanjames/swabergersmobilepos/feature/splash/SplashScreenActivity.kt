package com.ryanjames.swabergersmobilepos.feature.splash

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.NotificationService
import com.ryanjames.swabergersmobilepos.core.NotificationService.Companion.EXTRA_NOTIFICATION_TYPE
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity2
import com.ryanjames.swabergersmobilepos.feature.login.LoginActivity
import com.ryanjames.swabergersmobilepos.helper.LoginManager
import javax.inject.Inject


class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var loginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)
        setContentView(R.layout.activity_splash_screen)

        Log.d("SPLASH", "SplashActivity")

        val notification = NotificationService.getNotificationFromIntent(intent)
        if (notification != null) {
            startActivity(BottomNavActivity.createIntent(this).apply {
                putExtra(EXTRA_NOTIFICATION_TYPE, notification)
            })
        } else {
            if (loginManager.isLoggedIn()) {
                startActivity(BottomNavActivity2.createIntent(this))
            } else {
                startActivity(LoginActivity.createIntent(this))
            }
        }
        finish()


    }
}