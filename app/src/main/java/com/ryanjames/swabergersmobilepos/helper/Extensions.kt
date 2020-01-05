package com.ryanjames.swabergersmobilepos.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*

fun Float.toTwoDigitString(): String {
    return String.format(Locale.US, "%.2f", this)
}

inline fun <VM : ViewModel> Any.viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
    }