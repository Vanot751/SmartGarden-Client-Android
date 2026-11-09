package com.example.smartgarden

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.smartgarden.network.LoginRequest
import com.example.smartgarden.network.RetrofitClient
import com.example.smartgarden.network.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CustomPlantProfile(
    val id: String,
    val apodo: String,
    val commonName: String = "",
    val tempMin: Float = 18f,
    val tempMax: Float = 35f,
    val minMoistureThreshold: Int = 30,
)

data class PlantSearchResultItem(
    val apiSpeciesId: String,
    val commonName: String?,
    val scientificName: String?,
    val watering: String?,
    val sunlight: List<String>?,
    val thumbnail: String?
)


/**
 * Categor├¡as bot├ínicas para los filtros tipo "Chips" del Dashboard.
 */
enum class PlantCategory(
    val displayName: String,
    val optimalTempRange: String,
    val minMoistureThreshold: Int,
) {
    TODAS("Todas", "20┬░C - 35┬░C", 30),
    TROPICALES("Tropicales", "22┬░C - 32┬░C", 35),
    SUCULENTAS("Suculentas", "18┬░C - 35┬░C", 20),
    AROMATICAS("Arom├íticas", "20┬░C - 30┬░C", 30),
}

/**
 * Estado global inmutable de FloraSense.
 */
data class FloraSenseUiState(
    // Estado de Autenticaci├│n
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val userEmail: String = "",
    val authError: String? = null,
    val isDarkMode: Boolean = false,
    val isoSecurityProtocol: String = "ISO/IEC 27400 ÔÇó TLS 1.3 ÔÇó AES-256-GCM",

    // Sincronizaci├│n Multi-Dispositivo
    val connectedDevicesCount: Int = 2,
    val isRealtimeConnected: Boolean = false,
    val lastSyncSource: String = "Django API",
    val syncLatencyMs: Int = 22,

    // Plantas y Cultivos Din├ímicos (API Django REST + Perenual)
    val userPlants: List<CustomPlantProfile> = listOf(
        CustomPlantProfile(id = "todas", apodo = "Todas", tempMin = 18f, tempMax = 35f, minMoistureThreshold = 30),
        CustomPlantProfile(id = "tropicales", apodo = "Tropicales", tempMin = 22f, tempMax = 32f, minMoistureThreshold = 35),
        CustomPlantProfile(id = "suculentas", apodo = "Suculentas", tempMin = 18f, tempMax = 38f, minMoistureThreshold = 20),
        CustomPlantProfile(id = "aromaticas", apodo = "Arom├íticas", tempMin = 20f, tempMax = 30f, minMoistureThreshold = 30),
    ),
    val activePlant: CustomPlantProfile = CustomPlantProfile(id = "todas", apodo = "Todas", tempMin = 18f, tempMax = 35f, minMoistureThreshold = 30),
    val isAddPlantDialogVisible: Boolean = false,
    val plantToDelete: CustomPlantProfile? = null,
    val plantSearchQuery: String = "",
    val plantSearchResults: List<PlantSearchResultItem> = emptyList(),
    val isSearchingPlants: Boolean = false,

    // Filtro de categor├¡a de planta activo en el Dashboard
    val selectedCategory: PlantCategory = PlantCategory.TODAS,

    // Telemetr├¡a Ambiental en Tiempo Real (Trama T,L,H)
    val temperatureCelsius: Float = 24.0f,
    val soilMoisturePercent: Int = 50,
    val sunlightPercent: Int = 78,

    // Estado de Actuadores y Dosificaci├│n (Panel de Control)
    val isWaterPumpOn: Boolean = true,
    val isNebulizerOn: Boolean = false,
    val selectedDoseMl: Int = 100, // Opciones: 50, 100, 250
    val isEmergencyStopActive: Boolean = false,
    val statusLogMessage: String = "Conectando...",
) {
    // Reglas de negocio deterministas del prototipo IoT
    val isCriticalHeat: Boolean
        get() = temperatureCelsius > 35.0f

    val isLowMoisture: Boolean
        get() = soilMoisturePercent < 30

    val hasCriticalAlert: Boolean
        get() = isCriticalHeat || isLowMoisture || isEmergencyStopActive
}

class FloraSenseViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FloraSenseUiState())
    val uiState: StateFlow<FloraSenseUiState> = _uiState.asStateFlow()

    init {
        // Verifica si ya existe una sesi├│n guardada con Django
        val savedToken = TokenManager.getToken()
        if (!savedToken.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    userEmail = "Usuario Autenticado",
                )
            }
        }

        // Mecanismo de Polling (Sondeo autom├ítico) para simular tiempo real
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isAuthenticated) {
                    fetchTelemetryFromDjango()
                }
                delay(5000L) // Espera 5 segundos antes de la siguiente iteraci├│n
            }
        }
    }

    // ========================================================================
    // TELEMETR├ìA (LECTURA DESDE DJANGO)
    // ========================================================================
    fun fetchTelemetryFromDjango() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getTelemetry()

                // Mapea la respuesta seg├║n la estructura de tu JSON
                // Por ejemplo, si tienes data asumiendo que contiene temperature y moisture:
                // (Modificar seg├║n la estructura final de TelemetryResponse)

                _uiState.update { current ->
                    current.copy(
                        isRealtimeConnected = true,
                        // Aqu├¡ asumimos valores por defecto hasta ajustar el data class real en base al JSON de Django
                        // temperatureCelsius = response.data.temperature ?: current.temperatureCelsius,
                        // soilMoisturePercent = response.data.moisture ?: current.soilMoisturePercent,
                        statusLogMessage = "Telemetr├¡a actualizada desde Django",
                        lastSyncSource = "Django REST API"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRealtimeConnected = false,
                        statusLogMessage = "Error de sincronizaci├│n con Django: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    // ========================================================================
    // PASO 2: AUTENTICACI├ôN REAL CON API REST EN DJANGO
    // ========================================================================
    fun loginWithDjango(
        username: String,
        password: String,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            _uiState.update {
                it.copy(authError = "Ingresa un usuario o correo electr├│nico v├ílido.")
            }
            return
        }
        if (password.length < 6) {
            _uiState.update {
                it.copy(authError = "Pol├¡tica ISO 27400: La contrase├▒a requiere m├¡nimo 6 caracteres.")
            }
            return
        }

        _uiState.update {
            it.copy(isAuthenticating = true, authError = null)
        }

        viewModelScope.launch {
            try {
                // Hacemos el llamado a Retrofit
                val request = LoginRequest(username = cleanUsername, password = password)
                val response = RetrofitClient.api.login(request)
                
                // Extraer el token dependiendo de si Django usa "token" o "access" (Simple JWT)
                val actualToken = response.token ?: response.access
                
                if (actualToken != null) {
                    // Guardamos el token de forma segura
                    TokenManager.saveToken(actualToken)
                    
                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthenticating = false,
                            userEmail = cleanUsername,
                            authError = null,
                            statusLogMessage = "Sesi├│n Django activa ($cleanUsername)",
                        )
                    }
                    onLoginSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = "El servidor no devolvi├│ un token de acceso v├ílido."
                        )
                    }
                }
            } catch (e: Exception) {
                // Manejo de errores de red o credenciales
                val errorMessage = "Error en el servidor: ${e.localizedMessage}"
                _uiState.update {
                    it.copy(
                        isAuthenticated = false,
                        isAuthenticating = false,
                        authError = errorMessage
                    )
                }
                onError(errorMessage)
            }
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        TokenManager.clearToken()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                userEmail = "",
                authError = null,
                statusLogMessage = "Sesi├│n cerrada",
            )
        }
        onLoggedOut()
    }

    // ========================================================================
    // CONTROL DE ACTUADORES (ESCRITURA) - PENDIENTE DE CONECTAR A DJANGO
    // ========================================================================
    fun setWaterPumpState(enabled: Boolean) {
        if (_uiState.value.isEmergencyStopActive) return
        // TODO: Implementar POST a Django
    }

    fun setNebulizerState(enabled: Boolean) {
        if (_uiState.value.isEmergencyStopActive) return
        // TODO: Implementar POST a Django
    }

    fun selectDoseMl(doseMl: Int) {
        if (_uiState.value.isEmergencyStopActive) return
        // TODO: Implementar POST a Django
    }

    fun triggerEmergencyStop() {
        // TODO: Implementar POST a Django
    }

    fun applyIrrigationDose() {
        if (_uiState.value.isEmergencyStopActive) return
        // TODO: Implementar POST a Django
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { current ->
            current.copy(isDarkMode = enabled)
        }
    }

    fun toggleDarkMode(context: Context) {
        val newMode = !_uiState.value.isDarkMode
        context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_dark_mode", newMode)
            .apply()
        _uiState.update { current ->
            current.copy(isDarkMode = newMode)
        }
    }

    // ========================================================================
    // FUNCIONES COMPLEMENTARIAS DEL DASHBOARD
    // ========================================================================
    fun selectPlantCategory(category: PlantCategory) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                statusLogMessage = "Perfil bot├ínico: ${category.displayName} (├ôptimo ${category.optimalTempRange})",
            )
        }
    }

    fun updateSimulatedTelemetry(
        temperature: Float = _uiState.value.temperatureCelsius,
        moisture: Int = _uiState.value.soilMoisturePercent,
        sunlight: Int = _uiState.value.sunlightPercent,
    ) {
        // TODO: Eliminar o refactorizar si se va a reemplazar por datos 100% reales de Django
        val clampedMoisture = moisture.coerceIn(0, 100)
        val clampedSunlight = sunlight.coerceIn(0, 100)

        _uiState.update { current ->
            current.copy(
                temperatureCelsius = temperature,
                soilMoisturePercent = clampedMoisture,
                sunlightPercent = clampedSunlight
            )
        }
    }

    fun simulateSecondDeviceSync() {
        if (_uiState.value.isEmergencyStopActive) return
        // TODO: Eliminar o refactorizar
    }
}
