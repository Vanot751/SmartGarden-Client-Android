package com.example.smartgarden.network.models

import com.google.gson.annotations.SerializedName

data class PlantSearchResultItem(
    @SerializedName("api_species_id") val apiSpeciesId: String,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: String?,
    @SerializedName("watering") val watering: String?,
    @SerializedName("sunlight") val sunlight: List<String>?,
    @SerializedName("thumbnail") val thumbnail: String?,
)

data class PlantSearchResponse(
    @SerializedName("query") val query: String,
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<PlantSearchResultItem>,
)

data class UserPlantDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("module") val module: Int,
    @SerializedName("api_species_id") val apiSpeciesId: String? = null,
    @SerializedName("apodo") val apodo: String,
    @SerializedName("temp_min") val tempMin: Float = 15f,
    @SerializedName("temp_max") val tempMax: Float = 35f,
    @SerializedName("humedad_optima") val humedadOptima: Int = 50,
)
