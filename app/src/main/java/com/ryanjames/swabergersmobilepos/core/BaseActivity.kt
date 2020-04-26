package com.ryanjames.swabergersmobilepos.core

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
                .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface, p1: Int) {
                        dialogInterface.dismiss()
                    }
                })
                .show()
        }
    }

}