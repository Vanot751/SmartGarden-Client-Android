package com.example.smartgarden.network

import com.example.smartgarden.network.models.CreateModuleRequest
import com.example.smartgarden.network.models.IoTModuleDto
import com.example.smartgarden.network.models.LoginRequest
import com.example.smartgarden.network.models.PlantSearchResponse
import com.example.smartgarden.network.models.RefreshTokenRequest
import com.example.smartgarden.network.models.RefreshTokenResponse
import com.example.smartgarden.network.models.TelemetryDto
import com.example.smartgarden.network.models.TelemetryPageResponse
import com.example.smartgarden.network.models.TokenResponse
import com.example.smartgarden.network.models.UserPlantDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DjangoApiService {

    // Autenticación JWT
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    // Módulos IoT
    @GET("modules/")
    suspend fun getModules(): Response<List<IoTModuleDto>>

    @POST("modules/")
    suspend fun createModule(@Body request: CreateModuleRequest): Response<IoTModuleDto>

    @GET("modules/{id}/")
    suspend fun getModuleDetails(@Path("id") id: Int): Response<IoTModuleDto>

    // Plantas del Usuario
    @GET("user-plants/")
    suspend fun getUserPlants(): Response<List<UserPlantDto>>

    @POST("user-plants/")
    suspend fun createUserPlant(@Body request: UserPlantDto): Response<UserPlantDto>

    @DELETE("user-plants/{id}/")
    suspend fun deleteUserPlant(@Path("id") id: Int): Response<Unit>

    // Telemetría
    @GET("telemetry/")
    suspend fun getTelemetryHistory(
        @Query("module") moduleId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
    ): Response<TelemetryPageResponse>

    @POST("modules/{id}/telemetry/")
    suspend fun sendTelemetry(
        @Path("id") moduleId: Int,
        @Body telemetry: TelemetryDto,
    ): Response<TelemetryDto>

    // Búsqueda Botánica
    @GET("plants/search/")
    suspend fun searchPlants(@Query("q") query: String): Response<PlantSearchResponse>
}
