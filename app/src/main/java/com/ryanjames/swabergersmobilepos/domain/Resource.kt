package com.ryanjames.swabergersmobilepos.domain

import com.ryanjames.swabergersmobilepos.helper.Event

sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: Event<T>) : Resource<T>()
    data class Error(val exception: Event<Exception>) : Resource<Nothing>()
    object InProgress : Resource<Nothing>()
}