package com.ryanjames.swabergersmobilepos.core

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.helper.getLoggerTag

open class BaseActivity : AppCompatActivity() {

    protected inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
        }

    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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

    fun showLoadingDialog(message: String = getString(R.string.loading)) {
        val loadingView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
        val tvLoadingMessage = loadingView.findViewById(R.id.tvLoadingMessage) as TextView
        tvLoadingMessage.text = message

        loadingDialog = AlertDialog.Builder(this)
            .setView(loadingView)
            .setCancelable(false)
            .show()
    }

    fun showActivityFinishingDialog(message: String) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                dialog.dismiss()
                finish()
            }.show()
    }

    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    protected open fun onUpPressed() {
        finish()
    }
}