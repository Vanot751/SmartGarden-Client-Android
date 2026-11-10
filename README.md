# SmartGarden Android

Cliente Android nativo de SmartGarden. La aplicación consume la misma API
Django REST que utiliza la web desplegada:

```text
Android -> https://smart-garden-api-django.vercel.app/api/
                         |
                         -> PostgreSQL administrado en Supabase
```

## Arquitectura

- Kotlin, Jetpack Compose y Material 3.
- MVVM con `StateFlow`.
- Autenticación mediante SimpleJWT (access y refresh tokens) y almacenamiento cifrado en `EncryptedSharedPreferences` (AES256).
- Renovación transparente y automática de tokens mediante `TokenAuthenticator` en OkHttp (`POST /api/token/refresh/`).
- Persistencia offline de datos botánicos y telemetría mediante `OfflineCacheManager` para funcionamiento sin conexión.
- Retrofit + OkHttp para la comunicación con Django.
- Modo claro pastel y modo oscuro OLED persistidos localmente.
- Sondeo moderado de telemetría mientras la sesión está activa.

## Funcionalidades conectadas a la API

- Inicio de sesión JWT.
- Registro de cuentas mediante `POST /api/auth/register/`.
- Consulta de módulos y macetas del usuario.
- Creación automática de una maceta virtual para cuentas sin módulos.
- Búsqueda en el catálogo botánico.
- Creación y eliminación de plantas asignadas a módulos.
- Lectura, envío e histórico de telemetría por módulo.
- Sincronización manual y automática con Django.

## Funcionalidades locales

La API actual no expone un recurso persistente de actuadores. Por esa razón,
los controles de bomba, nebulizador, dosis y parada de emergencia se mantienen
como estado local y no se presentan como sincronizados con un servicio
inexistente.

La simulación de temperatura, humedad y luz sí se envía al módulo activo
mediante `POST /api/modules/<id>/telemetry/`, por lo que queda disponible en
la telemetría de Django y se conserva al actualizar la aplicación.

Las alertas ambientales actuales son notificaciones locales mientras la
aplicación está abierta. No se integra Firebase, FCM ni ningún servicio push
en esta versión.

## Compilación

Desde la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

El proyecto requiere un JDK compatible y el Android SDK configurado en
`ANDROID_HOME` o mediante `local.properties`.
