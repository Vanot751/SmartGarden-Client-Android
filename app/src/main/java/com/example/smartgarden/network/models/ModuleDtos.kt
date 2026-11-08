package com.example.smartgarden.network.models

import com.google.gson.annotations.SerializedName

data class IoTModuleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("user") val user: String?,
    @SerializedName("mac_address") val macAddress: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("created_at") val createdAt: String?,
)

data class CreateModuleRequest(
    @SerializedName("mac_address") val macAddress: String,
    @SerializedName("nombre") val nombre: String,
)
