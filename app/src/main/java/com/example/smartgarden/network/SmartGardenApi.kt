package com.example.smartgarden.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

interface SmartGardenApi {

    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("telemetry/")
    suspend fun getTelemetry(): TelemetryResponse
}

// ==========================================
// Data Classes para mapear JSON con Gson
// ==========================================

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("access") val access: String? = null,
    @SerializedName("refresh") val refresh: String? = null
)

// Reemplaza los campos según la estructura exacta de tu JSON en Django
data class TelemetryResponse(
    val status: String?,
    val data: Any?
)
