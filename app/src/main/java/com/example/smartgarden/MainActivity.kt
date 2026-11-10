package com.example.smartgarden

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.smartgarden.network.TokenManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

// ============================================================================
// ESTRUCTURA Y PALETAS DE COLOR: PASTEL MINIMALISM + MODO OSCURO OLED
// ============================================================================
data class FloraSenseColors(
    val background: Color,
    val surfaceContainer: Color,
    val surfaceHigh: Color,
    val textPrimary: Color,
    val textSubtle: Color,
    val emeraldActive: Color,
    val emeraldSoftBg: Color,
    val pastelOrangeTemp: Color,
    val pastelOrangeAccent: Color,
    val pastelBlueMoisture: Color,
    val pastelBlueAccent: Color,
    val pastelYellowSolar: Color,
    val pastelYellowAccent: Color,
    val emergencyRed: Color,
    val emergencySoftBg: Color,
)

// Modo Claro Original (Pastel Minimalism)
val LightFloraSenseColors = FloraSenseColors(
    background = Color(0xFFF5F5F0),
    surfaceContainer = Color(0xFFEBEBE4),
    surfaceHigh = Color(0xFFE0E0D6),
    textPrimary = Color(0xFF2E2E2E),
    textSubtle = Color(0xFF5C5C57),
    emeraldActive = Color(0xFF2ECC71),
    emeraldSoftBg = Color(0xFFD5F5E3),
    pastelOrangeTemp = Color(0xFFFCE7D6),
    pastelOrangeAccent = Color(0xFF9A3412),
    pastelBlueMoisture = Color(0xFFD6EAF8),
    pastelBlueAccent = Color(0xFF0369A1),
    pastelYellowSolar = Color(0xFFFEF9C3),
    pastelYellowAccent = Color(0xFF854D0E),
    emergencyRed = Color(0xFFE74C3C),
    emergencySoftBg = Color(0xFFFADBD8),
)

// Modo Oscuro OLED Puro (True Black #000000)
val OledDarkFloraSenseColors = FloraSenseColors(
    background = Color(0xFF000000),          // Negro Puro OLED
    surfaceContainer = Color(0xFF121212),     // Superficie Oscura OLED
    surfaceHigh = Color(0xFF1E1E1E),          // Realce Oscuro OLED
    textPrimary = Color(0xFFF5F5F5),          // Texto Blanco Apagado
    textSubtle = Color(0xFFA0A09C),           // Texto Secundario Gris Claro
    emeraldActive = Color(0xFF2ECC71),        // Verde Esmeralda Vibrante
    emeraldSoftBg = Color(0xFF0F3822),        // Verde Oscuro Tinta
    pastelOrangeTemp = Color(0xFF2E1A11),     // Melocotón Oscuro Tinta
    pastelOrangeAccent = Color(0xFFFB923C),   // Naranja Neón Acento
    pastelBlueMoisture = Color(0xFF0D2538),   // Celeste Oscuro Tinta
    pastelBlueAccent = Color(0xFF38BDF8),     // Azul Neón Acento
    pastelYellowSolar = Color(0xFF282510),    // Amarillo Oscuro Tinta
    pastelYellowAccent = Color(0xFFFACC15),   // Amarillo Ámbar Acento
    emergencyRed = Color(0xFFEF4444),         // Rojo Neón Emergencia
    emergencySoftBg = Color(0xFF3B1215),      // Rojo Oscuro Tinta
)

val LocalFloraSenseColors = staticCompositionLocalOf { LightFloraSenseColors }

// Getters reactivos de colores dinámicos según el modo activo
val PearlWarmBackground: Color @Composable get() = LocalFloraSenseColors.current.background
val WarmSurfaceContainer: Color @Composable get() = LocalFloraSenseColors.current.surfaceContainer
val WarmSurfaceHigh: Color @Composable get() = LocalFloraSenseColors.current.surfaceHigh
val CharcoalText: Color @Composable get() = LocalFloraSenseColors.current.textPrimary
val CharcoalSubtle: Color @Composable get() = LocalFloraSenseColors.current.textSubtle
val EmeraldActive: Color @Composable get() = LocalFloraSenseColors.current.emeraldActive
val EmeraldSoftBg: Color @Composable get() = LocalFloraSenseColors.current.emeraldSoftBg
val PastelOrangeTemp: Color @Composable get() = LocalFloraSenseColors.current.pastelOrangeTemp
val PastelOrangeAccent: Color @Composable get() = LocalFloraSenseColors.current.pastelOrangeAccent
val PastelBlueMoisture: Color @Composable get() = LocalFloraSenseColors.current.pastelBlueMoisture
val PastelBlueAccent: Color @Composable get() = LocalFloraSenseColors.current.pastelBlueAccent
val PastelYellowSolar: Color @Composable get() = LocalFloraSenseColors.current.pastelYellowSolar
val PastelYellowAccent: Color @Composable get() = LocalFloraSenseColors.current.pastelYellowAccent
val EmergencyRed: Color @Composable get() = LocalFloraSenseColors.current.emergencyRed
val EmergencySoftBg: Color @Composable get() = LocalFloraSenseColors.current.emergencySoftBg

class MainActivity : ComponentActivity() {

    companion object {
        const val CHANNEL_ID = "temp_alerts_channel"
        const val CHANNEL_NAME = "Alertas de Temperatura Smart Garden"
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permiso de notificaciones otorgado o denegado por el usuario
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializamos el manejador seguro de Tokens
        TokenManager.init(applicationContext)
        
        enableEdgeToEdge()

        // Configuración de notificaciones locales para alertas de temperatura
        createNotificationChannel()
        checkAndRequestNotificationPermission()

        setContent {
            AppNavigation()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notificaciones de alertas de temperatura crítica para Smart Garden IoT"
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun FloraSenseTheme(
    isDarkMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (isDarkMode) OledDarkFloraSenseColors else LightFloraSenseColors

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = colors.emeraldActive,
            onPrimary = colors.textPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surfaceContainer,
            onSurface = colors.textPrimary,
            error = colors.emergencyRed,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = colors.emeraldActive,
            onPrimary = colors.textPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surfaceContainer,
            onSurface = colors.textPrimary,
            error = colors.emergencyRed,
            onError = Color.White,
        )
    }

    val typography = Typography(
        displayLarge = TextStyle(
            fontSize = 38.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            letterSpacing = (-0.8).sp,
        ),
        headlineMedium = TextStyle(
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        ),
        titleMedium = TextStyle(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        ),
        bodyMedium = TextStyle(
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            color = colors.textPrimary,
        ),
        labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSubtle,
            letterSpacing = 0.7.sp,
        ),
    )

    CompositionLocalProvider(
        LocalFloraSenseColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FloraSenseAppPreview() {
    FloraSenseTheme {
        DashboardScreen(
            uiState = FloraSenseUiState(),
            onSelectCategory = {},
            onSelectPlant = {},
            onEnsureModule = {},
            onSearchPlants = {},
            onAddPlant = {},
            onDeletePlant = {},
            onUpdateSimulatedTelemetry = { _, _, _ -> },
            onSimulateSecondDevice = {},
            onNavigateToControls = {},
            onLogout = {},
            onToggleDarkMode = {},
        )
    }
}
