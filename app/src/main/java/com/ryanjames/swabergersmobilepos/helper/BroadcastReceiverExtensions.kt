package com.ryanjames.swabergersmobilepos.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables

fun AppCompatActivity.subscribeToBroadcastsOnLifecycle(
    action: String, fn: (Intent) -> Unit,
    subscribeOn: Lifecycle.Event = Lifecycle.Event.ON_CREATE,
    unsubscribeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) {
    observeBroadcasts(action).subscribeOnLifecycle(lifecycle, fn, subscribeOn, unsubscribeOn)
}

private fun <T> Observable<T>.subscribeOnLifecycle(
    lifecycle: Lifecycle,
    fn: (T) -> Unit,
    subscribeOn: Lifecycle.Event,
    unsubscribeOn: Lifecycle.Event
) {
    val lifecycleObserver = object : LifecycleObserver {
        private var subscription: Disposable? = null

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            if (subscribeOn == Lifecycle.Event.ON_CREATE) {
                subscription = subscribe(fn)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            if (subscribeOn == Lifecycle.Event.ON_START) {
                subscription = subscribe(fn)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            if (unsubscribeOn == Lifecycle.Event.ON_DESTROY) {
                subscription?.dispose()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            if (unsubscribeOn == Lifecycle.Event.ON_STOP) {
                subscription?.dispose()
            }
        }

    }

    lifecycle.addObserver(lifecycleObserver)
}

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