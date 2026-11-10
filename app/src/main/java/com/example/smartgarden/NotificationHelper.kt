package com.example.smartgarden

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Helper para la creación y despacho de notificaciones locales de alertas ambientales de Smart Garden.
 */
object NotificationHelper {

    private const val NOTIFICATION_ID_TEMP = 1001
    private const val NOTIFICATION_ID_MOISTURE = 1002

    @SuppressLint("MissingPermission")
    fun sendTemperatureAlert(context: Context, plantName: String, tempCelsius: Float) {
        // Verificar si el permiso de notificaciones está concedido en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val formattedTemp = String.format(java.util.Locale.US, "%.1f", tempCelsius)
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("🔥 Alerta Crítica de Temperatura")
            .setContentText("$plantName alcanzó ${formattedTemp}°C.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¡Atención! La maceta \"$plantName\" registra ${formattedTemp}°C (> 35°C). Se requiere atención inmediata o activar el sistema de ventilación/nebulización.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_TEMP, builder.build())
        }
    }

    @SuppressLint("MissingPermission")
    fun sendMoistureAlert(
        context: Context,
        plantName: String,
        moisturePercent: Int,
        minThresholdPercent: Int = 30,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("💧 Alerta Crítica de Humedad del Suelo")
            .setContentText("$plantName tiene humedad de suelo en $moisturePercent% (mínimo: $minThresholdPercent%).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¡Atención! La maceta \"$plantName\" tiene humedad de suelo de $moisturePercent% (debajo del mínimo de $minThresholdPercent%). Se recomienda activar la bomba de agua o aplicar una dosis de riego.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_MOISTURE, builder.build())
        }
    }
}
