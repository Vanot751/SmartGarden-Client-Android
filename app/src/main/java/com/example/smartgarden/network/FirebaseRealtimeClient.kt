package com.example.smartgarden.network

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FirebaseRealtimeClient {
    private const val FIREBASE_BASE_URL = "https://smartgarden-web-react-iot-default-rtdb.firebaseio.com/telemetria"

    // Reutilizar conexiones HTTP persistentes para latencia mínima (< 30ms)
    private val client = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .connectTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class TelemetryPayload(
        val temperatura: Float,
        val humedad: Float,
        val luz: Float,
        val nodo: String = "Módulo IoT #1",
        val planta: String? = null,
        val plantId: String? = null,
        val user: String? = null,
        val timestamp: String
    )

    data class EnqueuedTelemetry(
        val userKey: String,
        val payload: TelemetryPayload
    )

    fun sanitizeUserKey(rawUser: String?): String {
        val clean = (rawUser ?: "default").trim().lowercase()
        return clean.replace(Regex("[.#$\\[\\]/]"), "_")
    }

    fun getTelemetryUrl(rawUser: String?): String {
        val key = sanitizeUserKey(rawUser)
        return "$FIREBASE_BASE_URL/$key/actual.json"
    }

    // Flow confluente: descarta automáticamente valores intermedios y siempre procesa el último
    private val telemetryFlow = MutableSharedFlow<EnqueuedTelemetry>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            telemetryFlow.collectLatest { item ->
                try {
                    val url = "$FIREBASE_BASE_URL/${item.userKey}/actual.json"
                    val body = gson.toJson(item.payload).toRequestBody(jsonMediaType)
                    val request = Request.Builder()
                        .url(url)
                        .put(body)
                        .build()
                    client.newCall(request).execute().use { /* cerrar para reutilizar conexión en el pool */ }
                } catch (_: Exception) {
                    // Silencioso para funcionamiento offline
                }
            }
        }
    }

    fun syncTelemetry(
        temperatura: Float,
        humedad: Float,
        luz: Float,
        plantName: String? = null,
        plantId: String? = null,
        username: String? = null
    ) {
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val effectiveUser = username ?: TokenManager.getUsername() ?: "default"
        val userKey = sanitizeUserKey(effectiveUser)
        val payload = TelemetryPayload(
            temperatura = temperatura,
            humedad = humedad,
            luz = luz,
            planta = plantName,
            plantId = plantId,
            user = effectiveUser,
            timestamp = isoDate
        )
        telemetryFlow.tryEmit(EnqueuedTelemetry(userKey, payload))
    }
}
