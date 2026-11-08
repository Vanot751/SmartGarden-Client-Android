package com.example.smartgarden

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.smartgarden.network.models.PlantSearchResultItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Pantalla 2: Dashboard de Telemetría Ambiental con Chips de categorías, 3 tarjetas pastel,
 * lógica de alerta (< 30% humedad o > 35°C calor) y FAB hacia Controles Manuales.
 */
@Composable
fun DashboardScreen(
    uiState: FloraSenseUiState,
    onSelectCategory: (PlantCategory) -> Unit,
    onSelectActivePlant: (CustomPlantProfile) -> Unit,
    onShowAddPlantDialog: () -> Unit,
    onHideAddPlantDialog: () -> Unit,
    onSearchPlants: (Context, String) -> Unit,
    onAddNewPlant: (Context, String, String?, Float, Float, Int) -> Unit,
    onUpdateSimulatedTelemetry: (Float, Int, Int) -> Unit,
    onSimulateSecondDevice: () -> Unit,
    onNavigateToControls: () -> Unit,
    onLogout: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    var showSimulator by remember { mutableStateOf(false) }

    if (uiState.isAddPlantDialogVisible) {
        AddPlantDialog(
            uiState = uiState,
            onDismiss = onHideAddPlantDialog,
            onSearch = onSearchPlants,
            onAddPlant = onAddNewPlant,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PearlWarmBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PearlWarmBackground)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Cabecera del Dashboard y estado Firebase RTDB Multi-Dispositivo
            DashboardTopHeader(
                uiState = uiState,
                showSimulator = showSimulator,
                onToggleSimulator = { showSimulator = !showSimulator },
                onSimulateSecondDevice = onSimulateSecondDevice,
                onLogout = onLogout,
                onToggleDarkMode = onToggleDarkMode,
            )

            // Panel opcional para simular lecturas del Arduino UNO (TMP36, Higrómetro, LDR)
            AnimatedVisibility(visible = showSimulator) {
                DashboardTelemetrySimulator(
                    uiState = uiState,
                    onUpdateTelemetry = onUpdateSimulatedTelemetry,
                )
            }

            // ================================================================
            // FILTROS TIPO "CHIPS" PARA CULTIVOS Y CULTIVOS PERSONALIZADOS
            // ================================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CULTIVO Y PERFIL BOTÁNICO ACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Chip para Agregar Nueva Planta (+ Agregar Planta)
                    FilterChip(
                        selected = false,
                        onClick = onShowAddPlantDialog,
                        label = {
                            Text(
                                text = "+ Agregar Planta",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar Planta",
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = EmeraldSoftBg,
                            labelColor = CharcoalText,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = EmeraldActive,
                        ),
                    )

                    uiState.userPlants.forEach { plant ->
                        val isSelected = uiState.activePlant.id == plant.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectActivePlant(plant) },
                            label = {
                                Text(
                                    text = plant.apodo,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = WarmSurfaceContainer,
                                labelColor = CharcoalText,
                                selectedContainerColor = EmeraldActive,
                                selectedLabelColor = CharcoalText,
                                selectedLeadingIconColor = CharcoalText,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CharcoalText.copy(alpha = 0.1f),
                                selectedBorderColor = EmeraldActive,
                            ),
                        )
                    }
                }
            }

            // ================================================================
            // BANNER GLOBAL DE ALERTA (< 30% HUMEDAD O > 35°C TEMPERATURA)
            // ================================================================
            AnimatedVisibility(visible = uiState.isLowMoisture || uiState.isCriticalHeat || uiState.isEmergencyStopActive) {
                CriticalTelemetryAlertBanner(
                    uiState = uiState,
                    onGoToControls = onNavigateToControls,
                )
            }

            // ================================================================
            // 3 TARJETAS DE TELEMETRÍA EN COLORES PASTEL
            // ================================================================
            Text(
                text = "TELEMETRÍA EN TIEMPO REAL (${uiState.selectedCategory.displayName.uppercase()})",
                style = MaterialTheme.typography.labelSmall,
                color = CharcoalSubtle,
            )

            // 1. Tarjeta de Temperatura (Naranja Pastel)
            SensorTelemetryCard(
                title = "TEMPERATURA AMBIENTE",
                sensorBadge = "TMP36 • Pin A0",
                valueText = String.format(Locale.US, "%.1f°C", uiState.temperatureCelsius),
                backgroundColor = PastelOrangeTemp,
                accentColor = if (uiState.isCriticalHeat) EmergencyRed else PastelOrangeAccent,
                icon = Icons.Default.Thermostat,
                statusText = if (uiState.isCriticalHeat) {
                    "⚠ ALERTA CRÍTICA (> 35°C): Estrés térmico • Piezoeléctrico Pin D9 activo"
                } else {
                    "Rango óptimo para ${uiState.selectedCategory.displayName}: ${uiState.selectedCategory.optimalTempRange}"
                },
                isWarning = uiState.isCriticalHeat,
            )

            // 2. Tarjeta de Humedad del Suelo (Celeste Pastel)
            SensorTelemetryCard(
                title = "HUMEDAD DEL SUELO",
                sensorBadge = "Higrómetro • Pin A2",
                valueText = "${uiState.soilMoisturePercent}%",
                backgroundColor = PastelBlueMoisture,
                accentColor = PastelBlueAccent,
                icon = Icons.Default.WaterDrop,
                statusText = if (uiState.isLowMoisture) {
                    "⚠ Advertencia: Humedad crítica (< 30%) • Bomba Pin D8 requerida"
                } else {
                    "Humedad de sustrato en rango saludable (≥ 30%)"
                },
                isWarning = uiState.isLowMoisture,
            )

            // 3. Tarjeta de Luz Solar (Amarillo Pálido)
            SensorTelemetryCard(
                title = "LUZ SOLAR",
                sensorBadge = "LDR 10kΩ • Pin A1",
                valueText = "${uiState.sunlightPercent}%",
                backgroundColor = PastelYellowSolar,
                accentColor = PastelYellowAccent,
                icon = Icons.Default.WbSunny,
                statusText = "Radiación fotosintética activa • Fotoperíodo estable",
                isWarning = false,
            )

            // Estado de sincronización Firebase Realtime Database entre 2 dispositivos
            FirebaseSyncFooterCard(
                uiState = uiState,
                onSimulateSecondDevice = onSimulateSecondDevice,
            )

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun DashboardTopHeader(
    uiState: FloraSenseUiState,
    showSimulator: Boolean,
    onToggleSimulator: () -> Unit,
    onSimulateSecondDevice: () -> Unit,
    onLogout: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "SmartGarden",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column {
                    Text(
                        text = "SmartGarden",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = uiState.userEmail.ifEmpty { "operador@smartgarden.io" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalSubtle,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Botón Conmutador Sol/Luna para Modo Oscuro OLED
                IconButton(onClick = onToggleDarkMode) {
                    Icon(
                        imageVector = if (uiState.isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                        contentDescription = if (uiState.isDarkMode) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro OLED",
                        tint = if (uiState.isDarkMode) Color(0xFFFACC15) else CharcoalText,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleSimulator() },
                    color = if (showSimulator) EmeraldActive else WarmSurfaceContainer,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Simular sensores",
                            tint = CharcoalText,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Simular",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalText,
                        )
                    }
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = CharcoalText,
                    )
                }
            }
        }

        // Barra de estado multi-dispositivo Firebase RTDB
        Surface(
            color = WarmSurfaceContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSimulateSecondDevice() },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isEmergencyStopActive) EmergencyRed else EmeraldActive),
                    )
                    Text(
                        text = "Firebase RTDB • ${uiState.connectedDevicesCount} Dispositivos en línea",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                    )
                }
                Text(
                    text = "${uiState.syncLatencyMs}ms • Sync #2",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )
            }
        }
    }
}

