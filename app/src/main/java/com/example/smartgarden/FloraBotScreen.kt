package com.example.smartgarden

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FloraBotScreen(
    uiState: FloraSenseUiState,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll al final cada vez que cambien los mensajes o el estado de carga
    LaunchedEffect(uiState.chatMessages.size, uiState.isChatLoading) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    val quickQuestions = remember {
        listOf(
            "Traducir nombre popular a científico/inglés",
            "¿Cómo busco boldo, palto o copihue?",
            "¿Cómo está mi maceta?",
            "¿Cuándo le toca riego?",
            "¿Tiene riesgo de plagas?",
            "¿La iluminación actual es adecuada?",
            "¿Cómo están todas mis macetas?",
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PearlWarmBackground)
            .imePadding(),
    ) {
        // Encabezado FloraBot IA
        FloraBotHeader(
            activePlant = uiState.activePlant,
            onClearHistory = onClearHistory,
        )

        // Banner de Contexto de Telemetría Compartida
        ActivePlantTelemetryBanner(uiState = uiState)

        // Sugerencias Rápidas de Pregunta
        QuickQuestionChips(
            questions = quickQuestions,
            enabled = !uiState.isChatLoading,
            onSelect = { question ->
                onSendMessage(question)
            },
        )

        // Lista de Mensajes de Conversación
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
        ) {
            items(uiState.chatMessages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }

            if (uiState.isChatLoading) {
                item {
                    BotTypingIndicator()
                }
            }
        }

        // Barra inferior de entrada de texto
        ChatInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            isLoading = uiState.isChatLoading,
            onSend = {
                if (inputText.isNotBlank() && !uiState.isChatLoading) {
                    onSendMessage(inputText)
                    inputText = ""
                }
            },
        )
    }
}

@Composable
private fun FloraBotHeader(
    activePlant: CustomPlantProfile?,
    onClearHistory: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WarmSurfaceContainer,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = EmeraldActive,
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "FloraBot",
                        tint = CharcoalText,
                        modifier = Modifier.padding(10.dp),
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FloraBot IA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalText,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldSoftBg,
                        ) {
                            Text(
                                text = "Inteligente",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldActive,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = "Asesor agronómico & traductor botánico",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalSubtle,
                    )
                }
            }

            IconButton(
                onClick = onClearHistory,
                colors = IconButtonDefaults.iconButtonColors(contentColor = CharcoalSubtle),
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Limpiar conversación",
                )
            }
        }
    }
}

@Composable
private fun ActivePlantTelemetryBanner(uiState: FloraSenseUiState) {
    val plant = uiState.activePlant
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = EmeraldSoftBg.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = EmeraldActive,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = plant?.let { "Maceta: ${it.apodo}" } ?: "Sin maceta seleccionada",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalText,
                    maxLines = 1,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = PastelOrangeAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f°C", uiState.temperatureCelsius),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = PastelBlueAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${uiState.soilMoisturePercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = PastelYellowAccent, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${uiState.sunlightPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickQuestionChips(
    questions: List<String>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        questions.forEach { q ->
            FilterChip(
                selected = false,
                onClick = { onSelect(q) },
                enabled = enabled,
                label = {
                    Text(
                        text = q,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = EmeraldActive,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = WarmSurfaceContainer,
                    labelColor = CharcoalText,
                ),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessageItem) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) EmeraldActive else WarmSurfaceContainer
    val textColor = CharcoalText
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            if (!isUser) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(top = 2.dp),
                    shape = CircleShape,
                    color = EmeraldSoftBg,
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "FloraBot",
                        tint = EmeraldActive,
                        modifier = Modifier.padding(6.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp,
                ),
                color = bubbleColor,
                shadowElevation = 0.5.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 20.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalSubtle.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
    }
}

@Composable
private fun BotTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = CircleShape,
            color = EmeraldSoftBg,
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                tint = EmeraldActive,
                modifier = Modifier.padding(6.dp),
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WarmSurfaceContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = EmeraldActive,
                )
                Text(
                    text = "FloraBot está analizando las variables...",
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalSubtle,
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WarmSurfaceContainer,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Pregúntale a FloraBot...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalSubtle,
                    )
                },
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSend() }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldActive,
                    unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.3f),
                    focusedContainerColor = PearlWarmBackground,
                    unfocusedContainerColor = PearlWarmBackground,
                    cursorColor = EmeraldActive,
                ),
            )

            IconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (value.isNotBlank() && !isLoading) EmeraldActive else WarmSurfaceHigh),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = CharcoalText,
                    disabledContentColor = CharcoalSubtle,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CharcoalText,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar mensaje",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
