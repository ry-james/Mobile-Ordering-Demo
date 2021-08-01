package com.ryanjames.swabergersmobilepos.domain

import com.ryanjames.swabergersmobilepos.helper.Event

sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>() {
        val event = Event(data)
    }

    data class Error(val exception: Exception) : Resource<Nothing>() {

        constructor(throwable: Throwable) : this(exception = Exception(throwable))

        val event = Event(exception)

    }

    object InProgress : Resource<Nothing>()

    inline fun takeIfSuccess(func: (T) -> Unit): T? {
        if (this is Success) {
            func.invoke(this.data)
            return this.data
        }
        return null
    }

    inline fun <K> mapIfSuccess(func: (T) -> K): K? {
        if (this is Success) {
            return func.invoke(this.data)
        }
        return null
    }
}