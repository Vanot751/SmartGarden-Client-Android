package com.example.smartgarden.network.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
)

data class TokenResponse(
    @SerializedName("access") val access: String,
    @SerializedName("refresh") val refresh: String,
)

data class RefreshTokenRequest(
    @SerializedName("refresh") val refresh: String,
)

data class RefreshTokenResponse(
    @SerializedName("access") val access: String,
)
