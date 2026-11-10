package com.example.smartgarden

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: FloraSenseUiState,
    onSelectCategory: (PlantCategory) -> Unit,
    onSelectPlant: (CustomPlantProfile) -> Unit,
    onEnsureModule: () -> Unit,
    onSearchPlants: (String) -> Unit,
    onAddPlant: (PlantSearchResultItem) -> Unit,
    onDeletePlant: (CustomPlantProfile) -> Unit,
    onUpdateSimulatedTelemetry: (Float, Int, Int) -> Unit,
    onSimulateSecondDevice: () -> Unit,
    onNavigateToControls: () -> Unit,
    onLogout: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    var showSimulator by remember { mutableStateOf(false) }
    var plantPendingDelete by remember { mutableStateOf<CustomPlantProfile?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlWarmBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DashboardHeader(
            userEmail = uiState.userEmail,
            connected = uiState.isRealtimeConnected,
            onRefresh = onSimulateSecondDevice,
            onToggleDarkMode = onToggleDarkMode,
            onLogout = onLogout,
            isDarkMode = uiState.isDarkMode,
        )

        StatusBanner(message = uiState.statusLogMessage)

        if (uiState.hasCriticalAlert) {
            AlertSummary(uiState = uiState, onOpenControls = onNavigateToControls)
        }

        SectionTitle(
            title = "Resumen del jardín",
            subtitle = "${uiState.userPlants.size} macetas • ${uiState.lastSyncSource}",
        )
        SummaryCard(uiState = uiState, onOpenControls = onNavigateToControls)

        ExpertWebPanelCard()

        PlantFleetSection(
            uiState = uiState,
            onSelectPlant = onSelectPlant,
            onEnsureModule = onEnsureModule,
            onSearch = onSearchPlants,
            onAddPlant = onAddPlant,
            onDeletePlant = { plantPendingDelete = it },
        )

        SectionTitle(
            title = "Telemetría",
            subtitle = uiState.activePlant?.let {
                "${it.apodo} • ${it.tempMin}°C - ${it.tempMax}°C • humedad mínima ${it.minMoistureThreshold}%"
            } ?: "Selecciona una maceta para ver sus parámetros",
        )
        TelemetryGrid(uiState = uiState)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = showSimulator,
                onClick = { showSimulator = !showSimulator },
                label = { Text(if (showSimulator) "Ocultar simulador" else "Simular lectura") },
                leadingIcon = {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = WarmSurfaceContainer,
                    selectedContainerColor = EmeraldSoftBg,
                    labelColor = CharcoalText,
                    selectedLabelColor = CharcoalText,
                ),
            )
            Button(
                onClick = onNavigateToControls,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldActive,
                    contentColor = CharcoalText,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Controles", fontWeight = FontWeight.Bold)
            }
        }

        AnimatedVisibility(showSimulator) {
            TelemetrySimulator(
                uiState = uiState,
                onUpdate = onUpdateSimulatedTelemetry,
            )
        }

        Text(
            text = uiState.statusLogMessage,
            style = MaterialTheme.typography.labelSmall,
            color = CharcoalSubtle,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    plantPendingDelete?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantPendingDelete = null },
            title = { Text("Eliminar maceta") },
            text = {
                Text("¿Seguro que deseas eliminar \"${plant.apodo}\"? También se eliminarán sus parámetros asociados.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlant(plant)
                        plantPendingDelete = null
                    },
                ) {
                    Text("Eliminar", color = EmergencyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantPendingDelete = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun DashboardHeader(
    userEmail: String,
    connected: Boolean,
    onRefresh: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Hola, ${userEmail.ifBlank { "operador" }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape),
                    color = if (connected) EmeraldActive else EmergencyRed,
                ) {}
                Text(
                    text = if (connected) "API conectada" else "Sin conexión",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )
            }
        }
        IconButton(
            onClick = {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://smart-garden-web-react.vercel.app/")
                    )
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Abrir Panel Experto Web en navegador",
                tint = CharcoalText,
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = CharcoalText)
        }
        IconButton(onClick = onToggleDarkMode) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                contentDescription = "Cambiar tema",
                tint = CharcoalText,
            )
        }
        IconButton(onClick = onLogout) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión", tint = CharcoalSubtle)
        }
    }
}

