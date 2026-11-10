# SmartGarden Android

Cliente Android nativo de SmartGarden. La aplicación consume la misma API Django REST que utiliza la plataforma web:

```text
Android -> https://smart-garden-api-django.vercel.app/api/
                         |
                         -> PostgreSQL administrado en Supabase
```

## Arquitectura

- Kotlin, Jetpack Compose y Material 3.
- Arquitectura MVVM con `StateFlow`.
- Autenticación mediante SimpleJWT (access y refresh tokens) y almacenamiento seguro en `EncryptedSharedPreferences` (AES256).
- Renovación transparente y automática de tokens mediante `TokenAuthenticator` en OkHttp (`POST /api/token/refresh/`).
- Persistencia offline de datos botánicos y telemetría mediante `OfflineCacheManager` (Room Database) para funcionamiento sin conexión.
- Retrofit y OkHttp para la comunicación con el backend Django.
- Modo claro pastel y modo oscuro OLED persistidos localmente.
- Sondeo de telemetría mientras la sesión está activa.

## Funcionalidades conectadas a la API

- Inicio de sesión con autenticación JWT.
- Registro de cuentas mediante `POST /api/auth/register/`.
- Consulta de módulos y macetas del usuario autenticado.
- Creación automática de una maceta virtual para cuentas sin módulos registrados.
- Búsqueda en el catálogo botánico de especies.
- Asignación y desvinculación de plantas en módulos.
- Lectura, envío e histórico de telemetría por módulo.
- Sincronización manual y automática con el backend Django.

## Funcionalidades locales

La API actual no expone un recurso persistente de actuadores. Por esa razón, los controles de bomba, nebulizador, dosis y parada de emergencia se gestionan como estado local en la interfaz.

La simulación de temperatura, humedad y luz se transmite al módulo activo mediante `POST /api/modules/<id>/telemetry/`, quedando almacenada en la telemetría de Django y conservándose al actualizar la aplicación.

Las alertas ambientales actuales operan como notificaciones locales mientras la aplicación se encuentra en ejecución.

## Compilación

Desde la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
```

El proyecto requiere un JDK compatible y el Android SDK configurado en `ANDROID_HOME` o mediante `local.properties`.
