package com.example.smartgarden

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            if (manager != null && manager.getNotificationChannel(MainActivity.CHANNEL_ID) == null) {
                val channel = android.app.NotificationChannel(
                    MainActivity.CHANNEL_ID,
                    MainActivity.CHANNEL_NAME,
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de alertas de temperatura crítica para Smart Garden IoT"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendTemperatureAlert(context: Context, plantName: String, tempCelsius: Float) {
        ensureChannel(context)
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
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TEMP,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification_large)

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("🔥 Alerta Crítica de Temperatura")
            .setContentText("$plantName alcanzó ${formattedTemp}°C.")
            .setContentIntent(pendingIntent)
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
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_MOISTURE,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_notification_large)

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle("💧 Alerta Crítica de Humedad del Suelo")
            .setContentText("$plantName tiene humedad de suelo en $moisturePercent% (mínimo: $minThresholdPercent%).")
            .setContentIntent(pendingIntent)
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
