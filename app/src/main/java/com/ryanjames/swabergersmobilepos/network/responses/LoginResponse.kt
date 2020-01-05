package com.ryanjames.swabergersmobilepos.network.responses

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class LoginRequestBody(
    val username: String,
    val password: String
)

data class RefreshTokenResponse(@SerializedName("access_token") val accessToken: String)