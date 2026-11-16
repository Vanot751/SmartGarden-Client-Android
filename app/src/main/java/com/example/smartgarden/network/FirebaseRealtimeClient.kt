package com.example.smartgarden.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FirebaseRealtimeClient {
    private const val FIREBASE_RTDB_URL = "https://smartgarden-web-react-iot-default-rtdb.firebaseio.com/telemetria/actual.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class TelemetryPayload(
        val temperatura: Float,
        val humedad: Float,
        val luz: Float,
        val nodo: String = "Módulo IoT #1",
        val timestamp: String
    )

    suspend fun syncTelemetry(temperatura: Float, humedad: Float, luz: Float) {
        withContext(Dispatchers.IO) {
            try {
                val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                val payload = TelemetryPayload(
                    temperatura = temperatura,
                    humedad = humedad,
                    luz = luz,
                    timestamp = isoDate
                )
                val body = gson.toJson(payload).toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(FIREBASE_RTDB_URL)
                    .put(body)
                    .build()
                client.newCall(request).execute().close()
            } catch (_: Exception) {
                // Silencioso para funcionamiento offline
            }
        }
    }
}
