package com.ryanjames.swabergersmobilepos.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.jakewharton.rxbinding.view.RxView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

fun Float.toTwoDigitString(): String {
    return String.format(Locale.US, "%.2f", this)
}

inline fun <VM : ViewModel> Any.viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
    }


fun <T> MutableLiveData<HashSet<T>>.add(value: T) {
    val list = this.value ?: hashSetOf()
    list.add(value)
    this.value = list
}

fun <T> MutableLiveData<HashSet<T>>.remove(value: T) {
    val list = this.value ?: hashSetOf()
    list.remove(value)
    this.value = list
}

fun <T> MutableLiveData<HashSet<T>>.value(): HashSet<T> {
    return this.value ?: hashSetOf()
}

fun <T> MutableLiveData<HashSet<T>>.size(): Int {
    val list = this.value ?: hashSetOf()
    return list.size
}

fun <T> MutableList<T>.clearAndAddAll(elements: Collection<T>) {
    clear()
    addAll(elements)
}

fun Any.getLoggerTag(): String {
    return this::class.java.simpleName
}

fun Date.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun <T, K> HashMap<T, List<K>>.deepEquals(mapToCompare: HashMap<T, List<K>>): Boolean {
    if (this.entries.size != mapToCompare.entries.size) {
        return false
    }

    for ((key, value) in this) {
        if (value != mapToCompare[key]) {
            return false
        }
    }

    return this.keys == mapToCompare.keys
}

fun String.isBlankOrEmpty(): Boolean {
    return isBlank() || isEmpty()
}

fun String.trimAllWhitespace(): String {
    return this.trim().replace("\\s+".toRegex(), " ")
}

fun View.setOnSingleClickListener(activity: FragmentActivity?, onClick: (View) -> Unit, throttle: Long) {

    val lifecycleObserver: LifecycleObserver = object : LifecycleObserver {
        val subscription = RxView.clicks(this@setOnSingleClickListener).throttleFirst(throttle, TimeUnit.MILLISECONDS).subscribe {
            onClick(this@setOnSingleClickListener)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            subscription?.unsubscribe()
        }

    }
    activity?.lifecycle?.addObserver(lifecycleObserver)
}

fun View.setOnSingleClickListener(activity: FragmentActivity?, onClick: (View) -> Unit) {
    setOnSingleClickListener(activity, onClick, 2000)
}

fun Disposable.disposedBy(bag: CompositeDisposable) {
    bag.add(this)
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}

fun Context.bitmapDescriptorFromVector(vectorResId: Int, width: Int = -1, height: Int = -1): BitmapDescriptor? {
    return ContextCompat.getDrawable(this, vectorResId)?.run {
        val w = if (width > 0) width else intrinsicWidth
        val h = if (height > 0) height else intrinsicHeight
        setBounds(0, 0, w, h)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
