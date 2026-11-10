package com.example.smartgarden.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Conexión a la API desplegada en Vercel (HTTPS)
    const val BASE_URL = "https://smart-garden-api-django.vercel.app/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Nivel NONE para seguridad en producción (evita filtrar tokens JWT en Logcat)
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .authenticator(TokenAuthenticator())
        .addInterceptor(loggingInterceptor)
        .build()

    val api: SmartGardenApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SmartGardenApi::class.java)
    }
}
