package com.ryanjames.swabergersmobilepos.helper

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*
import kotlin.collections.HashSet

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