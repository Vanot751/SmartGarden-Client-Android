package com.example.smartgarden.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

class TokenAuthenticator : Authenticator {

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath

        // Evitar bucle si la petición original ya era hacia endpoints de autenticación
        if (path.contains("token") || path.contains("auth/login") || path.contains("auth/register")) {
            return null
        }

        // Si ya se intentó reautenticar más de 2 veces para esta misma solicitud, no reintentar
        if (responseCount(response) >= 2) {
            return null
        }

        synchronized(this) {
            val currentToken = TokenManager.getToken()
            val authHeader = response.request.header("Authorization")
            val tokenUsed = authHeader?.removePrefix("Bearer ")?.trim()

            // Si otro hilo ya renovó el token mientras este hilo esperaba en el lock,
            // simplemente reintentamos con el nuevo token sin llamar a refresh de nuevo.
            if (!currentToken.isNullOrEmpty() && currentToken != tokenUsed) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken.isNullOrEmpty()) {
                TokenManager.clearAll()
                return null
            }

            // Llamar al endpoint estándar SimpleJWT: POST /api/token/refresh/
            val requestJson = JsonObject().apply {
                addProperty("refresh", refreshToken)
            }.toString()

            val requestBody = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())

            val refreshRequest = Request.Builder()
                .url("${RetrofitClient.BASE_URL}token/refresh/")
                .post(requestBody)
                .build()

            val refreshResponse = try {
                refreshClient.newCall(refreshRequest).execute()
            } catch (_: Exception) {
                return null
            }

            if (!refreshResponse.isSuccessful) {
                refreshResponse.close()
                // Si el refresh token fue rechazado (por ejemplo, expiró tras 7 días o es inválido),
                // limpiamos la sesión para que el usuario sea redirigido a login
                TokenManager.clearAll()
                return null
            }

            val bodyString = refreshResponse.body?.string()
            refreshResponse.close()

            if (bodyString.isNullOrEmpty()) {
                return null
            }

            return try {
                val jsonObject = gson.fromJson(bodyString, JsonObject::class.java)
                val newAccessToken = jsonObject.get("access")?.asString
                val newRefreshToken = jsonObject.get("refresh")?.asString

                if (!newAccessToken.isNullOrEmpty()) {
                    TokenManager.updateTokens(newAccessToken, newRefreshToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
