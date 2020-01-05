package com.ryanjames.swabergersmobilepos.network

// A generic class that contains data and status about loading this data.
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    // Successful API call
    class Success<T>(data: T) : Resource<T>(data)

    // Loading - reading and accessing data from cache
    class Loading<T>(data: T? = null) : Resource<T>(data)

    // Error from API
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}