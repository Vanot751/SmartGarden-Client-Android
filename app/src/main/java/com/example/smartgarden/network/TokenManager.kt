package com.example.smartgarden.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private const val PREFS_NAME = "secure_prefs"
    private const val TOKEN_KEY = "jwt_token"
    private const val REFRESH_TOKEN_KEY = "jwt_refresh_token"
    private const val USERNAME_KEY = "jwt_username"

    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences != null) return
        try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context.applicationContext,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        // Migración inteligente: Si no hay nombre de usuario guardado en el almacén seguro,
        // recuperamos el correo/usuario previamente recordado en smartgarden_prefs
        if (getUsername().isNullOrBlank()) {
            val appPrefs = context.applicationContext.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
            val savedEmail = appPrefs.getString("saved_email", null)
            if (!savedEmail.isNullOrBlank()) {
                saveUsername(savedEmail.trim())
            }
        }
    }

    fun saveSession(accessToken: String, refreshToken: String?, username: String?) {
        sharedPreferences?.edit()?.apply {
            putString(TOKEN_KEY, accessToken)
            if (!refreshToken.isNullOrEmpty()) {
                putString(REFRESH_TOKEN_KEY, refreshToken)
            }
            if (!username.isNullOrEmpty()) {
                putString(USERNAME_KEY, username)
            }
            apply()
        }
    }

    fun updateTokens(accessToken: String, refreshToken: String?) {
        sharedPreferences?.edit()?.apply {
            putString(TOKEN_KEY, accessToken)
            if (!refreshToken.isNullOrEmpty()) {
                putString(REFRESH_TOKEN_KEY, refreshToken)
            }
            apply()
        }
    }

    fun saveToken(token: String) {
        sharedPreferences?.edit()?.putString(TOKEN_KEY, token)?.apply()
    }

    fun getToken(): String? {
        return sharedPreferences?.getString(TOKEN_KEY, null)
    }

    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences?.edit()?.putString(REFRESH_TOKEN_KEY, refreshToken)?.apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences?.getString(REFRESH_TOKEN_KEY, null)
    }

    fun saveUsername(username: String) {
        sharedPreferences?.edit()?.putString(USERNAME_KEY, username)?.apply()
    }

    fun getUsername(): String? {
        return sharedPreferences?.getString(USERNAME_KEY, null)
    }

    fun clearToken() {
        clearAll()
    }

    fun clearAll() {
        sharedPreferences?.edit()?.apply {
            remove(TOKEN_KEY)
            remove(REFRESH_TOKEN_KEY)
            remove(USERNAME_KEY)
            apply()
        }
    }
}
