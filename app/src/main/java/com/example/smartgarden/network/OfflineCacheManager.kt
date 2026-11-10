package com.example.smartgarden.network

import android.content.Context
import android.content.SharedPreferences
import com.example.smartgarden.CustomPlantProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object OfflineCacheManager {
    private const val PREFS_NAME = "smartgarden_offline_cache"
    private const val KEY_PLANTS = "cached_plants"
    private const val KEY_MODULES = "cached_modules"
    private const val KEY_LAST_SYNC = "last_sync_time"
    private const val KEY_TELEMETRY = "cached_telemetry"

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun savePlants(plants: List<CustomPlantProfile>) {
        try {
            val json = gson.toJson(plants)
            prefs?.edit()?.putString(KEY_PLANTS, json)?.apply()
        } catch (_: Exception) {}
    }

    fun getPlants(): List<CustomPlantProfile> {
        return try {
            val json = prefs?.getString(KEY_PLANTS, null) ?: return emptyList()
            val type = object : TypeToken<List<CustomPlantProfile>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveModules(moduleIds: List<Int>) {
        try {
            val json = gson.toJson(moduleIds)
            prefs?.edit()?.putString(KEY_MODULES, json)?.apply()
        } catch (_: Exception) {}
    }

    fun getModules(): List<Int> {
        return try {
            val json = prefs?.getString(KEY_MODULES, null) ?: return emptyList()
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTelemetry(reading: TelemetryDto) {
        try {
            val json = gson.toJson(reading)
            prefs?.edit()?.putString(KEY_TELEMETRY, json)?.apply()
        } catch (_: Exception) {}
    }

    fun getTelemetry(): TelemetryDto? {
        return try {
            val json = prefs?.getString(KEY_TELEMETRY, null) ?: return null
            gson.fromJson(json, TelemetryDto::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun saveLastSync(timestampStr: String) {
        prefs?.edit()?.putString(KEY_LAST_SYNC, timestampStr)?.apply()
    }

    fun getLastSync(): String? {
        return prefs?.getString(KEY_LAST_SYNC, null)
    }

    fun clearCache() {
        prefs?.edit()?.clear()?.apply()
    }
}
