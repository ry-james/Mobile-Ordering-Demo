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
}