package com.example.smartgarden.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("django_access_token", null)

        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

object NetworkClient {

    // URL base por defecto para el Emulador de Android Studio hacia el localhost de la PC
    private const val DEFAULT_EMULATOR_BASE_URL = "http://10.0.2.2:8000/api/"

    private var currentBaseUrl = DEFAULT_EMULATOR_BASE_URL
    private var apiServiceInstance: DjangoApiService? = null

    fun getApiService(context: Context, customBaseUrl: String? = null): DjangoApiService {
        val baseUrlToUse = customBaseUrl?.ifEmpty { null } ?: currentBaseUrl

        if (apiServiceInstance == null || currentBaseUrl != baseUrlToUse) {
            currentBaseUrl = baseUrlToUse

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(4, TimeUnit.SECONDS)
                .writeTimeout(4, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiServiceInstance = retrofit.create(DjangoApiService::class.java)
        }

        return apiServiceInstance!!
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("django_access_token", accessToken)
            .putString("django_refresh_token", refreshToken)
            .apply()
    }

    fun clearTokens(context: Context) {
        context.getSharedPreferences("smartgarden_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("django_access_token")
            .remove("django_refresh_token")
            .apply()
    }
}
