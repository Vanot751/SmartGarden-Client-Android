package com.example.smartgarden

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla 1: Autenticación minimalista con Firebase Auth simulado y cumplimiento visual ISO/IEC 27400.
 */
@Composable
fun LoginScreen(
    uiState: FloraSenseUiState,
    onClearError: () -> Unit,
    onLoginSubmit: (String, String) -> Unit,
    onRegisterSubmit: (String, String, String, String) -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE) }
    var rememberUser by rememberSaveable { mutableStateOf(prefs.getBoolean("remember_user", true)) }
    var email by rememberSaveable {
        mutableStateOf(if (rememberUser) prefs.getString("saved_email", "").orEmpty() else "")
    }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlWarmBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Insignia superior de estándar de ciberseguridad IoT ISO/IEC 27400
            Surface(
                color = EmeraldSoftBg,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, EmeraldActive.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Seguridad ISO 27400",
                        tint = CharcoalText,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = uiState.isoSecurityProtocol,
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                    )
                }
            }

            // Botón conmutador Sol/Luna para Modo Oscuro OLED
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (uiState.isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                    contentDescription = if (uiState.isDarkMode) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro OLED",
                    tint = if (uiState.isDarkMode) Color(0xFFFACC15) else CharcoalText,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // Identidad de marca SmartGarden
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "SmartGarden Logo",
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "SmartGarden IoT",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 26.sp,
                color = CharcoalText,
            )
            Text(
                text = "Pasarela Segura de Monitoreo y Control Botánico",
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalSubtle,
            )
        }

        // Tarjeta de formulario de acceso (Pastel Minimalism, sin blanco puro)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, CharcoalText.copy(alpha = 0.08f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = if (isRegisterMode) "REGISTRO DE NUEVA CUENTA (FIREBASE AUTH)" else "AUTENTICACIÓN DE OPERADOR (FIREBASE AUTH)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalSubtle,
                )

                // Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        onClearError()
                    },
                    label = { Text("Correo Electrónico") },
                    placeholder = { Text("operador@smartgarden.io") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = CharcoalText,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PearlWarmBackground,
                        unfocusedContainerColor = PearlWarmBackground,
                        focusedBorderColor = EmeraldActive,
                        unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.35f),
                        focusedTextColor = CharcoalText,
                        unfocusedTextColor = CharcoalText,
                        focusedLabelColor = CharcoalText,
                        unfocusedLabelColor = CharcoalSubtle,
                        cursorColor = CharcoalText,
                    ),
                )

                // Campo de Confirmación de Email (Solo en modo registro)
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = confirmEmail,
                        onValueChange = {
                            confirmEmail = it
                            onClearError()
                        },
                        label = { Text("Confirmar Correo Electrónico") },
                        placeholder = { Text("operador@smartgarden.io") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Confirmar Email",
                                tint = CharcoalText,
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PearlWarmBackground,
                            unfocusedContainerColor = PearlWarmBackground,
                            focusedBorderColor = EmeraldActive,
                            unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.35f),
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText,
                            focusedLabelColor = CharcoalText,
                            unfocusedLabelColor = CharcoalSubtle,
                            cursorColor = CharcoalText,
                        ),
                    )
                }

                // Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        onClearError()
                    },
                    label = { Text("Contraseña Cifrada") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Contraseña",
                            tint = CharcoalText,
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = "Alternar visibilidad",
                                tint = CharcoalSubtle,
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PearlWarmBackground,
                        unfocusedContainerColor = PearlWarmBackground,
                        focusedBorderColor = EmeraldActive,
                        unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.35f),
                        focusedTextColor = CharcoalText,
                        unfocusedTextColor = CharcoalText,
                        focusedLabelColor = CharcoalText,
                        unfocusedLabelColor = CharcoalSubtle,
                        cursorColor = CharcoalText,
                    ),
                )

                // Campo de Confirmación de Contraseña (Solo en modo registro)
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            onClearError()
                        },
                        label = { Text("Confirmar Contraseña Cifrada") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Confirmar Contraseña",
                                tint = CharcoalText,
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = "Alternar visibilidad confirmación",
                                    tint = CharcoalSubtle,
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PearlWarmBackground,
                            unfocusedContainerColor = PearlWarmBackground,
                            focusedBorderColor = EmeraldActive,
                            unfocusedBorderColor = CharcoalSubtle.copy(alpha = 0.35f),
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText,
                            focusedLabelColor = CharcoalText,
                            unfocusedLabelColor = CharcoalSubtle,
                            cursorColor = CharcoalText,
                        ),
                    )
                }

                // Casilla de verificación: Recordar operador en el dispositivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Checkbox(
                        checked = rememberUser,
                        onCheckedChange = { isChecked ->
                            rememberUser = isChecked
                            prefs.edit().putBoolean("remember_user", isChecked).apply()
                            if (!isChecked) {
                                prefs.edit().remove("saved_email").apply()
                            } else if (email.isNotEmpty()) {
                                prefs.edit().putString("saved_email", email.trim()).apply()
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldActive,
                            uncheckedColor = CharcoalSubtle,
                            checkmarkColor = CharcoalText,
                        ),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recordar operador en este dispositivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalText,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Mensaje de error de validación
                if (uiState.authError != null) {
                    Surface(
                        color = EmergencySoftBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = EmergencyRed,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = uiState.authError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = EmergencyRed,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Botón principal (Iniciar Sesión / Registrar Cuenta) en Verde Esmeralda
                Button(
                    onClick = {
                        if (rememberUser) {
                            prefs.edit()
                                .putBoolean("remember_user", true)
                                .putString("saved_email", email.trim())
                                .apply()
                        } else {
                            prefs.edit()
                                .putBoolean("remember_user", false)
                                .remove("saved_email")
                                .apply()
                        }

                        if (isRegisterMode) {
                            onRegisterSubmit(email, confirmEmail, password, confirmPassword)
                        } else {
                            onLoginSubmit(email, password)
                        }
                    },
                    enabled = !uiState.isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
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
                    if (uiState.isAuthenticating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = CharcoalText,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isRegisterMode) "Registrando cuenta..." else "Verificando credenciales...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegisterMode) "Registrar Cuenta Nueva" else "Iniciar Sesión",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                        )
                    }
                }

                // Botón para alternar entre Iniciar Sesión y Registro
                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        confirmEmail = ""
                        confirmPassword = ""
                        onClearError()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalText,
                        fontWeight = FontWeight.Bold,
                    )
                }

                TextButton(
                    onClick = {
                        email = "operador_botanico"
                        password = "TuPassword2026!"
                        if (rememberUser) {
                            prefs.edit().putString("saved_email", email).apply()
                        }
                        onClearError()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = "Restaurar credenciales de demostración IoT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CharcoalSubtle,
                    )
                }
            }
        }

        // Panel de Cumplimiento ISO/IEC 27400 (Seguridad y Cifrado Visual)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WarmSurfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldActive),
                    )
                    Text(
                        text = "ARQUITECTURA DE SEGURIDAD ISO/IEC 27400",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalText,
                    )
                }

                IsoSecurityFeatureRow(
                    title = "Confidencialidad (Anti-Sniffing)",
                    description = "Túnel cifrado TLS 1.3 para proteger la trama de telemetría (T,L,H) entre nodos.",
                )
                IsoSecurityFeatureRow(
                    title = "Integridad (Anti-Spoofing)",
                    description = "Validación de token Firebase Auth para impedir inyección de comandos en la bomba CC.",
                )
                IsoSecurityFeatureRow(
                    title = "Disponibilidad Multi-Dispositivo",
                    description = "Sincronización simultánea en Firebase RTDB (${uiState.connectedDevicesCount} dispositivos autorizados).",
                )
            }
        }
    }
}

@Composable
private fun IsoSecurityFeatureRow(
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldActive,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CharcoalText,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalSubtle,
                fontSize = 12.sp,
            )
        }
    }
}
