package com.example.todolist.network.auth

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

private const val authUrl =
    "http://192.168.88.92:8000/auth/"

interface AuthApi {
    @POST("register")
    suspend fun register(@Body query: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body query: LoginRequest): Response<AuthResponse>
}

fun buildRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val json = Json { ignoreUnknownKeys = true }
    return Retrofit.Builder()
        .baseUrl(authUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}