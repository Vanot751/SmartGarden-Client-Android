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
    private const val FIREBASE_RTDB_URL = "https://smartgarden-web-react-iot-default-rtdb.firebaseio.com/telemetria/actual.json"

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
        val timestamp: String
    )

    // Flow confluente: descarta automáticamente valores intermedios y siempre procesa el último
    private val telemetryFlow = MutableSharedFlow<TelemetryPayload>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            telemetryFlow.collectLatest { payload ->
                try {
                    val body = gson.toJson(payload).toRequestBody(jsonMediaType)
                    val request = Request.Builder()
                        .url(FIREBASE_RTDB_URL)
                        .put(body)
                        .build()
                    client.newCall(request).execute().use { /* cerrar para reutilizar conexión en el pool */ }
                } catch (_: Exception) {
                    // Silencioso para funcionamiento offline
                }
            }
        }
    }

    fun syncTelemetry(temperatura: Float, humedad: Float, luz: Float) {
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val payload = TelemetryPayload(
            temperatura = temperatura,
            humedad = humedad,
            luz = luz,
            timestamp = isoDate
        )
        telemetryFlow.tryEmit(payload)
    }
}