@Composable
private fun ExpertWebPanelCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
        border = BorderStroke(1.dp, CharcoalText.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldSoftBg,
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Panel Experto Web",
                        tint = EmeraldActive,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Panel Experto Web",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                    )
                    Text(
                        text = "Gemelo Digital 3D y analítica histórica",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalSubtle,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://smart-garden-web-react.vercel.app/")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldActive,
                    contentColor = CharcoalText,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Abrir ↗",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
    }
}

@Composable
private fun SummaryCard(uiState: FloraSenseUiState, onOpenControls: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldSoftBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = EmeraldActive,
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = CharcoalText, modifier = Modifier.padding(11.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    if (uiState.hasCriticalAlert) "Requiere atención" else "Jardín en buen estado",
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (uiState.hasCriticalAlert) "Revisa las alertas activas." else "Tus plantas están siendo monitoreadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalSubtle,
                )
            }
            TextButton(onClick = onOpenControls) { Text("Ver") }
        }
    }
}

@Composable
private fun AlertSummary(uiState: FloraSenseUiState, onOpenControls: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencySoftBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = EmergencyRed)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text("Alerta ambiental", fontWeight = FontWeight.Bold, color = EmergencyRed)
                Text(
                    when {
                        uiState.isEmergencyStopActive -> "La parada de emergencia está activa."
                        uiState.isCriticalHeat -> "Temperatura sobre el rango recomendado."
                        else -> "La humedad del suelo está bajo el mínimo."
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onOpenControls) { Text("Revisar") }
        }
    }
}

@Composable
private fun PlantFleetSection(
    uiState: FloraSenseUiState,
    onSelectPlant: (CustomPlantProfile) -> Unit,
    onEnsureModule: () -> Unit,
    onSearch: (String) -> Unit,
    onAddPlant: (PlantSearchResultItem) -> Unit,
    onDeletePlant: (CustomPlantProfile) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Mis macetas", "Gestiona las plantas vinculadas a tu cuenta")
        if (uiState.moduleIds.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = EmergencySoftBg,
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("No hay ningún módulo asociado a esta cuenta.", fontWeight = FontWeight.Bold)
                    Text(
                        "Crea una maceta virtual para poder agregar plantas y probar la aplicación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalSubtle,
                    )
                    Button(
                        onClick = onEnsureModule,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldActive,
                            contentColor = CharcoalText,
                        ),
                    ) {
                        Text("Crear maceta")
                    }
                }
            }
        }
        if (uiState.userPlants.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = WarmSurfaceContainer,
            ) {
                Text("Todavía no tienes macetas registradas.", modifier = Modifier.padding(14.dp), color = CharcoalSubtle)
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                uiState.userPlants.forEach { plant ->
                    PlantFleetCard(
                        plant = plant,
                        selected = plant.id == uiState.activePlant?.id,
                        onSelect = { onSelectPlant(plant) },
                        onDelete = { onDeletePlant(plant) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.plantSearchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.isSearchingPlants) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = EmeraldActive,
                    )
                } else if (uiState.plantSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = CharcoalSubtle)
                    }
                }
            },
            label = { Text("Buscar especie") },
            placeholder = { Text("Palto, romero, hortensia...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldActive,
                unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(14.dp),
        )

        if (uiState.isSearchingPlants) {
            Text("Buscando en el catálogo...", style = MaterialTheme.typography.bodySmall, color = CharcoalSubtle)
        } else {
            uiState.plantSearchResults.take(4).forEach { result ->
                SearchPlantRow(
                    result = result,
                    enabled = uiState.moduleIds.isNotEmpty(),
                    isAdding = uiState.addingPlantId == result.apiSpeciesId,
                    onAdd = { onAddPlant(result) },
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(message: String) {
    if (message.isBlank()) return
    val isSuccess = message.contains("agregada", ignoreCase = true) ||
        message.contains("éxito", ignoreCase = true) ||
        message.contains("exitosamente", ignoreCase = true) ||
        message.contains("registrada", ignoreCase = true)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = if (isSuccess) EmeraldSoftBg else WarmSurfaceContainer,
        border = BorderStroke(1.dp, if (isSuccess) EmeraldActive.copy(alpha = 0.6f) else CharcoalSubtle.copy(alpha = 0.15f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isSuccess) EmeraldActive else CharcoalSubtle,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSuccess) FontWeight.Bold else FontWeight.Medium,
                color = CharcoalText,
            )
        }
    }
}

@Composable
private fun PlantFleetCard(
    plant: CustomPlantProfile,
    selected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(190.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EmeraldSoftBg else WarmSurfaceContainer,
        onClick = onSelect,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = EmeraldActive, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(plant.apodo, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar maceta", tint = EmergencyRed, modifier = Modifier.size(18.dp))
                }
            }
            Text("${plant.tempMin}°C - ${plant.tempMax}°C", style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
            Text("Humedad mínima: ${plant.minMoistureThreshold}%", style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
            Text(if (selected) "Planta activa" else "Tocar para seleccionar", style = MaterialTheme.typography.labelSmall, color = EmeraldActive)
        }
    }
}

@Composable
private fun SearchPlantRow(
    result: PlantSearchResultItem,
    enabled: Boolean,
    isAdding: Boolean,
    onAdd: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = WarmSurfaceHigh) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.commonName ?: result.apiSpeciesId, fontWeight = FontWeight.Bold)
                result.scientificName?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle) }
            }
            Button(
                onClick = onAdd,
                enabled = enabled && !isAdding,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive, contentColor = CharcoalText),
            ) {
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CharcoalText)
                    Spacer(Modifier.width(4.dp))
                    Text("Agregando...")
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (enabled) "Agregar" else "Sin módulo")
                }
            }
        }
    }
}

