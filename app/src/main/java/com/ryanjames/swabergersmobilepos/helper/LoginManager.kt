package com.ryanjames.swabergersmobilepos.helper

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.PublishRelay
import javax.inject.Inject

class LoginManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    val forceLogout = PublishRelay.create<Event<Boolean>>()

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString(SharedPrefsKeys.KEY_AUTH_TOKEN, null) != null
    }

    fun logOut() {
        forceLogout.accept(Event(true))
        sharedPreferences.edit()
            .remove(SharedPrefsKeys.KEY_AUTH_TOKEN)
            .remove(SharedPrefsKeys.KEY_REFRESH_TOKEN)
            .apply()

    }

}