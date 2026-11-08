package com.example.smartgarden

import android.content.Context
import androidx.lifecycle.ViewModel
<<<<<<< HEAD
import com.example.smartgarden.network.NetworkClient
import com.example.smartgarden.network.models.LoginRequest
import com.example.smartgarden.network.models.PlantSearchResultItem
import com.example.smartgarden.network.models.UserPlantDto
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
=======
import com.example.smartgarden.network.LoginRequest
import com.example.smartgarden.network.RetrofitClient
import com.example.smartgarden.network.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
>>>>>>> 65f5faea3dfb8dfec4e605c3b000c574d388957f
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Categorías botánicas para los filtros tipo "Chips" del Dashboard.
 */
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

/**
<<<<<<< HEAD
 * Perfil botánico dinámico de cultivo (compatible con la API Django REST y Perenual).
 */
data class CustomPlantProfile(
    val id: String,
    val apodo: String,
    val commonName: String = "",
    val tempMin: Float = 18f,
    val tempMax: Float = 35f,
    val minMoistureThreshold: Int = 30,
)

/**
 * Estado global inmutable de FloraSense sincronizado con Firebase Auth y Firebase Realtime Database.
=======
 * Estado global inmutable de FloraSense.
>>>>>>> 65f5faea3dfb8dfec4e605c3b000c574d388957f
 */
data class FloraSenseUiState(
    // Estado de Autenticación
    val isAuthenticated: Boolean = false,
    val isAuthenticating: Boolean = false,
    val userEmail: String = "",
    val authError: String? = null,
    val isDarkMode: Boolean = false,
    val isoSecurityProtocol: String = "ISO/IEC 27400 • TLS 1.3 • AES-256-GCM",

    // Sincronización Multi-Dispositivo
    val connectedDevicesCount: Int = 2,
    val isRealtimeConnected: Boolean = false,
    val lastSyncSource: String = "Django API",
    val syncLatencyMs: Int = 22,

    // Plantas y Cultivos Dinámicos (API Django REST + Perenual)
    val userPlants: List<CustomPlantProfile> = listOf(
        CustomPlantProfile(id = "todas", apodo = "Todas", tempMin = 18f, tempMax = 35f, minMoistureThreshold = 30),
        CustomPlantProfile(id = "tropicales", apodo = "Tropicales", tempMin = 22f, tempMax = 32f, minMoistureThreshold = 35),
        CustomPlantProfile(id = "suculentas", apodo = "Suculentas", tempMin = 18f, tempMax = 38f, minMoistureThreshold = 20),
        CustomPlantProfile(id = "aromaticas", apodo = "Aromáticas", tempMin = 20f, tempMax = 30f, minMoistureThreshold = 30),
    ),
    val activePlant: CustomPlantProfile = CustomPlantProfile(id = "todas", apodo = "Todas", tempMin = 18f, tempMax = 35f, minMoistureThreshold = 30),
    val isAddPlantDialogVisible: Boolean = false,
    val plantToDelete: CustomPlantProfile? = null,
    val plantSearchQuery: String = "",
    val plantSearchResults: List<PlantSearchResultItem> = emptyList(),
    val isSearchingPlants: Boolean = false,

    // Filtro de categoría de planta activo en el Dashboard
    val selectedCategory: PlantCategory = PlantCategory.TODAS,

    // Telemetría Ambiental en Tiempo Real (Trama T,L,H)
    val temperatureCelsius: Float = 24.0f,
    val soilMoisturePercent: Int = 50,
    val sunlightPercent: Int = 78,

    // Estado de Actuadores y Dosificación (Panel de Control)
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
        // Verifica si ya existe una sesión guardada con Django
        val savedToken = TokenManager.getToken()
        if (!savedToken.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    userEmail = "Usuario Autenticado",
                )
            }
        }

        // Mecanismo de Polling (Sondeo automático) para simular tiempo real
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isAuthenticated) {
                    fetchTelemetryFromDjango()
                }
                delay(5000L) // Espera 5 segundos antes de la siguiente iteración
            }
        }
    }

    // ========================================================================