@Composable
private fun TelemetryGrid(uiState: FloraSenseUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TelemetryCard(
            modifier = Modifier.weight(1f),
            title = "Temperatura",
            value = String.format(Locale.US, "%.1f°C", uiState.temperatureCelsius),
            icon = Icons.Default.Thermostat,
            color = PastelOrangeTemp,
            accent = PastelOrangeAccent,
        )
        TelemetryCard(
            modifier = Modifier.weight(1f),
            title = "Humedad",
            value = "${uiState.soilMoisturePercent}%",
            icon = Icons.Default.WaterDrop,
            color = PastelBlueMoisture,
            accent = PastelBlueAccent,
        )
        TelemetryCard(
            modifier = Modifier.weight(1f),
            title = "Luz",
            value = "${uiState.sunlightPercent}%",
            icon = Icons.Default.WbSunny,
            color = PastelYellowSolar,
            accent = PastelYellowAccent,
        )
    }
}

@Composable
private fun TelemetryCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    accent: Color,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = color) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(title, style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle, maxLines = 1)
        }
    }
}

@Composable
private fun TelemetrySimulator(
    uiState: FloraSenseUiState,
    onUpdate: (Float, Int, Int) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = WarmSurfaceContainer) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Simulación local", fontWeight = FontWeight.Bold)
            Text(
                "Los cambios se guardan en la API para ${uiState.activePlant?.apodo ?: "la maceta seleccionada"}.",
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalSubtle,
            )
            Text("Temperatura: ${String.format(Locale.US, "%.1f", uiState.temperatureCelsius)}°C", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = uiState.temperatureCelsius,
                onValueChange = { onUpdate(it, uiState.soilMoisturePercent, uiState.sunlightPercent) },
                valueRange = 0f..45f,
            )
            Text("Humedad: ${uiState.soilMoisturePercent}%", style = MaterialTheme.typography.labelSmall)
            Slider(
                value = uiState.soilMoisturePercent.toFloat(),
                onValueChange = { onUpdate(uiState.temperatureCelsius, it.toInt(), uiState.sunlightPercent) },
                valueRange = 0f..100f,
            )
        }
    }
}
