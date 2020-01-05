package com.ryanjames.swabergersmobilepos.network.responses

import retrofit2.Response
import java.io.IOException

sealed class ApiResponse<T> {

    fun create(error: Throwable): ApiResponse<T> {
        val errorMessage = if (!error.message.isNullOrEmpty()) {
            error.message
        } else {
            "Unknown Error"
        }
        return ApiErrorResponse(errorMessage)
    }

    fun create(response: Response<T>): ApiResponse<T> {
        if (response.isSuccessful) {
            val body = response.body()

            if (body == null || response.code() == 204) {
                return ApiEmptyResponse()
            } else {
                return ApiSuccessResponse(body)
            }
        } else {
            var errorMessage = ""
            try {
                errorMessage = response.errorBody().toString()
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                errorMessage = response.message()
            }
            return ApiErrorResponse(errorMessage)
        }


    }

    class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

    class ApiErrorResponse<T>(val errorMessage: String?) : ApiResponse<T>()

    class ApiEmptyResponse<T>() : ApiResponse<T>()
}