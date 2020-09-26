package com.ryanjames.swabergersmobilepos.domain

import com.ryanjames.swabergersmobilepos.helper.Event

sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>() {
        val event = Event(data)
    }

    data class Error(val exception: Event<Exception>) : Resource<Nothing>()
    object InProgress : Resource<Nothing>()
}