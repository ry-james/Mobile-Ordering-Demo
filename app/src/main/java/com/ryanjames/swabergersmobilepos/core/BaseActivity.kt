package com.ryanjames.swabergersmobilepos.core

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.helper.getLoggerTag
import java.net.SocketTimeoutException

open class BaseActivity : AppCompatActivity() {

    protected inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
        }

    protected fun handleError(error: Throwable) {
        if (error is SocketTimeoutException) {
            AlertDialog.Builder(this)
                .setMessage("The network call timed out. Please try again.")
                .setPositiveButton(R.string.ok_cta) { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val btnUp = findViewById<ImageView?>(R.id.ivToolbarUp)
        btnUp?.setOnClickListener {
            onUpPressed()
        }
    }

    protected fun setToolbarTitle(title: String) {
        val tvToolbar = findViewById<TextView?>(R.id.tvToolbarText)
        if (tvToolbar == null) {
            Log.w(getLoggerTag(), "Can't set the toolbar title. Toolbar layout is not included in your layout.")
            return
        }
        tvToolbar.text = title
    }

    protected open fun onUpPressed() {
        finish()
    }
}