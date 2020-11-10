package com.ryanjames.swabergersmobilepos.feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ryanjames.swabergersmobilepos.helper.LoginManager
import com.ryanjames.swabergersmobilepos.helper.isBlankOrEmpty
import com.ryanjames.swabergersmobilepos.network.retrofit.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    val apiService: ApiService,
    val loginManager: LoginManager
) : ViewModel() {

    val username = MutableLiveData<String>().apply { value = "" }
    val password = MutableLiveData<String>().apply { value = "" }
    private val compositeDisposable = CompositeDisposable()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean>
        get() = _loginSuccess

    private val _loginFailure = MutableLiveData<LoginError>()
    val loginFailure: LiveData<LoginError>
        get() = _loginFailure

    fun login() {
        if (isValidated()) {
            compositeDisposable.add(
                apiService.authenticate(username.value ?: "", password.value ?: "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ _loginSuccess.value = true },
                        { error ->
                            error.printStackTrace()
                            _loginFailure.value = LoginError.LOGIN_ERROR
                        })
            )
        }
    }

    fun isLoggedIn(): Boolean {
        return loginManager.isLoggedIn()
    }

    private fun isValidated(): Boolean {
        val isUsernameBlank = username.value?.isBlankOrEmpty() == true
        val isPasswordBlank = password.value?.isBlankOrEmpty() == true

        if (isUsernameBlank && isPasswordBlank) {
            _loginFailure.value = LoginError.USERNAME_AND_PASSWORD_ARE_EMPTY
            return false
        }

        if (isUsernameBlank) {
            _loginFailure.value = LoginError.USERNAME_IS_EMPTY
            return false
        }

        if (isPasswordBlank) {
            _loginFailure.value = LoginError.PASSWORD_IS_EMPTY
            return false
        }

        return true
    }

    enum class LoginError {
        LOGIN_ERROR,
        USERNAME_IS_EMPTY,
        PASSWORD_IS_EMPTY,
        USERNAME_AND_PASSWORD_ARE_EMPTY
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}