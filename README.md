# SmartGarden Android — Aplicación Móvil IoT Nativa

Cliente móvil nativo para el ecosistema **SmartGarden**, desarrollado en **Kotlin**, **Jetpack Compose** y **Material Design 3**, diseñado para la supervisión ambiental continua, control de actuadores y telemetría en tiempo real de macetas inteligentes conectadas a placas **ESP32**.

---

## 🏛️ Arquitectura del Sistema

```text
┌───────────────────────┐         Sondeo / REST          ┌──────────────────────────────────┐
│   Android App (MVVM)  │ ◄────────────────────────────► │  API Django (Vercel + Supabase)  │
│  - Jetpack Compose    │                                └──────────────────────────────────┘
│  - StateFlow / Room   │         Stream SSE (< 30ms)    ┌──────────────────────────────────┐
│  - Guardián 24/7 FGS  │ ◄────────────────────────────► │ Firebase Realtime Database (RTDB)│
└───────────────────────┘                                └─────────────────▲────────────────┘
                                                                           │ Telemetría WiFi
                                                         ┌─────────────────┴────────────────┐
                                                         │     ESP32 Firmware + Sensores    │
                                                         └──────────────────────────────────┘
```

- **Patrón MVVM**: `FloraSenseViewModel` orquestando el estado reactivo mediante `StateFlow` y `FloraSenseUiState`.
- **UI Declarativa**: Jetpack Compose con animaciones fluidas, paleta orgánica biofílica y soporte nativo para **Modo Claro Pastel** y **Modo Oscuro OLED**.
- **Autenticación Segura (SimpleJWT)**:
  - Tokens de acceso y refresco almacenados en `EncryptedSharedPreferences` (cifrado por hardware AES256-GCM / AES256-SIV).
  - Renovación transparente y automática de tokens mediante `TokenAuthenticator` en OkHttp (`POST /api/token/refresh/`).
- **Persistencia y Caché Offline**:
  - `OfflineCacheManager` respaldado en Room Database para consulta y visualización de catálogo botánico y telemetría histórica sin conexión a Internet.
- **Sincronización Dual**:
  - **Capa Rápida (< 30 ms)**: Firebase Realtime Database para reflejo instantáneo de sensores y actuadores.
  - **Capa Histórica**: API Django REST sobre PostgreSQL (Supabase) para registros permanentes en `TelemetryLog`.

---

## 🛡️ Guardián 24/7 (`TelemetryGuardianService`)

Servicio en primer plano (**Foreground Service**) que protege las plantas del usuario de manera continua, incluso con la aplicación cerrada o el dispositivo bloqueado:

- **Canal de Notificaciones de Alta Prioridad**: Canal dedicado `temp_alerts_channel` con vibración, sonido y visualización flotante (Heads-up notification).
- **Conexión SSE Persistente**: Mantiene un stream `text/event-stream` de ultrabajo consumo hacia Firebase RTDB sobre OkHttp, recibiendo actualizaciones instantáneas sin necesidad de sondeos pesados.
- **Aislamiento Multi-Usuario (Multi-Tenant)**: El servicio consulta dinámicamente `TokenManager.getUsername()` y se conecta exclusivamente a `/telemetria/<nombre_usuario>/actual.json`, evitando falsas alarmas de otros usuarios.
- **Supervivencia a Cierre de Tareas (`onTaskRemoved`)**: Implementa persistencia asistida por `AlarmManager` para restaurarse automáticamente si el usuario desliza la aplicación en la bandeja de Recientes.
- **Reglas de Detección Crítica**:
  - **Alerta Térmica**: Se activa al superar los 35.0 °C (o umbral configurado por especie).
  - **Alerta Hídrica**: Se activa al caer la humedad de suelo bajo el 30% (o umbral de la maceta).
  - Incluye el nombre personalizado de la maceta afectada en el cuerpo de la notificación (ej. *"¡Atención! La maceta 'Monstera Deliciosa' registra 38.5°C"*).

---

## 🎛️ Control de Actuadores y Parada de Emergencia

- **Aislamiento por Maceta**: Cada planta en la flota conserva su estado independiente de bomba de agua, nebulizador ultrasónico y calibración de dosis (50 ml, 100 ml, 250 ml o personalizado) mediante `Map<String, PlantActuatorsState>`.
- **Parada de Emergencia Global (E-STOP)**: Bloqueo maestro que anula y apaga inmediatamente todos los relés y actuadores de todas las plantas por seguridad física del circuito.

---

## 🚀 Compilación y Ejecución

### Requisitos
- Android Studio Ladybug / Iguana o superior.
- JDK 17 o 21 (incluido en Android Studio JBR).
- Android SDK 34/35 configurado (`compileSdk = 35`, `minSdk = 26`).

### Compilación desde Terminal (Windows PowerShell)
```powershell
# Compilar APK de depuración
.\gradlew.bat assembleDebug

# Detener el daemon de Gradle (obligatorio para evitar bloqueos de archivos en OneDrive/Windows)
.\gradlew.bat --stop
```

El APK generado se encontrará disponible en:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 Estructura del Código

```text
app/src/main/java/com/example/smartgarden/
├── MainActivity.kt               # Entrada de la app, permisos y ciclo de vida
├── AppNavigation.kt              # Grafo de navegación Compose y barra de pestañas
├── FloraSenseViewModel.kt        # ViewModel principal, lógica de negocio y estado
├── NotificationHelper.kt         # Despacho y canales de notificaciones locales y críticas
├── network/
│   ├── ApiService.kt             # Interfaces Retrofit hacia Django REST
│   ├── RetrofitClient.kt         # Cliente HTTP, interceptor JWT y autenticador
│   ├── FirebaseRealtimeClient.kt # Conector SSE y emisor reactivo a Firebase RTDB
│   ├── TokenManager.kt           # Manejador seguro de credenciales (EncryptedSharedPrefs)
│   └── OfflineCacheManager.kt    # Caché local offline Room/SharedPreferences
├── service/
│   └── TelemetryGuardianService.kt # Foreground Service Guardián 24/7 en segundo plano
└── ui/theme/                     # Colores biofílicos, tipografía e iconografía
```
