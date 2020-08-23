package com.ryanjames.swabergersmobilepos.feature.login

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.SwabergersApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityLoginBinding
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.helper.setOnSingleClickListener
import javax.inject.Inject


class LoginActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SwabergersApplication.appComponent.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        subscribe()
    }

    private fun subscribe() {
        viewModel.loginSuccess.observe(this, Observer {
            hideLoadingDialog()
            startActivity(BottomNavActivity.createIntent(this))
            finish()
        })

        viewModel.loginFailure.observe(this, Observer { error ->

            hideLoadingDialog()

            val errorMessage = when (error) {
                LoginViewModel.LoginError.LOGIN_ERROR -> getString(R.string.log_in_failed)
                LoginViewModel.LoginError.PASSWORD_IS_EMPTY -> getString(R.string.password_empty)
                LoginViewModel.LoginError.USERNAME_IS_EMPTY -> getString(R.string.username_empty)
                LoginViewModel.LoginError.USERNAME_AND_PASSWORD_ARE_EMPTY -> getString(R.string.username_password_empty)
                else -> getString(R.string.log_in_failed)
            }

            AlertDialog.Builder(this)
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.ok_cta) { dialog, _ -> dialog.dismiss() }
                .show()
        })

        binding.button.setOnSingleClickListener(this) {
            showLoadingDialog(getString(R.string.signing_in))
            viewModel.login()
        }
    }
}
