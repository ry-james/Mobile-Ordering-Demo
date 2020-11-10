package com.ryanjames.swabergersmobilepos.feature.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
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

        if (loginManager.isLoggedIn()) {
            startActivity(BottomNavActivity.createIntent(this))
        } else {
            startActivity(LoginActivity.createIntent(this))
        }

        finish()
    }
}