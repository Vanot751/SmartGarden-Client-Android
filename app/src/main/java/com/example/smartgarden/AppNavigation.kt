package com.example.smartgarden

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Definición de rutas tipadas para la arquitectura de navegación de FloraSense.
 */
enum class FloraSenseRoute(val route: String, val label: String) {
    LOGIN("login", "Acceso Seguro"),
    DASHBOARD("dashboard", "Telemetría"),
    CONTROL_PANEL("control_panel", "Actuadores"),
}

/**
 * Sistema de navegación principal basado en Jetpack Navigation Compose y MVVM.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: FloraSenseViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE) }
    val isRememberUser = remember { prefs.getBoolean("remember_user", true) }
    val isSavedDarkMode = remember { prefs.getBoolean("is_dark_mode", false) }

    // Restaurar el Modo Oscuro OLED guardado en las preferencias del dispositivo
    LaunchedEffect(Unit) {
        if (isSavedDarkMode) {
            viewModel.setDarkMode(true)
        }
    }

    FloraSenseTheme(isDarkMode = uiState.isDarkMode) {
        // Redirección automática al Dashboard si existe una sesión JWT válida.
        LaunchedEffect(uiState.isAuthenticated) {
            if (uiState.isAuthenticated && isRememberUser) {
                navController.navigate(FloraSenseRoute.DASHBOARD.route) {
                    popUpTo(FloraSenseRoute.LOGIN.route) { inclusive = true }
                }
            }
        }

        // Alertas locales mientras la aplicación está abierta.
        LaunchedEffect(uiState.isRealtimeConnected, uiState.temperatureCelsius, uiState.soilMoisturePercent, uiState.selectedCategory) {
            // Solo evaluar alertas cuando la última lectura de la API fue válida.
            if (uiState.isRealtimeConnected) {
                if (uiState.isCriticalHeat) {
                    NotificationHelper.sendTemperatureAlert(
                        context = context,
                        plantName = uiState.activePlant?.apodo ?: "la maceta activa",
                        tempCelsius = uiState.temperatureCelsius,
                    )
                }
                val minMoistureThreshold = uiState.activeMoistureThreshold
                if (uiState.soilMoisturePercent < minMoistureThreshold) {
                    NotificationHelper.sendMoistureAlert(
                        context = context,
                        plantName = uiState.activePlant?.apodo ?: "La maceta activa",
                        moisturePercent = uiState.soilMoisturePercent,
                        minThresholdPercent = minMoistureThreshold,
                    )
                }
            }
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = currentRoute == FloraSenseRoute.DASHBOARD.route ||
            currentRoute == FloraSenseRoute.CONTROL_PANEL.route

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = PearlWarmBackground,
            bottomBar = {
                if (showBottomBar) {
                    FloraSenseBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { targetRoute ->
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = FloraSenseRoute.LOGIN.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                // Pantalla 1: Autenticación (API Django)
                composable(route = FloraSenseRoute.LOGIN.route) {
                    LoginScreen(
                        uiState = uiState,
                        onClearError = viewModel::clearAuthError,
                        onLoginSubmit = { email, password ->
                            viewModel.loginWithDjango(
                                username = email,
                                password = password,
                                onLoginSuccess = {
                                    navController.navigate(FloraSenseRoute.DASHBOARD.route) {
                                        popUpTo(FloraSenseRoute.LOGIN.route) { inclusive = true }
                                    }
                                },
                                onError = { /* El ViewModel ya maneja el estado de error en uiState.authError */ }
                            )
                        },
                        onRegisterSubmit = { email, confirmEmail, password, confirmPassword ->
                            viewModel.registerWithDjango(
                                email = email,
                                password = password,
                                passwordConfirmation = confirmPassword,
                                onRegisterSuccess = {
                                    navController.navigate(FloraSenseRoute.DASHBOARD.route) {
                                        popUpTo(FloraSenseRoute.LOGIN.route) { inclusive = true }
                                    }
                                },
                                onError = { /* El ViewModel ya maneja el estado de error en uiState.authError */ },
                            )
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode(context) },
                    )
                }

                // Pantalla 2: Dashboard de Telemetría Ambiental
                composable(route = FloraSenseRoute.DASHBOARD.route) {
                    DashboardScreen(
                        uiState = uiState,
                        onSelectCategory = viewModel::selectPlantCategory,
                        onSelectPlant = viewModel::selectActivePlant,
                        onEnsureModule = viewModel::ensureModuleAvailable,
                        onSearchPlants = viewModel::searchPlants,
                        onAddPlant = { result ->
                            viewModel.addPlant(result)
                        },
                        onDeletePlant = viewModel::deletePlant,
                        onUpdateSimulatedTelemetry = viewModel::updateSimulatedTelemetry,
                        onSimulateSecondDevice = viewModel::simulateSecondDeviceSync,
                        onNavigateToControls = {
                            navController.navigate(FloraSenseRoute.CONTROL_PANEL.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onLogout = {
                            viewModel.logout {
                                navController.navigate(FloraSenseRoute.LOGIN.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode(context) },
                    )
                }

                // Pantalla 3: Panel de Control de Actuadores y Parada de Emergencia
                composable(route = FloraSenseRoute.CONTROL_PANEL.route) {
                    ControlPanelScreen(
                        uiState = uiState,
                        onToggleWaterPump = viewModel::setWaterPumpState,
                        onToggleNebulizer = viewModel::setNebulizerState,
                        onSelectDose = viewModel::selectDoseMl,
                        onApplyIrrigationDose = viewModel::applyIrrigationDose,
                        onTriggerEmergencyStop = viewModel::triggerEmergencyStop,
                        onSimulateSecondDevice = viewModel::simulateSecondDeviceSync,
                        onNavigateBackToDashboard = {
                            navController.navigate(FloraSenseRoute.DASHBOARD.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode(context) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FloraSenseBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = WarmSurfaceContainer,
        contentColor = CharcoalText,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = currentRoute == FloraSenseRoute.DASHBOARD.route,
            onClick = { onNavigate(FloraSenseRoute.DASHBOARD.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = FloraSenseRoute.DASHBOARD.label,
                )
            },
            label = {
                Text(
                    text = "Telemetría",
                    fontWeight = if (currentRoute == FloraSenseRoute.DASHBOARD.route) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CharcoalText,
                selectedTextColor = CharcoalText,
                indicatorColor = EmeraldActive,
                unselectedIconColor = CharcoalSubtle,
                unselectedTextColor = CharcoalSubtle,
            ),
        )

        NavigationBarItem(
            selected = currentRoute == FloraSenseRoute.CONTROL_PANEL.route,
            onClick = { onNavigate(FloraSenseRoute.CONTROL_PANEL.route) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = FloraSenseRoute.CONTROL_PANEL.label,
                )
            },
            label = {
                Text(
                    text = "Controles Manuales",
                    fontWeight = if (currentRoute == FloraSenseRoute.CONTROL_PANEL.route) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CharcoalText,
                selectedTextColor = CharcoalText,
                indicatorColor = EmeraldActive,
                unselectedIconColor = CharcoalSubtle,
                unselectedTextColor = CharcoalSubtle,
            ),
        )
    }
}
