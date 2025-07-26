package com.example.todolist.network.auth

import com.example.todolist.network.ErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject

interface AuthManager {
    suspend operator fun invoke(loginRequest: LoginRequest): AuthResult
    suspend operator fun invoke(registerRequest: RegisterRequest): AuthResult

    class Impl @Inject constructor(private val authApi: AuthApi) : AuthManager {
        override suspend fun invoke(loginRequest: LoginRequest): AuthResult {
            val response = authApi.login(loginRequest)
            return parseResponse(response)
        }

        override suspend fun invoke(registerRequest: RegisterRequest): AuthResult {
            val response = authApi.register(registerRequest)
            return parseResponse(response)
        }
    }
}

private fun parseResponse(response: Response<AuthResponse>): AuthResult {
    return if (response.isSuccessful) {
        if (response.body() != null)
            AuthResult.Success(response.body()!!)
        else
            AuthResult.NetworkException(java.lang.Exception("Empty body response"))
    } else {
        AuthResult.Error(Json.decodeFromString<ErrorResponse>(response.errorBody()?.string() ?: ""))
    }
}