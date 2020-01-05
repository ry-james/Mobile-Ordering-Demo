package com.ryanjames.swabergersmobilepos.network

interface Session {

    fun isLoggedIn(): Boolean

    fun saveAccessToken(token: String)

    fun getAccessToken(): String?

    fun logIn(username: String, password: String)

    fun invalidate()

}