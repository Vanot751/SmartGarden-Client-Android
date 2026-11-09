package com.example.smartgarden.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Ahora que tu backend está corriendo en 0.0.0.0:8000, podemos usar
    // directamente la IP de tu red local que sí es visible para el emulador.
    private const val BASE_URL = "http://192.168.1.115:8000/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Nivel BODY para ver las peticiones y respuestas en el Logcat
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
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
