package com.ryanjames.swabergersmobilepos.helper

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.ryanjames.swabergersmobilepos.R

class DialogManager constructor(lifecycle: Lifecycle?, private var context: Context?) {

    private var loadingDialog: AlertDialog? = null

    init {
        val lifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                context = null
            }
        }
        lifecycle?.addObserver(lifecycleObserver)
    }

    fun showActivityFinishingDialog(message: String) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                dialog.dismiss()
                (context as? Activity)?.finish()
            }.show()
    }

    fun showLoadingDialog(message: String? = context?.getString(R.string.loading)) {
        val loadingView = LayoutInflater.from(context).inflate(R.layout.view_progress, null)
        val tvLoadingMessage = loadingView.findViewById(R.id.tvLoadingMessage) as TextView
        tvLoadingMessage.text = message

        loadingDialog = AlertDialog.Builder(context)
            .setView(loadingView)
            .setCancelable(false)
            .show()
    }

    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    fun showDismissableDialog(message: String) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(R.string.ok_cta) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

}