package com.example.smartgarden.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.smartgarden.MainActivity
import com.example.smartgarden.NotificationHelper
import com.example.smartgarden.R
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Servicio en segundo plano que mantiene activo el monitoreo de alertas ambientales
 * incluso con la aplicación cerrada o el teléfono bloqueado.
 */
class TelemetryGuardianService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // Stream continuo sin timeout
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    private var lastTempAlertTimestamp = 0L
    private var lastMoistureAlertTimestamp = 0L

    companion object {
        const val GUARDIAN_CHANNEL_ID = "smartgarden_guardian_status"
        const val GUARDIAN_NOTIFICATION_ID = 2001
        private const val ALERT_COOLDOWN_MS = 25_000L // 25s entre alertas sonoras

        fun startGuardian(context: Context) {
            val intent = Intent(context, TelemetryGuardianService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopGuardian(context: Context) {
            val intent = Intent(context, TelemetryGuardianService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createGuardianChannel()
        startInForeground()
        listenToFirebaseStream()
    }

    private fun createGuardianChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GUARDIAN_CHANNEL_ID,
                "SmartGarden Guardián",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoreo ambiental continuo para alertas en tiempo real"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startInForeground() {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, GUARDIAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("SmartGarden: Guardián Activo")
            .setContentText("Monitoreo ambiental 24/7 en segundo plano")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                GUARDIAN_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(GUARDIAN_NOTIFICATION_ID, notification)
        }
    }

    private fun listenToFirebaseStream() {
        serviceScope.launch {
            val rtdbUrl = "https://smartgarden-web-react-iot-default-rtdb.firebaseio.com/telemetria/actual.json"

            while (isActive) {
                try {
                    val request = Request.Builder()
                        .url(rtdbUrl)
                        .addHeader("Accept", "text/event-stream")
                        .build()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            delay(5000L)
                            return@use
                        }

                        val source = response.body?.source() ?: return@use
                        while (isActive && !source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (line.startsWith("data: ")) {
                                val rawJson = line.removePrefix("data: ").trim()
                                if (rawJson.isNotEmpty() && rawJson != "null") {
                                    handleFirebasePayload(rawJson)
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    delay(4000L) // Reintento automático en caso de pérdida de conexión
                }
            }
        }
    }

    private fun handleFirebasePayload(rawJson: String) {
        try {
            val root = JsonParser.parseString(rawJson).asJsonObject
            val path = root.get("path")?.asString ?: "/"
            val dataElem = root.get("data") ?: return

            var temp: Float? = null
            var hum: Float? = null

            if (dataElem.isJsonObject) {
                val dataObj = dataElem.asJsonObject
                if (dataObj.has("temperatura")) temp = dataObj.get("temperatura").asFloat
                if (dataObj.has("humedad")) hum = dataObj.get("humedad").asFloat
                if (dataObj.has("humedadSuelo")) hum = dataObj.get("humedadSuelo").asFloat
            } else if (dataElem.isJsonPrimitive) {
                if (path == "/temperatura") temp = dataElem.asFloat
                if (path == "/humedad" || path == "/humedadSuelo") hum = dataElem.asFloat
            }

            val now = System.currentTimeMillis()

            // Disparo de Alerta de Temperatura Crítica (> 35°C)
            if (temp != null && temp > 35.0f) {
                if (now - lastTempAlertTimestamp > ALERT_COOLDOWN_MS) {
                    lastTempAlertTimestamp = now
                    NotificationHelper.sendTemperatureAlert(
                        context = applicationContext,
                        plantName = "Maceta Inteligente",
                        tempCelsius = temp
                    )
                }
            }

            // Disparo de Alerta de Humedad Crítica (< 30%)
            if (hum != null && hum < 30.0f) {
                if (now - lastMoistureAlertTimestamp > ALERT_COOLDOWN_MS) {
                    lastMoistureAlertTimestamp = now
                    NotificationHelper.sendMoistureAlert(
                        context = applicationContext,
                        plantName = "Maceta Inteligente",
                        moisturePercent = hum.toInt(),
                        minThresholdPercent = 30
                    )
                }
            }
        } catch (_: Exception) {
            // Payload omitido de manera segura
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
