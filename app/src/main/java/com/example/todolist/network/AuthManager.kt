package com.example.todolist.network

import com.example.todolist.ui.auth.AuthResponse
import com.example.todolist.ui.auth.AuthResult
import com.example.todolist.ui.auth.ErrorResponse
import com.example.todolist.ui.auth.LoginRequest
import com.example.todolist.ui.auth.RegisterRequest
import kotlinx.serialization.json.Json
import retrofit2.Response
import javax.inject.Inject

interface AuthManager {
    suspend operator fun invoke(loginRequest: LoginRequest): AuthResult
    suspend operator fun invoke(registerRequest: RegisterRequest): AuthResult

    class Impl @Inject constructor(private val apiAuth: ApiAuth) : AuthManager {
        override suspend fun invoke(loginRequest: LoginRequest): AuthResult {
            val response = apiAuth.login(loginRequest)
            return parseResponse(response)
        }

        override suspend fun invoke(registerRequest: RegisterRequest): AuthResult {
            val response = apiAuth.register(registerRequest)
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