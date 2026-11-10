package com.example.smartgarden

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ControlPanelScreen(
    uiState: FloraSenseUiState,
    onToggleWaterPump: (Boolean) -> Unit,
    onToggleNebulizer: (Boolean) -> Unit,
    onSelectDose: (Int) -> Unit,
    onApplyIrrigationDose: () -> Unit,
    onTriggerEmergencyStop: () -> Unit,
    onSimulateSecondDevice: () -> Unit,
    onNavigateBackToDashboard: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlWarmBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBackToDashboard) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = CharcoalText)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Controles", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Acciones manuales del jardín", style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
            }
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    if (uiState.isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                    contentDescription = "Cambiar tema",
                    tint = CharcoalText,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (uiState.isEmergencyStopActive) EmergencySoftBg else EmeraldSoftBg,
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(9.dp),
                    shape = CircleShape,
                    color = if (uiState.isEmergencyStopActive) EmergencyRed else EmeraldActive,
                ) {}
                Text(
                    text = if (uiState.isEmergencyStopActive) "Parada de emergencia activa" else "Controles disponibles",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Maceta seleccionada", style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
                Text(
                    uiState.activePlant?.apodo ?: "Ninguna maceta seleccionada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                uiState.activePlant?.let { plant ->
                    Text(
                        "Rango óptimo: ${plant.tempMin}°C - ${plant.tempMax}°C • Humedad mínima: ${plant.minMoistureThreshold}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalSubtle,
                    )
                }
            }
        }

        ControlSwitchCard(
            title = "Bomba de agua",
            description = "Riego calibrado para la maceta seleccionada",
            iconColor = PastelBlueAccent,
            icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PastelBlueAccent) },
            enabled = uiState.isWaterPumpOn,
            disabled = uiState.isEmergencyStopActive,
            onToggle = onToggleWaterPump,
        )
        ControlSwitchCard(
            title = "Nebulizador",
            description = "Aumenta la humedad ambiental",
            iconColor = PastelYellowAccent,
            icon = { Icon(Icons.Default.WbSunny, contentDescription = null, tint = PastelYellowAccent) },
            enabled = uiState.isNebulizerOn,
            disabled = uiState.isEmergencyStopActive,
            onToggle = onToggleNebulizer,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dosis de riego", fontWeight = FontWeight.Bold)
                Text("${uiState.selectedDoseMl} ml", style = MaterialTheme.typography.headlineSmall, color = EmeraldActive)
                Slider(
                    value = uiState.selectedDoseMl.toFloat(),
                    onValueChange = { onSelectDose(it.toInt()) },
                    valueRange = 50f..250f,
                    steps = 1,
                    enabled = !uiState.isEmergencyStopActive,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 100, 250).forEach { dose ->
                        Button(
                            onClick = { onSelectDose(dose) },
                            enabled = !uiState.isEmergencyStopActive,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.selectedDoseMl == dose) EmeraldActive else WarmSurfaceHigh,
                                contentColor = CharcoalText,
                            ),
                        ) {
                            Text("${dose}ml")
                        }
                    }
                }
                Button(
                    onClick = onApplyIrrigationDose,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isEmergencyStopActive,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldActive, contentColor = CharcoalText),
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Preparar riego", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }

        Button(
            onClick = onTriggerEmergencyStop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isEmergencyStopActive) EmeraldActive else EmergencyRed,
                contentColor = if (uiState.isEmergencyStopActive) CharcoalText else Color.White,
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Emergency, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                if (uiState.isEmergencyStopActive) "Restablecer controles" else "Parada de emergencia",
                modifier = Modifier.padding(start = 6.dp),
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = "Los actuadores se mantienen como estado local porque la API actual todavía no expone un endpoint persistente.",
            style = MaterialTheme.typography.labelSmall,
            color = CharcoalSubtle,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ControlSwitchCard(
    title: String,
    description: String,
    iconColor: Color,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    disabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WarmSurfaceContainer,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = iconColor.copy(alpha = 0.14f)) {
                icon()
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = CharcoalSubtle)
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                enabled = !disabled,
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldActive, checkedTrackColor = EmeraldSoftBg),
            )
        }
    }
}
