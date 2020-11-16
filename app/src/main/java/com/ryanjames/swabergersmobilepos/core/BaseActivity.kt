package com.ryanjames.swabergersmobilepos.core

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ryanjames.swabergersmobilepos.R
import com.ryanjames.swabergersmobilepos.helper.DialogManager
import com.ryanjames.swabergersmobilepos.helper.getLoggerTag

open class BaseActivity : AppCompatActivity() {

    protected val dialogManager by lazy { DialogManager(lifecycle, this) }

    protected inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
        }

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

    protected open fun onUpPressed() {
        finish()
    }
}