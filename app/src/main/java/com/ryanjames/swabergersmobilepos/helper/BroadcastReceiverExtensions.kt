package com.ryanjames.swabergersmobilepos.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables

fun Context.observeBroadcasts(action: String): Observable<Intent> {
    return observeBroadcasts(IntentFilter(action))
}

fun Context.observeBroadcasts(intentFilter: IntentFilter): Observable<Intent> {
    val observable = Observable.create<Intent> { observer ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                observer.onNext(intent)
            }
        }

        observer.setDisposable(Disposables.fromRunnable {
            unregisterReceiver(receiver)
        })

        registerReceiver(receiver, intentFilter)
    }

    return observable
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
}