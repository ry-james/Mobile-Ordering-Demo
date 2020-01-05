package com.ryanjames.swabergersmobilepos.network

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.ryanjames.swabergersmobilepos.helper.AppExecutors
import com.ryanjames.swabergersmobilepos.network.responses.ApiResponse

// CacheObject: Type for the Resource data.
// RequestObject: Type for the API response.
abstract class NetworkBoundResource<CacheObject, RequestObject>(appExecutor: AppExecutors) {

    private val results: MediatorLiveData<Resource<CacheObject>> = MediatorLiveData()

    init {
        // Update LiveData for loading status
        results.value = Resource.Loading()

        // Observe LiveData from local database
        val dbSource = loadFromDb()

        results.addSource(dbSource) { cacheObject ->
            results.removeSource(dbSource)
            if (shouldFetch(cacheObject)) {
                // get data from network
            } else {
                results.addSource(dbSource) { cacheObject ->
                    setValue(Resource.Success(cacheObject))
                }
            }
        }
    }

    private fun setValue(newValue: Resource<CacheObject>) {
        if (results.value != newValue) {
            results.value = newValue
        }
    }

    // Called to save the result of the API response into the database
    @WorkerThread
    protected abstract fun saveCallResult(item: RequestObject)

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract fun shouldFetch(data: CacheObject?): Boolean

    // Called to get the cached data from the database.
    @MainThread
    protected abstract fun loadFromDb(): LiveData<CacheObject>

    // Called to create the API call.
    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestObject>>

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    fun asLiveData(): LiveData<CacheObject> = TODO()
}
