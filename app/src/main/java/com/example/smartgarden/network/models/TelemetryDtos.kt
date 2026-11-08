package com.example.smartgarden.network.models

import com.google.gson.annotations.SerializedName

data class TelemetryDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("module") val module: Int? = null,
    @SerializedName("temperatura") val temperatura: Float,
    @SerializedName("humedad") val humedad: Float,
    @SerializedName("luz") val luz: Float,
    @SerializedName("timestamp") val timestamp: String? = null,
)

data class TelemetryPageResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<TelemetryDto>,
)
