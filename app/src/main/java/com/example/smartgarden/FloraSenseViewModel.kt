package com.example.smartgarden

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartgarden.network.CreateUserPlantRequest
import com.example.smartgarden.network.LoginRequest
import com.example.smartgarden.network.OfflineCacheManager
import com.example.smartgarden.network.PlantSearchDto
import com.example.smartgarden.network.RetrofitClient
import com.example.smartgarden.network.TelemetryDto
import com.example.smartgarden.network.TokenManager
import com.example.smartgarden.network.UserPlantDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CustomPlantProfile(
    val id: String,
    val apodo: String,
    val commonName: String = "",
    val tempMin: Float = 18f,
    val tempMax: Float = 35f,
    val minMoistureThreshold: Int = 30,
    val moduleId: Int? = null,
)

data class PlantSearchResultItem(
    val apiSpeciesId: String,
    val commonName: String?,
    val scientificName: String?,
    val watering: String?,
    val sunlight: List<String>?,
    val thumbnail: String?,
    val tempMin: Float? = null,
    val tempMax: Float? = null,
    val humidityMin: Float? = null,
)

enum class PlantCategory(
    val displayName: String,
    val optimalTempRange: String,
    val minMoistureThreshold: Int,
) {
    TODAS("Todas", "20°C - 35°C", 30),
    TROPICALES("Tropicales", "22°C - 32°C", 35),
    SUCULENTAS("Suculentas", "18°C - 35°C", 20),
    AROMATICAS("Aromáticas", "20°C - 30°C", 30),
}

data class FloraSenseUiState(
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val userEmail: String = "",
    val authError: String? = null,
    val isDarkMode: Boolean = false,
    val isoSecurityProtocol: String = "Django REST • JWT • TLS",
    val connectedDevicesCount: Int = 0,
    val isRealtimeConnected: Boolean = false,
    val lastSyncSource: String = "API Django",
    val syncLatencyMs: Int = 0,
    val moduleIds: List<Int> = emptyList(),
    val userPlants: List<CustomPlantProfile> = emptyList(),
    val activePlant: CustomPlantProfile? = null,
    val isAddPlantDialogVisible: Boolean = false,
    val plantToDelete: CustomPlantProfile? = null,
    val plantSearchQuery: String = "",
    val plantSearchResults: List<PlantSearchResultItem> = emptyList(),
    val isSearchingPlants: Boolean = false,
    val addingPlantId: String? = null,
    val selectedCategory: PlantCategory = PlantCategory.TODAS,
    val temperatureCelsius: Float = 24f,
    val soilMoisturePercent: Int = 50,
    val sunlightPercent: Int = 78,
    val isWaterPumpOn: Boolean = false,
    val isNebulizerOn: Boolean = false,
    val selectedDoseMl: Int = 100,
    val isEmergencyStopActive: Boolean = false,
    val statusLogMessage: String = "Listo para sincronizar",
) {
    val isCriticalHeat: Boolean get() = temperatureCelsius > 35f
    val activeMoistureThreshold: Int
        get() = activePlant?.minMoistureThreshold ?: selectedCategory.minMoistureThreshold
    val isLowMoisture: Boolean get() = soilMoisturePercent < activeMoistureThreshold
    val hasCriticalAlert: Boolean get() = isCriticalHeat || isLowMoisture || isEmergencyStopActive
}

class FloraSenseViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FloraSenseUiState())
    val uiState: StateFlow<FloraSenseUiState> = _uiState.asStateFlow()
    private var telemetrySubmissionJob: Job? = null

    init {
        val savedToken = TokenManager.getToken()
        val savedUsername = TokenManager.getUsername()
        if (!savedToken.isNullOrEmpty()) {
            val cachedPlants = OfflineCacheManager.getPlants()
            val cachedModules = OfflineCacheManager.getModules()
            val cachedTelemetry = OfflineCacheManager.getTelemetry()

            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    userEmail = savedUsername ?: "sicosis3dfx",
                    userPlants = cachedPlants,
                    activePlant = cachedPlants.firstOrNull(),
                    moduleIds = cachedModules,
                    connectedDevicesCount = cachedModules.size,
                    temperatureCelsius = cachedTelemetry?.temperatura ?: it.temperatureCelsius,
                    soilMoisturePercent = (cachedTelemetry?.humedad ?: it.soilMoisturePercent.toFloat()).toInt().coerceIn(0, 100),
                    sunlightPercent = (cachedTelemetry?.luz ?: it.sunlightPercent.toFloat()).toInt().coerceIn(0, 100),
                    statusLogMessage = if (cachedPlants.isNotEmpty()) "Sincronizando con Django..." else "Conectando con la API...",
                )
            }
            refreshCloudState()
        }
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isAuthenticated) fetchTelemetryFromDjango()
                delay(15_000L)
            }
        }
    }

    fun refreshCloudState() {
        viewModelScope.launch {
            try {
                val modules = loadOrCreateModule()
                val plants = RetrofitClient.api.getUserPlants().results
                val mapped = plants.map(::toProfile)

                // Guardar en almacenamiento seguro offline
                OfflineCacheManager.savePlants(mapped)
                OfflineCacheManager.saveModules(modules.map { it.id })

                _uiState.update {
                    it.copy(
                        userPlants = mapped,
                        activePlant = mapped.firstOrNull { it.id == _uiState.value.activePlant?.id }
                            ?: mapped.firstOrNull(),
                        moduleIds = modules.map { it.id },
                        connectedDevicesCount = modules.size,
                        isRealtimeConnected = true,
                        lastSyncSource = "API Django",
                        statusLogMessage = if (modules.isEmpty()) {
                            "No hay macetas disponibles. Pulsa 'Crear maceta' para continuar."
                        } else if (mapped.isEmpty()) {
                            "Maceta virtual lista para agregar una planta"
                        } else {
                            "Macetas sincronizadas"
                        },
                    )
                }
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isRealtimeConnected = false,
                        statusLogMessage = "No se pudieron sincronizar las macetas: ${parseHttpError(error)}",
                    )
                }
            } catch (error: Exception) {
                markApiError("No se pudieron sincronizar las macetas", error)
            }
        }
    }

    fun ensureModuleAvailable() {
        viewModelScope.launch {
            try {
                loadOrCreateModule()
                refreshCloudState()
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isRealtimeConnected = false,
                        statusLogMessage = "No se pudo crear la maceta virtual: ${parseHttpError(error)}",
                    )
                }
            } catch (error: Exception) {
                markApiError("No se pudo crear la maceta virtual", error)
            }
        }
    }

    private suspend fun loadOrCreateModule(): List<com.example.smartgarden.network.GardenModuleDto> {
        var modules = RetrofitClient.api.getModules().results
        if (modules.isEmpty()) {
            modules = listOf(
                RetrofitClient.api.createModule(
                    com.example.smartgarden.network.CreateModuleRequest(
                        macAddress = generateVirtualMacAddress(),
                        nombre = "Maceta virtual 1",
                    )
                )
            )
        }
        return modules
    }

    private fun generateVirtualMacAddress(): String {
        val bytes = UUID.randomUUID().toString()
            .replace("-", "")
            .takeLast(10)
            .chunked(2)
        return "02:${bytes.joinToString(":")}".uppercase()
    }

    fun fetchTelemetryFromDjango() {
        viewModelScope.launch {
            try {
                val startedAt = System.currentTimeMillis()
                val moduleId = _uiState.value.activePlant?.moduleId
                val reading = RetrofitClient.api.getTelemetry(moduleId = moduleId, pageSize = 1).results.firstOrNull()
                if (reading == null) {
                    _uiState.update {
                        it.copy(isRealtimeConnected = true, statusLogMessage = "Sin lecturas de telemetría todavía")
                    }
                    return@launch
                }
                applyTelemetry(reading, (System.currentTimeMillis() - startedAt).toInt())
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                markApiError("No se pudo actualizar la telemetría", error)
            } catch (error: Exception) {
                markApiError("No se pudo actualizar la telemetría", error)
            }
        }
    }

    fun loginWithDjango(
        username: String,
        password: String,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty() || password.length < 6) {
            val message = "Ingresa un usuario válido y una contraseña de al menos 6 caracteres."
            _uiState.update { it.copy(authError = message) }
            onError(message)
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(cleanUsername, password))
                val accessToken = response.access ?: response.token
                    ?: error("El servidor no devolvió un token de acceso.")
                TokenManager.saveSession(
                    accessToken = accessToken,
                    refreshToken = response.refresh,
                    username = cleanUsername
                )
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        isAuthenticating = false,
                        userEmail = cleanUsername,
                        statusLogMessage = "Sesión conectada con Django",
                    )
                }
                refreshCloudState()
                onLoginSuccess()
            } catch (error: Exception) {
                val message = "No se pudo iniciar sesión. Revisa tus credenciales y conexión."
                _uiState.update { it.copy(isAuthenticated = false, isAuthenticating = false, authError = message) }
                onError(message)
            }
        }
    }

    fun registerWithDjango(
        email: String,
        password: String,
        passwordConfirmation: String,
        onRegisterSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val cleanUsername = email.trim()
        if (cleanUsername.isEmpty() || password.length < 8 || password != passwordConfirmation) {
            val message = "Revisa el usuario, la contraseña y su confirmación."
            _uiState.update { it.copy(authError = message) }
            onError(message)
            return
        }
        _uiState.update { it.copy(isAuthenticating = true, authError = null) }
        viewModelScope.launch {
            try {
                val req = com.example.smartgarden.network.RegisterRequest(
                    username = cleanUsername,
                    email = cleanUsername,
                    password = password,
                    passwordConfirmation = passwordConfirmation,
                )
                val response = RetrofitClient.api.register(req)
                val token = response.access ?: response.token
                if (!token.isNullOrEmpty()) {
                    TokenManager.saveSession(
                        accessToken = token,
                        refreshToken = response.refresh,
                        username = cleanUsername
                    )
                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthenticating = false,
                            userEmail = cleanUsername,
                            statusLogMessage = "Cuenta registrada e iniciada exitosamente",
                        )
                    }
                    refreshCloudState()
                    onRegisterSuccess()
                } else {
                    val message = "La API no devolvió los tokens de la cuenta registrada."
                    _uiState.update { it.copy(isAuthenticated = false, isAuthenticating = false, authError = message) }
                    onError(message)
                }
            } catch (error: retrofit2.HttpException) {
                val message = parseHttpError(error)
                _uiState.update { it.copy(isAuthenticated = false, isAuthenticating = false, authError = message) }
                onError(message)
            } catch (error: Exception) {
                val message = "Error al conectar con la API: ${error.message ?: "Sin conexión a internet"}"
                _uiState.update { it.copy(isAuthenticated = false, isAuthenticating = false, authError = message) }
                onError(message)
            }
        }
    }

    private fun parseHttpError(error: retrofit2.HttpException): String {
        return try {
            val errorJson = error.response()?.errorBody()?.string()
            if (!errorJson.isNullOrEmpty()) {
                val jsonElement = com.google.gson.JsonParser.parseString(errorJson)
                if (jsonElement.isJsonObject) {
                    val obj = jsonElement.asJsonObject
                    val messages = mutableListOf<String>()
                    obj.entrySet().forEach { (key, value) ->
                        val fieldName = when (key) {
                            "module" -> "Módulo"
                            "api_species_id" -> "Especie"
                            "apodo" -> "Apodo"
                            "humedad_optima" -> "Humedad"
                            "temp_min" -> "Temp. Mínima"
                            "temp_max" -> "Temp. Máxima"
                            "detail" -> ""
                            "non_field_errors" -> ""
                            else -> key
                        }
                        val strValue = if (value.isJsonArray) {
                            value.asJsonArray.joinToString { it.asString }
                        } else if (value.isJsonPrimitive) {
                            value.asString
                        } else {
                            value.toString()
                        }
                        if (fieldName.isEmpty()) {
                            messages.add(strValue)
                        } else {
                            messages.add("$fieldName: $strValue")
                        }
                    }
                    if (messages.isNotEmpty()) {
                        return messages.joinToString(" • ")
                    }
                }
            }
            "Error de solicitud (Código ${error.code()})"
        } catch (_: Exception) {
            "Error de servidor (${error.code()})"
        }
    }

    private fun handleSessionExpired() {
        TokenManager.clearAll()
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                isRealtimeConnected = false,
                authError = "Tu sesión ha expirado por seguridad. Por favor ingresa nuevamente.",
                statusLogMessage = "Sesión expirada",
            )
        }
    }

    fun clearAuthError() = _uiState.update { it.copy(authError = null) }

    fun logout(onLoggedOut: () -> Unit) {
        TokenManager.clearAll()
        OfflineCacheManager.clearCache()
        _uiState.value = FloraSenseUiState(statusLogMessage = "Sesión cerrada")
        onLoggedOut()
    }

    fun setWaterPumpState(enabled: Boolean) {
        if (!_uiState.value.isEmergencyStopActive) {
            _uiState.update { it.copy(isWaterPumpOn = enabled, statusLogMessage = "Bomba actualizada localmente") }
        }
    }

    fun setNebulizerState(enabled: Boolean) {
        if (!_uiState.value.isEmergencyStopActive) {
            _uiState.update { it.copy(isNebulizerOn = enabled, statusLogMessage = "Nebulizador actualizado localmente") }
        }
    }

    fun selectDoseMl(doseMl: Int) {
        if (!_uiState.value.isEmergencyStopActive) _uiState.update { it.copy(selectedDoseMl = doseMl) }
    }

    fun triggerEmergencyStop() {
        _uiState.update {
            it.copy(
                isEmergencyStopActive = !it.isEmergencyStopActive,
                isWaterPumpOn = false,
                isNebulizerOn = false,
                statusLogMessage = if (!it.isEmergencyStopActive) "Parada de emergencia local activa" else "Parada de emergencia restablecida",
            )
        }
    }

    fun applyIrrigationDose() {
        if (!_uiState.value.isEmergencyStopActive) {
            _uiState.update { it.copy(statusLogMessage = "Riego de ${it.selectedDoseMl} ml preparado localmente") }
        }
    }

    fun setDarkMode(enabled: Boolean) = _uiState.update { it.copy(isDarkMode = enabled) }

    fun toggleDarkMode(context: Context) {
        val enabled = !_uiState.value.isDarkMode
        context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("is_dark_mode", enabled).apply()
        setDarkMode(enabled)
    }

    fun selectPlantCategory(category: PlantCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectActivePlant(plant: CustomPlantProfile) {
        _uiState.update {
            it.copy(
                activePlant = plant,
                statusLogMessage = "${plant.apodo} seleccionada",
            )
        }
        fetchTelemetryFromDjango()
    }

    fun updateSimulatedTelemetry(temperature: Float = _uiState.value.temperatureCelsius, moisture: Int = _uiState.value.soilMoisturePercent, sunlight: Int = _uiState.value.sunlightPercent) {
        val normalizedMoisture = moisture.coerceIn(0, 100)
        val normalizedSunlight = sunlight.coerceIn(0, 100)
        _uiState.update {
            it.copy(
                temperatureCelsius = temperature,
                soilMoisturePercent = normalizedMoisture,
                sunlightPercent = normalizedSunlight,
                statusLogMessage = "Guardando lectura de ${it.activePlant?.apodo ?: "la maceta"}...",
            )
        }
        telemetrySubmissionJob?.cancel()
        telemetrySubmissionJob = viewModelScope.launch {
            delay(400L)
            val plant = _uiState.value.activePlant
            val moduleId = plant?.moduleId
            if (moduleId == null) {
                _uiState.update { it.copy(statusLogMessage = "Selecciona una maceta con módulo para guardar la lectura") }
                return@launch
            }
            try {
                val reading = RetrofitClient.api.submitTelemetry(
                    moduleId = moduleId,
                    request = com.example.smartgarden.network.TelemetryRequest(
                        temperatura = _uiState.value.temperatureCelsius,
                        humedad = _uiState.value.soilMoisturePercent.toFloat(),
                        luz = _uiState.value.sunlightPercent.toFloat(),
                    ),
                )
                applyTelemetry(reading, _uiState.value.syncLatencyMs)
                _uiState.update { it.copy(statusLogMessage = "Lectura guardada en la API para ${plant.apodo}") }
            } catch (error: Exception) {
                markApiError("No se pudo guardar la lectura en la API", error)
            }
        }
    }

    fun simulateSecondDeviceSync() {
        _uiState.update { it.copy(statusLogMessage = "La API Django es la fuente compartida; actualiza para consultar cambios") }
        refreshCloudState()
        fetchTelemetryFromDjango()
    }

    fun searchPlants(query: String) {
        _uiState.update {
            it.copy(
                plantSearchQuery = query,
                isSearchingPlants = query.isNotBlank(),
                statusLogMessage = if (query.isBlank()) "Búsqueda limpiada" else "Buscando especies en el catálogo...",
            )
        }
        if (query.isBlank()) {
            _uiState.update { it.copy(plantSearchResults = emptyList(), isSearchingPlants = false) }
            return
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.searchPlants(query.trim())
                _uiState.update {
                    it.copy(
                        plantSearchResults = response.results.map(::toSearchItem),
                        isSearchingPlants = false,
                        statusLogMessage = if (response.results.isEmpty()) {
                            "No encontramos especies para esa búsqueda"
                        } else {
                            "${response.results.size} especies encontradas"
                        },
                    )
                }
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                markApiError("No se pudo consultar el catálogo botánico", error)
                _uiState.update { it.copy(isSearchingPlants = false, plantSearchResults = emptyList()) }
            } catch (error: Exception) {
                markApiError("No se pudo consultar el catálogo botánico", error)
                _uiState.update { it.copy(isSearchingPlants = false, plantSearchResults = emptyList()) }
            }
        }
    }

    fun addPlant(result: PlantSearchResultItem, preferredModuleId: Int? = null) {
        _uiState.update { it.copy(addingPlantId = result.apiSpeciesId) }
        viewModelScope.launch {
            try {
                val plantName = (result.commonName ?: result.scientificName ?: result.apiSpeciesId).ifBlank { "Planta" }
                val speciesId = result.apiSpeciesId.ifBlank { "1" }

                // 1. Obtener módulos y plantas actuales para identificar espacio libre
                val modules = try { RetrofitClient.api.getModules().results } catch (_: Exception) { emptyList() }
                val existingPlants = try { RetrofitClient.api.getUserPlants().results } catch (_: Exception) { emptyList() }

                val assignedModuleIds = existingPlants.map { it.module }.toSet()
                val freeModule = modules.firstOrNull { it.id !in assignedModuleIds || it.plant == null }

                val targetModuleId: Int? = preferredModuleId ?: freeModule?.id

                var createSuccess = false
                try {
                    RetrofitClient.api.createUserPlant(
                        CreateUserPlantRequest(
                            module = targetModuleId,
                            apiSpeciesId = speciesId,
                            apodo = plantName,
                            tempMin = result.tempMin ?: 18f,
                            tempMax = result.tempMax ?: 35f,
                            optimalHumidity = result.humidityMin ?: 60f,
                        )
                    )
                    createSuccess = true
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401) {
                        handleSessionExpired()
                        return@launch
                    }
                    if (e.code() == 400) {
                        // Intento A: Probar con module = null (sin vincular inmediatamente a un módulo ocupado)
                        try {
                            RetrofitClient.api.createUserPlant(
                                CreateUserPlantRequest(
                                    module = null,
                                    apiSpeciesId = speciesId,
                                    apodo = plantName,
                                    tempMin = result.tempMin ?: 18f,
                                    tempMax = result.tempMax ?: 35f,
                                    optimalHumidity = result.humidityMin ?: 60f,
                                )
                            )
                            createSuccess = true
                        } catch (e2: retrofit2.HttpException) {
                            if (e2.code() == 401) {
                                handleSessionExpired()
                                return@launch
                            }
                            // Intento B: Si requiere módulo y todos están ocupados, reemplaza la planta vieja del módulo objetivo
                            val moduleToOverwrite = targetModuleId ?: modules.firstOrNull()?.id
                            if (moduleToOverwrite != null) {
                                val oldPlant = existingPlants.firstOrNull { it.module == moduleToOverwrite }
                                if (oldPlant != null) {
                                    RetrofitClient.api.deleteUserPlant(oldPlant.id)
                                }
                                RetrofitClient.api.createUserPlant(
                                    CreateUserPlantRequest(
                                        module = moduleToOverwrite,
                                        apiSpeciesId = speciesId,
                                        apodo = plantName,
                                        tempMin = result.tempMin ?: 18f,
                                        tempMax = result.tempMax ?: 35f,
                                        optimalHumidity = result.humidityMin ?: 60f,
                                    )
                                )
                                createSuccess = true
                            } else {
                                throw e2
                            }
                        }
                    } else {
                        throw e
                    }
                }

                if (createSuccess) {
                    _uiState.update {
                        it.copy(
                            plantSearchQuery = "",
                            plantSearchResults = emptyList(),
                            isSearchingPlants = false,
                            statusLogMessage = "¡Planta '$plantName' agregada exitosamente a tu jardín!",
                        )
                    }
                    refreshCloudState()
                }
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                val details = parseHttpError(error)
                _uiState.update {
                    it.copy(statusLogMessage = "No se pudo guardar la planta: $details")
                }
            } catch (error: Exception) {
                markApiError("No se pudo guardar la planta", error)
            } finally {
                _uiState.update { it.copy(addingPlantId = null) }
            }
        }
    }

    fun deletePlant(plant: CustomPlantProfile) {
        val id = plant.id.toIntOrNull() ?: return
        viewModelScope.launch {
            try {
                RetrofitClient.api.deleteUserPlant(id)
                refreshCloudState()
            } catch (error: retrofit2.HttpException) {
                if (error.code() == 401) {
                    handleSessionExpired()
                    return@launch
                }
                markApiError("No se pudo eliminar la planta", error)
            } catch (error: Exception) {
                markApiError("No se pudo eliminar la planta", error)
            }
        }
    }

    private fun applyTelemetry(reading: TelemetryDto, latency: Int) {
        OfflineCacheManager.saveTelemetry(reading)
        _uiState.update {
            it.copy(
                temperatureCelsius = reading.temperatura ?: it.temperatureCelsius,
                soilMoisturePercent = (reading.humedad ?: it.soilMoisturePercent.toFloat()).toInt().coerceIn(0, 100),
                sunlightPercent = (reading.luz ?: it.sunlightPercent.toFloat()).toInt().coerceIn(0, 100),
                isRealtimeConnected = true,
                syncLatencyMs = latency,
                lastSyncSource = "API Django",
                statusLogMessage = "Telemetría actualizada",
            )
        }
    }

    private fun toProfile(plant: UserPlantDto) = CustomPlantProfile(
        id = plant.id.toString(),
        apodo = plant.apodo,
        commonName = plant.apodo,
        tempMin = plant.tempMin,
        tempMax = plant.tempMax,
        minMoistureThreshold = plant.optimalHumidity.toInt(),
        moduleId = plant.module,
    )

    private fun toSearchItem(plant: PlantSearchDto) = PlantSearchResultItem(
        apiSpeciesId = plant.apiSpeciesId,
        commonName = plant.commonName,
        scientificName = plant.scientificName,
        watering = plant.watering,
        sunlight = plant.sunlight,
        thumbnail = plant.thumbnail,
        tempMin = plant.tempMin,
        tempMax = plant.tempMax,
        humidityMin = plant.humidityMin,
    )

    private fun markApiError(prefix: String, error: Exception) {
        _uiState.update {
            it.copy(
                isRealtimeConnected = false,
                statusLogMessage = "$prefix: ${error.message ?: "error de red"}",
            )
        }
    }
}
