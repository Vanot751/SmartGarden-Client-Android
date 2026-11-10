package com.example.smartgarden.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SmartGardenApi {
    @POST("auth/login/")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register/")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @GET("modules/")
    suspend fun getModules(): PageResponse<GardenModuleDto>

    @POST("modules/")
    suspend fun createModule(@Body request: CreateModuleRequest): GardenModuleDto

    @GET("user-plants/")
    suspend fun getUserPlants(): PageResponse<UserPlantDto>

    @POST("user-plants/")
    suspend fun createUserPlant(@Body request: CreateUserPlantRequest): UserPlantDto

    @DELETE("user-plants/{id}/")
    suspend fun deleteUserPlant(@Path("id") id: Int)

    @GET("telemetry/")
    suspend fun getTelemetry(
        @Query("module") moduleId: Int? = null,
        @Query("page_size") pageSize: Int = 1,
    ): PageResponse<TelemetryDto>

    @GET("telemetry/history/")
    suspend fun getTelemetryHistory(@Query("range") range: String): TelemetryHistoryResponse

    @GET("plants/search/")
    suspend fun searchPlants(@Query("q") query: String): PlantSearchResponse

    @POST("modules/{id}/telemetry/")
    suspend fun submitTelemetry(
        @Path("id") moduleId: Int,
        @Body request: TelemetryRequest,
    ): TelemetryDto
}

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("password2") val passwordConfirmation: String,
)

data class LoginResponse(
    @SerializedName("access") val access: String? = null,
    @SerializedName("refresh") val refresh: String? = null,
    @SerializedName("token") val token: String? = null,
)

data class PageResponse<T>(
    val count: Int = 0,
    val results: List<T> = emptyList(),
)

data class GardenModuleDto(
    val id: Int,
    @SerializedName("mac_address") val macAddress: String,
    val nombre: String,
    val plant: UserPlantDto? = null,
)

data class CreateModuleRequest(
    @SerializedName("mac_address") val macAddress: String,
    val nombre: String,
)

data class UserPlantDto(
    val id: Int,
    val module: Int,
    @SerializedName("api_species_id") val apiSpeciesId: String,
    val apodo: String,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    @SerializedName("humedad_optima") val optimalHumidity: Float,
)

data class CreateUserPlantRequest(
    val module: Int? = null,
    @SerializedName("api_species_id") val apiSpeciesId: String,
    val apodo: String,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    @SerializedName("humedad_optima") val optimalHumidity: Float,
)

data class TelemetryDto(
    val id: Int? = null,
    val module: Int? = null,
    val temperatura: Float? = null,
    val humedad: Float? = null,
    val luz: Float? = null,
    val timestamp: String? = null,
)

data class TelemetryRequest(
    val temperatura: Float,
    val humedad: Float,
    val luz: Float,
)

data class TelemetryHistoryResponse(
    val rango: String? = null,
    val umbrales: TelemetryThresholds? = null,
    val historial: List<HistoryPoint> = emptyList(),
)

data class TelemetryThresholds(
    @SerializedName("temp_max") val tempMax: Float = 35f,
    @SerializedName("temp_min") val tempMin: Float = 18f,
    @SerializedName("humedad_min") val humidityMin: Float = 30f,
)

data class HistoryPoint(
    val timestamp: String? = null,
    val label: String? = null,
    val temperatura: Float = 0f,
    @SerializedName("humedadSuelo") val soilHumidity: Float = 0f,
    @SerializedName("luzSolar") val solarLight: Float = 0f,
)

data class PlantSearchResponse(
    val query: String? = null,
    val count: Int = 0,
    val results: List<PlantSearchDto> = emptyList(),
)

data class PlantSearchDto(
    @SerializedName("api_species_id") val apiSpeciesId: String,
    @SerializedName("common_name") val commonName: String? = null,
    @SerializedName("scientific_name") val scientificName: String? = null,
    val watering: String? = null,
    val sunlight: List<String>? = null,
    val thumbnail: String? = null,
    @SerializedName("temp_min") val tempMin: Float? = null,
    @SerializedName("temp_max") val tempMax: Float? = null,
    @SerializedName("humidity_min") val humidityMin: Float? = null,
)
