package com.ryanjames.swabergersmobilepos.feature.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.core.BaseActivity
import com.ryanjames.swabergersmobilepos.core.MobilePosDemoApplication
import com.ryanjames.swabergersmobilepos.core.ViewModelFactory
import com.ryanjames.swabergersmobilepos.databinding.ActivityLoginBinding
import com.ryanjames.swabergersmobilepos.feature.bottomnav.BottomNavActivity
import com.ryanjames.swabergersmobilepos.helper.setOnSingleClickListener
import javax.inject.Inject

private const val EXTRA_FORCE_LOG_OUT = "extra.force.log.out"

class LoginActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobilePosDemoApplication.appComponent.inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        subscribe()

        if (intent.getBooleanExtra(EXTRA_FORCE_LOG_OUT, false)) {
            dialogManager.showDismissableDialog("You're automatically logged out. Please re-enter your credentials")
        } else {
            finishIfLoggedIn()
        }

    }

    private fun finishIfLoggedIn() {
        if (viewModel.isLoggedIn()) {
            startActivity(BottomNavActivity.createIntent(this))
            finish()
        }
    }

    private fun subscribe() {
        viewModel.loginSuccess.observe(this, Observer {
            dialogManager.hideLoadingDialog()
            startActivity(BottomNavActivity.createIntent(this))
            finish()
        })

        viewModel.loginFailure.observe(this, Observer { error ->

            dialogManager.hideLoadingDialog()

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
            dialogManager.showLoadingDialog(getString(R.string.signing_in))
            viewModel.login()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }

        fun createIntentForceLogout(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(EXTRA_FORCE_LOG_OUT, true)
            return intent
        }
    }
}