<<<<<<< HEAD
    // PASO 2: AUTENTICACIÓN HÍBRIDA (DJANGO REST JWT + FIREBASE AUTH)
    // ========================================================================
    fun loginWithDjango(
        context: Context,
        username: String,
        password: String,
        baseUrl: String? = null,
        onLoginSuccess: () -> Unit,
    ) {
        val cleanUser = username.trim()
        if (cleanUser.isEmpty()) {
            _uiState.update { it.copy(authError = "Ingresa un usuario válido.") }
            return
        }

        _uiState.update { it.copy(isAuthenticating = true, authError = null) }

        viewModelScope.launch {
            try {
                val api = NetworkClient.getApiService(context, baseUrl)
                val response = api.login(LoginRequest(cleanUser, password))
                if (response.isSuccessful && response.body() != null) {
                    val tokens = response.body()!!
                    NetworkClient.saveTokens(context, tokens.access, tokens.refresh)
                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isAuthenticating = false,
                            userEmail = cleanUser,
                            authError = null,
                            statusLogMessage = "Sesión Django JWT activa ($cleanUser)",
                        )
                    }
                    onLoginSuccess()
                } else {
                    // Fallback a Firebase Auth si Django devuelve error de credenciales
                    loginWithFirebase(cleanUser, password, onLoginSuccess)
                }
            } catch (e: Exception) {
                // Fallback a Firebase Auth si el servidor local de Django no está encendido
                loginWithFirebase(cleanUser, password, onLoginSuccess)
=======
    // TELEMETRÍA (LECTURA DESDE DJANGO)
    // ========================================================================
    fun fetchTelemetryFromDjango() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getTelemetry()

                // Mapea la respuesta según la estructura de tu JSON
                // Por ejemplo, si tienes data asumiendo que contiene temperature y moisture:
                // (Modificar según la estructura final de TelemetryResponse)

                _uiState.update { current ->
                    current.copy(
                        isRealtimeConnected = true,
                        // Aquí asumimos valores por defecto hasta ajustar el data class real en base al JSON de Django
                        // temperatureCelsius = response.data.temperature ?: current.temperatureCelsius,
                        // soilMoisturePercent = response.data.moisture ?: current.soilMoisturePercent,
                        statusLogMessage = "Telemetría actualizada desde Django",
                        lastSyncSource = "Django REST API"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isRealtimeConnected = false,
                        statusLogMessage = "Error de sincronización con Django: ${e.localizedMessage}"
                    )
                }
>>>>>>> 65f5faea3dfb8dfec4e605c3b000c574d388957f
            }
        }
    }

<<<<<<< HEAD
    fun loginWithFirebase(
        email: String,
=======
    // ========================================================================
    // PASO 2: AUTENTICACIÓN REAL CON API REST EN DJANGO
    // ========================================================================
    fun loginWithDjango(
        username: String,
>>>>>>> 65f5faea3dfb8dfec4e605c3b000c574d388957f
        password: String,
        onLoginSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanUsername = username.trim()
        if (cleanUsername.isEmpty()) {
            _uiState.update {
                it.copy(authError = "Ingresa un usuario o correo electrónico válido.")
            }
            return
        }
        if (password.length < 6) {
            _uiState.update {
                it.copy(authError = "Política ISO 27400: La contraseña requiere mínimo 6 caracteres.")
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
                            statusLogMessage = "Sesión Django activa ($cleanUsername)",
                        )
                    }
                    onLoginSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            authError = "El servidor no devolvió un token de acceso válido."
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
                statusLogMessage = "Sesión cerrada",
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
<<<<<<< HEAD
    // BÚSQUEDA Y GESTIÓN DE PLANTAS DINÁMICAS (API DJANGO REST + PERENUAL)
    // ========================================================================
    fun selectActivePlant(plant: CustomPlantProfile) {
        _uiState.update { current ->
            current.copy(
                activePlant = plant,
                statusLogMessage = "Cultivo activo: ${plant.apodo} (${plant.tempMin}°C - ${plant.tempMax}°C)",
            )
        }
    }

    fun setAddPlantDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isAddPlantDialogVisible = visible) }
    }

    fun showDeletePlantConfirmation(plant: CustomPlantProfile?) {
        _uiState.update { it.copy(plantToDelete = plant) }
    }

    fun deletePlant(context: Context, plant: CustomPlantProfile) {
        _uiState.update { current ->
            val updatedList = current.userPlants.filterNot { it.id == plant.id }
            val fallbackPlant = updatedList.firstOrNull() ?: CustomPlantProfile(id = "todas", apodo = "Todas", tempMin = 18f, tempMax = 35f, minMoistureThreshold = 30)
            val newActive = if (current.activePlant.id == plant.id) fallbackPlant else current.activePlant

            current.copy(
                userPlants = updatedList,
                activePlant = newActive,
                plantToDelete = null,
                statusLogMessage = "Cultivo eliminado: ${plant.apodo}",
            )
        }

        viewModelScope.launch {
            try {
                val plantIdInt = plant.id.toIntOrNull()
                if (plantIdInt != null) {
                    val api = NetworkClient.getApiService(context)
                    api.deleteUserPlant(plantIdInt)
                }
            } catch (_: Exception) {}
        }
    }

