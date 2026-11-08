package com.example.smartgarden

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Pantalla 3: Panel de Control de Actuadores (Bomba de Agua y Nebulizador),
 * selector de volumen de riego (50ml, 100ml, 250ml) y Botón Gigante de PARADA DE EMERGENCIA.
 */
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
    val irrigationOptions = listOf(50, 100, 250)
    var isDoseMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlWarmBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Cabecera del Panel de Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(onClick = onNavigateBackToDashboard) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver a Telemetría",
                        tint = CharcoalText,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Controles Manuales",
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = "Actuadores • Arduino UNO",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalSubtle,
                        maxLines = 1,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // Botón Conmutador Sol/Luna para Modo Oscuro OLED
                IconButton(onClick = onToggleDarkMode) {
                    Icon(
                        imageVector = if (uiState.isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                        contentDescription = if (uiState.isDarkMode) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro OLED",
                        tint = if (uiState.isDarkMode) Color(0xFFFACC15) else CharcoalText,
                        modifier = Modifier.size(20.dp),
                    )
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isEmergencyStopActive) EmergencyRed else EmeraldActive),
                        )
                        Text(
                            text = if (uiState.isEmergencyStopActive) "BLOQUEADO" else "RTDB ACTIVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }

        // Resumen rápido de telemetría relevante para los actuadores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = PastelOrangeTemp,
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TEMPERATURA",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalSubtle,
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f°C", uiState.temperatureCelsius),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (uiState.isCriticalHeat) EmergencyRed else CharcoalText,
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                color = PastelBlueMoisture,
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HUMEDAD SUELO",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalSubtle,
                    )
                    Text(
                        text = "${uiState.soilMoisturePercent}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (uiState.isLowMoisture) EmergencyRed else CharcoalText,
                    )
                }
            }
        }

        // ====================================================================
        // 1. INTERRUPTORES (SWITCH) DE ACTUADORES DE HARDWARE
        // ====================================================================
        Text(
            text = "INTERRUPTORES DE HARDWARE (TIEMPO REAL)",
            style = MaterialTheme.typography.labelSmall,
            color = CharcoalSubtle,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, CharcoalText.copy(alpha = 0.06f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Switch 1: Bomba de Agua (Motor CC - Pin D8)
                HardwareSwitchItem(
                    title = "Bomba de Agua",
                    subtitle = "Motor CC 12V (Pin D8) • Caudal nominal 1.2 L/min",
                    icon = Icons.Default.WaterDrop,
                    checked = uiState.isWaterPumpOn && !uiState.isEmergencyStopActive,
                    enabled = !uiState.isEmergencyStopActive,
                    onCheckedChange = onToggleWaterPump,
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = CharcoalText.copy(alpha = 0.08f),
                )

                // Switch 2: Nebulizador
                HardwareSwitchItem(
                    title = "Nebulizador",
                    subtitle = "Humidificador ultrasónico foliar • Control ambiental",
                    icon = Icons.Default.Air,
                    checked = uiState.isNebulizerOn && !uiState.isEmergencyStopActive,
                    enabled = !uiState.isEmergencyStopActive,
                    onCheckedChange = onToggleNebulizer,
                )
            }
        }

        // ====================================================================
        // 2. MENÚ DE SELECCIÓN DE VOLUMEN DE RIEGO (50ml, 100ml, 250ml)
        // ====================================================================
        Text(
            text = "DOSIFICACIÓN DE VOLUMEN DE RIEGO",
            style = MaterialTheme.typography.labelSmall,
            color = CharcoalSubtle,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, CharcoalText.copy(alpha = 0.06f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Volumen de Riego Manual",
                            style = MaterialTheme.typography.titleMedium,
                            color = CharcoalText,
                        )
                        Text(
                            text = "Selecciona 50ml, 100ml o 250ml por pulso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalSubtle,
                        )
                    }

                    // Menú desplegable sincronizado
                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !uiState.isEmergencyStopActive) {
                                    isDoseMenuExpanded = true
                                },
                            color = WarmSurfaceHigh,
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${uiState.selectedDoseMl}ml",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CharcoalText,
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar menú de dosis",
                                    tint = CharcoalText,
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isDoseMenuExpanded,
                            onDismissRequest = { isDoseMenuExpanded = false },
                            modifier = Modifier.background(WarmSurfaceContainer),
                        ) {
                            irrigationOptions.forEach { dose ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Dosis: ${dose}ml",
                                            color = CharcoalText,
                                            fontWeight = if (uiState.selectedDoseMl == dose) {
                                                FontWeight.ExtraBold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        )
                                    },
                                    onClick = {
                                        onSelectDose(dose)
                                        isDoseMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Botones segmentados (50ml, 100ml, 250ml)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(WarmSurfaceHigh)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    irrigationOptions.forEach { dose ->
                        val isSelected = uiState.selectedDoseMl == dose
                        Button(
                            onClick = { onSelectDose(dose) },
                            enabled = !uiState.isEmergencyStopActive,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) EmeraldActive else Color.Transparent,
                                contentColor = CharcoalText,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = CharcoalSubtle,
                            ),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                text = "${dose}ml",
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = CharcoalText,
                            )
                        }
                    }
                }

                // Botón Verde Esmeralda para inyectar la dosis seleccionada
                Button(
                    onClick = onApplyIrrigationDose,
                    enabled = !uiState.isEmergencyStopActive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldActive,
                        contentColor = CharcoalText,
                        disabledContainerColor = WarmSurfaceHigh,
                        disabledContentColor = CharcoalSubtle,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Suministrar Riego Ahora (${uiState.selectedDoseMl}ml)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // ====================================================================
        // 3. BOTÓN GIGANTE Y ROJO DE "PARADA DE EMERGENCIA" (OVERRIDE MANUAL)
        // ====================================================================
        Text(
            text = "OVERRIDE MANUAL SOBRE MICROCONTROLADOR (> 35°C)",
            style = MaterialTheme.typography.labelSmall,
            color = if (uiState.isEmergencyStopActive || uiState.isCriticalHeat) {
                EmergencyRed
            } else {
                CharcoalSubtle
            },
        )

        Button(
            onClick = onTriggerEmergencyStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmergencyRed,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Parada de Emergencia",
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PARADA DE EMERGENCIA",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = if (uiState.isEmergencyStopActive) {
                        "ACTUADORES CORTADOS • TOCA PARA REARMAR"
                    } else {
                        "CORTE INMEDIATO DE BOMBA CC Y NEBULIZADOR"
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Tarjeta de sincronización en tiempo real entre los 2 dispositivos
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (uiState.isEmergencyStopActive) EmergencySoftBg else WarmSurfaceContainer,
            shape = RoundedCornerShape(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            color = if (uiState.isEmergencyStopActive) EmergencySoftBg else EmeraldSoftBg,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isEmergencyStopActive) EmergencyRed.copy(alpha = 0.3f) else EmeraldActive.copy(alpha = 0.3f)
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
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
                            text = "Modo: Conexión Remota (Django API)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalSubtle,
                            fontSize = 11.sp,
                        )
                    }
                }

                IconButton(onClick = onSimulateSecondDevice) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Simular cambio desde Dispositivo 2",
                        tint = CharcoalText,
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (checked) EmeraldSoftBg else WarmSurfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CharcoalText,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = CharcoalText,
                    )
                    Surface(
                        color = if (checked) EmeraldActive else WarmSurfaceHigh,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = if (checked) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalSubtle,
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PearlWarmBackground,
                checkedTrackColor = EmeraldActive,
                uncheckedThumbColor = CharcoalSubtle,
                uncheckedTrackColor = WarmSurfaceHigh,
                uncheckedBorderColor = CharcoalSubtle.copy(alpha = 0.4f),
            ),
        )
    }
}
