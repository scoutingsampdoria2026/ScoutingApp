package com.scoutingsampdoria.persone.data.network

import com.scoutingsampdoria.persone.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // BASE_URL viene da app/build.gradle.kts (buildConfigField).
    // Cambiala lì se in futuro cambi dominio PythonAnywhere.
    private val baseUrl = BuildConfig.BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val api: PersoneApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PersoneApi::class.java)
    }

    /** Helper per costruire l'header Authorization nel formato corretto. */
    fun bearer(token: String) = "Bearer $token"
}