private val fallbackBotanicalCatalog = listOf(
    // Árboles Frutales en Maceta / Plantón (Chile)
    PlantSearchResultItem(
        apiSpeciesId = "limonero",
        commonName = "Limonero / Limón de Pica en Maceta",
        scientificName = "Citrus limon",
        watering = "Frecuente",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "naranjo",
        commonName = "Naranjo de Mesa en Maceta",
        scientificName = "Citrus sinensis",
        watering = "Frecuente",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "palto",
        commonName = "Palto / Aguacate Hass en Maceta",
        scientificName = "Persea americana",
        watering = "Moderado",
        sunlight = listOf("Sol directo / Protegido de heladas"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "olivo",
        commonName = "Olivo de Azapa en Maceta",
        scientificName = "Olea europaea",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "higuera",
        commonName = "Higuera de Breva en Maceta",
        scientificName = "Ficus carica",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "manzano",
        commonName = "Manzano Criollo en Maceta",
        scientificName = "Malus domestica",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cerezo",
        commonName = "Cerezo / Cerezal en Maceta",
        scientificName = "Prunus avium",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "duraznero",
        commonName = "Duraznero / Nectarín en Maceta",
        scientificName = "Prunus persica",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "arandano",
        commonName = "Arándano / Blueberry en Maceta",
        scientificName = "Vaccinium corymbosum",
        watering = "Frecuente (Sustrato Ácido)",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "frambueso",
        commonName = "Frambueso / Frambuesa en Maceta",
        scientificName = "Rubus idaeus",
        watering = "Frecuente",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "frutilla",
        commonName = "Frutilla / Fresa de Huerta en Maceta",
        scientificName = "Fragaria × ananassa",
        watering = "Frecuente",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "parra",
        commonName = "Vid / Parra de Uva en Maceta",
        scientificName = "Vitis vinifera",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "granado",
        commonName = "Granado en Maceta",
        scientificName = "Punica granatum",
        watering = "Bajo / Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "chirimoyo",
        commonName = "Chirimoyo en Maceta",
        scientificName = "Annona cherimola",
        watering = "Moderado",
        sunlight = listOf("Luz brillante protegida"),
        thumbnail = null,
    ),

    // Plantas Ornamentales y de Interior Comunes en Chile
    PlantSearchResultItem(
        apiSpeciesId = "sansevieria",
        commonName = "Sansevieria / Lengua de Suegra (Snake Plant)",
        scientificName = "Sansevieria trifasciata",
        watering = "Bajo",
        sunlight = listOf("Luz baja / Sombra"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "monstera",
        commonName = "Monstera Deliciosa (Costilla de Adán)",
        scientificName = "Monstera deliciosa",
        watering = "Moderado",
        sunlight = listOf("Sombra parcial"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "pothos",
        commonName = "Pothos / Potus (Teléfono)",
        scientificName = "Epipremnum aureum",
        watering = "Moderado",
        sunlight = listOf("Luz indirecta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "gomero",
        commonName = "Gomero / Ficus Elástica",
        scientificName = "Ficus elastica",
        watering = "Moderado",
        sunlight = listOf("Luz brillante indirecta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "ficus_lyrata",
        commonName = "Ficus Lyrata / Higuera de Hoja de Violín",
        scientificName = "Ficus lyrata",
        watering = "Moderado",
        sunlight = listOf("Luz brillante"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cradle_moses",
        commonName = "Cuna de Moisés / Lirio de la Paz",
        scientificName = "Spathiphyllum wallisii",
        watering = "Frecuente",
        sunlight = listOf("Sombra / Luz indirecta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "aloe",
        commonName = "Aloe Vera / Sábila",
        scientificName = "Aloe barbadensis",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "suculenta",
        commonName = "Suculenta Echeveria / Crasa",
        scientificName = "Echeveria spp.",
        watering = "Bajo",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cactus",
        commonName = "Cactus de Desierto / Asiento de Suegra",
        scientificName = "Cactaceae spp.",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "helecho",
        commonName = "Helecho Rizado (Boston Fern)",
        scientificName = "Nephrolepis exaltata",
        watering = "Frecuente",
        sunlight = listOf("Sombra / Humedad alta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "orquidea",
        commonName = "Orquídea Phalaenopsis",
        scientificName = "Phalaenopsis spp.",
        watering = "Moderado",
        sunlight = listOf("Luz indirecta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cinta",
        commonName = "Cinta / Lazo de Amor (Mala Madre)",
        scientificName = "Chlorophytum comosum",
        watering = "Moderado",
        sunlight = listOf("Luz indirecta"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "jade",
        commonName = "Planta del Jade / Árbol del Dinero",
        scientificName = "Crassula ovata",
        watering = "Bajo",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "geranio",
        commonName = "Geranio / Cardenal de Jardín",
        scientificName = "Pelargonium hortorum",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "hortensia",
        commonName = "Hortensia de Jardín",
        scientificName = "Hydrangea macrophylla",
        watering = "Frecuente",
        sunlight = listOf("Sombra parcial"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "copihue",
        commonName = "Copihue (Flor Nacional de Chile)",
        scientificName = "Lapageria rosea",
        watering = "Frecuente (Tierra Ácida)",
        sunlight = listOf("Sombra / Subbosque"),
        thumbnail = null,
    ),

    // Hierbas, Hortalizas y Aromáticas en Chile
    PlantSearchResultItem(
        apiSpeciesId = "albahaca",
        commonName = "Albahaca Aromática",
        scientificName = "Ocimum basilicum",
        watering = "Frecuente",
        sunlight = listOf("Sol medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "menta",
        commonName = "Menta / Hierbabuena",
        scientificName = "Mentha spicata",
        watering = "Frecuente",
        sunlight = listOf("Sombra parcial"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "romero",
        commonName = "Romero Aromático",
        scientificName = "Salvia rosmarinus",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "oregano",
        commonName = "Orégano de Huerta",
        scientificName = "Origanum vulgare",
        watering = "Bajo / Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cilantro",
        commonName = "Cilantro de Huerta",
        scientificName = "Coriandrum sativum",
        watering = "Frecuente",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "perejil",
        commonName = "Perejil de Huerta",
        scientificName = "Petroselinum crispum",
        watering = "Frecuente",
        sunlight = listOf("Sol directo / Medio"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "tomillo",
        commonName = "Tomillo Aromático",
        scientificName = "Thymus vulgaris",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "cedron",
        commonName = "Cedrón / Hierba Luisa",
        scientificName = "Aloysia citrodora",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "ruda",
        commonName = "Ruda Medicinal",
        scientificName = "Ruta graveolens",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "aji",
        commonName = "Ají Cacho de Cabra / Ají Verde",
        scientificName = "Capsicum annuum",
        watering = "Moderado",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    ),
    PlantSearchResultItem(
        apiSpeciesId = "lavanda",
        commonName = "Lavanda Aromática",
        scientificName = "Lavandula angustifolia",
        watering = "Bajo",
        sunlight = listOf("Sol directo"),
        thumbnail = null,
    )
)

    fun searchPlantsApi(context: Context, query: String) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            _uiState.update {
                it.copy(
                    isSearchingPlants = false,
                    plantSearchResults = fallbackBotanicalCatalog.take(6),
                )
            }
            return
        }

        _uiState.update { it.copy(isSearchingPlants = true, plantSearchQuery = cleanQuery) }

        viewModelScope.launch {
            try {
                val api = NetworkClient.getApiService(context)
                val response = withTimeoutOrNull(3500) {
                    api.searchPlants(cleanQuery)
                }

                if (response != null && response.isSuccessful && response.body() != null && response.body()!!.results.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isSearchingPlants = false,
                            plantSearchResults = response.body()!!.results,
                        )
                    }
                } else {
                    val localMatches = fallbackBotanicalCatalog.filter {
                        it.commonName?.contains(cleanQuery, ignoreCase = true) == true ||
                        it.scientificName?.contains(cleanQuery, ignoreCase = true) == true ||
                        it.apiSpeciesId.contains(cleanQuery, ignoreCase = true)
                    }
                    val resultsToUse = if (localMatches.isNotEmpty()) {
                        localMatches
                    } else {
                        val formattedTitle = cleanQuery.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        listOf(
                            PlantSearchResultItem(
                                apiSpeciesId = cleanQuery.lowercase().replace(" ", "_"),
                                commonName = "$formattedTitle (Especie / Cultivo en Maceta)",
                                scientificName = "Especie $formattedTitle",
                                watering = "Moderado",
                                sunlight = listOf("Luz directa / media"),
                                thumbnail = null,
                            )
                        ) + fallbackBotanicalCatalog.take(5)
                    }
                    _uiState.update {
                        it.copy(
                            isSearchingPlants = false,
                            plantSearchResults = resultsToUse,
                        )
                    }
                }
            } catch (_: Exception) {
                val localMatches = fallbackBotanicalCatalog.filter {
                    it.commonName?.contains(cleanQuery, ignoreCase = true) == true ||
                    it.scientificName?.contains(cleanQuery, ignoreCase = true) == true ||
                    it.apiSpeciesId.contains(cleanQuery, ignoreCase = true)
                }
                val resultsToUse = if (localMatches.isNotEmpty()) {
                    localMatches
                } else {
                    val formattedTitle = cleanQuery.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    listOf(
                        PlantSearchResultItem(
                            apiSpeciesId = cleanQuery.lowercase().replace(" ", "_"),
                            commonName = "$formattedTitle (Especie / Cultivo en Maceta)",
                            scientificName = "Especie $formattedTitle",
                            watering = "Moderado",
                            sunlight = listOf("Luz directa / media"),
                            thumbnail = null,
                        )
                    ) + fallbackBotanicalCatalog.take(5)
                }
                _uiState.update {
                    it.copy(
                        isSearchingPlants = false,
                        plantSearchResults = resultsToUse,
                    )
                }
            }
        }
    }

    fun addNewPlant(
        context: Context,
        apodo: String,
        speciesName: String? = null,
        tempMin: Float = 18f,
        tempMax: Float = 35f,
        humedadOptima: Int = 30,
    ) {
        val newPlant = CustomPlantProfile(
            id = System.currentTimeMillis().toString(),
            apodo = apodo.ifEmpty { "Nuevo Cultivo" },
            commonName = speciesName ?: "Especie Botánica",
            tempMin = tempMin,
            tempMax = tempMax,
            minMoistureThreshold = humedadOptima,
        )

        _uiState.update { current ->
            val updatedList = current.userPlants + newPlant
            current.copy(
                userPlants = updatedList,
                activePlant = newPlant,
                isAddPlantDialogVisible = false,
                statusLogMessage = "Planta agregada: ${newPlant.apodo}",
            )
        }

        viewModelScope.launch {
            try {
                val api = NetworkClient.getApiService(context)
                api.createUserPlant(
                    UserPlantDto(
                        module = 1,
                        apiSpeciesId = speciesName,
                        apodo = apodo,
                        tempMin = tempMin,
                        tempMax = tempMax,
                        humedadOptima = humedadOptima,
                    )
                )
            } catch (_: Exception) {}
        }
    }

    // ========================================================================
    // FUNCIONES COMPLEMENTARIAS DEL DASHBOARD (ESCRITURA RTDB PARA PRUEBAS)
=======
    // FUNCIONES COMPLEMENTARIAS DEL DASHBOARD
>>>>>>> 65f5faea3dfb8dfec4e605c3b000c574d388957f
    // ========================================================================
    fun selectPlantCategory(category: PlantCategory) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                statusLogMessage = "Perfil botánico: ${category.displayName} (Óptimo ${category.optimalTempRange})",
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
