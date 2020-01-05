package com.ryanjames.swabergersmobilepos.network

object LoginManager {

    private var session: Session? = null
    private var accessToken: String? = null

    fun getSession(): Session {
        if (session == null) {
            session = object : Session {
                override fun logIn(username: String, password: String) {
//                    LoginService.login(username, password).subscribe { loginResponse ->
//                        accessToken = loginResponse.accessToken

//                    }
                }

                override fun isLoggedIn(): Boolean {

                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun saveAccessToken(token: String) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getAccessToken(): String? {
                    return accessToken
                }

                override fun invalidate() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            }
        }
        return session as Session
    }


    interface AuthenticationListener {
        fun onUserLoggedOut()
    }
}