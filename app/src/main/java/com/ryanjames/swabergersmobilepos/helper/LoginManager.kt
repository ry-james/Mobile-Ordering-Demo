package com.ryanjames.swabergersmobilepos.helper

import android.content.SharedPreferences
import javax.inject.Inject

class LoginManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString(SharedPrefsKeys.KEY_AUTH_TOKEN, null) != null
    }

}