@Composable
private fun CriticalTelemetryAlertBanner(
    uiState: FloraSenseUiState,
    onGoToControls: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGoToControls() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencyRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alerta crítica",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                val alertTitle = when {
                    uiState.isEmergencyStopActive -> "PARADA DE EMERGENCIA ACTIVA"
                    uiState.isCriticalHeat && uiState.isLowMoisture -> "¡ALERTA DOBLE: CALOR (>35°C) Y SEQUÍA (<30%)!"
                    uiState.isCriticalHeat -> "¡UMBRAL CRÍTICO DE CALOR SUPERADO (> 35°C)!"
                    else -> "¡ADVERTENCIA DE HUMEDAD BAJA (< 30%)!"
                }
                Text(
                    text = alertTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Toca para abrir los Controles Manuales y gestionar actuadores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }
        }
    }
}

@Composable
fun SensorTelemetryCard(
    title: String,
    sensorBadge: String,
    valueText: String,
    backgroundColor: Color,
    accentColor: Color,
    icon: ImageVector,
    statusText: String,
    isWarning: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isWarning) {
            BorderStroke(1.5.dp, EmergencyRed.copy(alpha = 0.7f))
        } else {
            BorderStroke(1.dp, CharcoalText.copy(alpha = 0.06f))
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                    )
                }

                Surface(
                    color = PearlWarmBackground.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(
                        text = sensorBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalSubtle,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            // Valor numérico grande (MaterialTheme.typography.displayLarge)
            Text(
                text = valueText,
                style = MaterialTheme.typography.displayLarge,
                color = CharcoalText,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isWarning) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = EmergencyRed,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Medium,
                    color = if (isWarning) EmergencyRed else CharcoalSubtle,
                )
            }
        }
    }
}

