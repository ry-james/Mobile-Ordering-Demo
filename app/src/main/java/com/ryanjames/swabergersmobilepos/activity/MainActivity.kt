package com.ryanjames.swabergersmobilepos.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.network.responses.LoginResponse
import com.ryanjames.swabergersmobilepos.repository.OrderRepository
import com.ryanjames.swabergersmobilepos.viewmodels.MainActivityViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var orderRepository: OrderRepository

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SwabergersApplication.appComponent.inject(this)

        mainActivityViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainActivityViewModel::class.java)
    }

    fun onClickAuthButton(view: View) {
        mainActivityViewModel.authenticate("testcedar", "james")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ loginResponse: LoginResponse? ->
                Toast.makeText(this, loginResponse?.accessToken, Toast.LENGTH_SHORT).show()
            }, {

            }).addToCompositeDisposable()

    }


    fun onClickLaunchMenu(view: View) {
        val intent = MenuActivity.createIntent(this)
        startActivity(intent)
    }

    fun onClickClearBag(view: View) {
        orderRepository.clearLocalBag()
    }

    private fun Disposable.addToCompositeDisposable() {
        compositeDisposable.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