@Composable
private fun DashboardTelemetrySimulator(
    uiState: FloraSenseUiState,
    onUpdateTelemetry: (Float, Int, Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "SIMULADOR DE LECTURAS ARDUINO UNO (TRAMA T,L,H)",
                style = MaterialTheme.typography.labelSmall,
                color = CharcoalSubtle,
            )

            Text(
                text = "Temperatura: ${String.format(Locale.US, "%.1f°C", uiState.temperatureCelsius)} (Alerta > 35°C)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = uiState.temperatureCelsius,
                onValueChange = {
                    onUpdateTelemetry(it, uiState.soilMoisturePercent, uiState.sunlightPercent)
                },
                valueRange = 12f..44f,
                colors = SliderDefaults.colors(
                    thumbColor = if (uiState.isCriticalHeat) EmergencyRed else EmeraldActive,
                    activeTrackColor = if (uiState.isCriticalHeat) EmergencyRed else EmeraldActive,
                    inactiveTrackColor = WarmSurfaceHigh,
                ),
            )

            Text(
                text = "Humedad del Suelo: ${uiState.soilMoisturePercent}% (Alerta < 30%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = uiState.soilMoisturePercent.toFloat(),
                onValueChange = {
                    onUpdateTelemetry(uiState.temperatureCelsius, it.toInt(), uiState.sunlightPercent)
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = if (uiState.isLowMoisture) EmergencyRed else EmeraldActive,
                    activeTrackColor = if (uiState.isLowMoisture) EmergencyRed else EmeraldActive,
                    inactiveTrackColor = WarmSurfaceHigh,
                ),
            )

            Text(
                text = "Luz Solar (LDR): ${uiState.sunlightPercent}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = uiState.sunlightPercent.toFloat(),
                onValueChange = {
                    onUpdateTelemetry(uiState.temperatureCelsius, uiState.soilMoisturePercent, it.toInt())
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldActive,
                    activeTrackColor = EmeraldActive,
                    inactiveTrackColor = WarmSurfaceHigh,
                ),
            )
        }
    }
}

@Composable
private fun FirebaseSyncFooterCard(
    uiState: FloraSenseUiState,
    onSimulateSecondDevice: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = CharcoalText,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "SINCRONIZACIÓN MULTI-DISPOSITIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSimulateSecondDevice() },
                    color = EmeraldSoftBg,
                    shape = RoundedCornerShape(50),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = CharcoalText,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "Simular Evento Disp. #2",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalText,
                        )
                    }
                }
            }

            Surface(
                color = if (uiState.isEmergencyStopActive) EmergencySoftBg else EmeraldSoftBg,
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    1.dp,
                    if (uiState.isEmergencyStopActive) EmergencyRed.copy(alpha = 0.3f) else EmeraldActive.copy(alpha = 0.3f)
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isEmergencyStopActive) EmergencyRed else EmeraldActive)
                    )
                    Text(
                        text = uiState.statusLogMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "Origen último paquete: ${uiState.lastSyncSource}",
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalSubtle,
                fontSize = 12.sp,
            )
        }
    }
}

private fun getRecommendedParametersForSpecies(speciesName: String?): Triple<Float, Float, Int> {
    val name = speciesName?.lowercase(Locale.getDefault()) ?: ""
    return when {
        name.contains("cactus") || name.contains("suculenta") || name.contains("aloe") || name.contains("jade") || name.contains("sansevieria") || name.contains("suegra") || name.contains("crasa") -> {
            Triple(18.0f, 38.0f, 20)
        }
        name.contains("limon") || name.contains("naranjo") || name.contains("palto") || name.contains("olivo") || name.contains("higuera") || name.contains("cerezo") || name.contains("durazn") || name.contains("manzano") || name.contains("parra") || name.contains("granado") || name.contains("chirimoyo") -> {
            Triple(15.0f, 32.0f, 40)
        }
        name.contains("arandano") || name.contains("frambuesa") || name.contains("frambueso") || name.contains("frutilla") || name.contains("fresa") -> {
            Triple(16.0f, 30.0f, 45)
        }
        name.contains("monstera") || name.contains("pothos") || name.contains("potus") || name.contains("gomero") || name.contains("ficus") || name.contains("orquidea") || name.contains("helecho") || name.contains("cradle") || name.contains("moises") || name.contains("cinta") -> {
            Triple(20.0f, 32.0f, 35)
        }
        name.contains("albahaca") || name.contains("menta") || name.contains("romero") || name.contains("oregano") || name.contains("cilantro") || name.contains("perejil") || name.contains("tomillo") || name.contains("cedron") || name.contains("ruda") || name.contains("lavanda") || name.contains("aji") || name.contains("tomate") -> {
            Triple(18.0f, 30.0f, 30)
        }
        else -> Triple(18.0f, 35.0f, 30)
    }
}

@Composable
private fun AddPlantDialog(
    uiState: FloraSenseUiState,
    onDismiss: () -> Unit,
    onSearch: (Context, String) -> Unit,
    onAddPlant: (Context, String, String?, Float, Float, Int) -> Unit,
) {
    val context = LocalContext.current
    var apodo by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSpecies by remember { mutableStateOf<PlantSearchResultItem?>(null) }
    var tempMin by remember { mutableStateOf("18.0") }
    var tempMax by remember { mutableStateOf("35.0") }
    var humedadOptima by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🌱 Agregar Nuevo Cultivo",
                style = MaterialTheme.typography.headlineMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "1. BUSCAR ESPECIE BOTÁNICA (API REST)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ej: Monstera, Cactus, Rosa...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    IconButton(
                        onClick = { onSearch(context, searchQuery) },
                        modifier = Modifier.background(EmeraldActive, RoundedCornerShape(12.dp)),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = CharcoalText,
                        )
                    }
                }

                if (uiState.isSearchingPlants) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = EmeraldActive,
                        )
                    }
                }

                if (uiState.plantSearchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        uiState.plantSearchResults.forEach { result ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSpecies = result
                                        apodo = result.commonName ?: apodo
                                        val (recMin, recMax, recHum) = getRecommendedParametersForSpecies(result.commonName ?: result.apiSpeciesId)
                                        tempMin = String.format(Locale.US, "%.1f", recMin)
                                        tempMax = String.format(Locale.US, "%.1f", recMax)
                                        humedadOptima = recHum.toString()
                                    },
                                color = if (selectedSpecies == result) EmeraldSoftBg else WarmSurfaceContainer,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = EmeraldActive,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Column {
                                        Text(
                                            text = result.commonName ?: "Especie sin nombre",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CharcoalText,
                                        )
                                        Text(
                                            text = result.scientificName ?: "",
                                            fontSize = 10.sp,
                                            color = CharcoalSubtle,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "2. PARÁMETROS DEL CULTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )

                if (selectedSpecies != null) {
                    val (rMin, rMax, rHum) = getRecommendedParametersForSpecies(selectedSpecies?.commonName ?: selectedSpecies?.apiSpeciesId)
                    Surface(
                        color = EmeraldSoftBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldActive,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "✓ Parámetros recomendados aplicados: ${String.format(Locale.US, "%.1f", rMin)}°C - ${String.format(Locale.US, "%.1f", rMax)}°C | Humedad ${rHum}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = CharcoalText,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = apodo,
                    onValueChange = { apodo = it },
                    label = { Text("Apodo de la Planta / Maceta") },
                    placeholder = { Text("Ej: Monstera Sala") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { tempMin = it },
                        label = { Text("Temp. Mín (°C)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = tempMax,
                        onValueChange = { tempMax = it },
                        label = { Text("Temp. Máx (°C)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    )
                }

                OutlinedTextField(
                    value = humedadOptima,
                    onValueChange = { humedadOptima = it },
                    label = { Text("Humedad Mínima Óptima (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minT = tempMin.toFloatOrNull() ?: 18f
                    val maxT = tempMax.toFloatOrNull() ?: 35f
                    val hum = humedadOptima.toIntOrNull() ?: 30
                    onAddPlant(
                        context,
                        apodo,
                        selectedSpecies?.commonName,
                        minT,
                        maxT,
                        hum,
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive, contentColor = CharcoalText),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Guardar Cultivo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CharcoalSubtle)
            }
        },
        containerColor = PearlWarmBackground,
    )
}
