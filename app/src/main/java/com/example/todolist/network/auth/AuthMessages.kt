package com.example.todolist.network.auth

import com.example.todolist.network.ErrorResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String
)

sealed class AuthResult {
    data class Success(val authResponse: AuthResponse) : AuthResult()
    data class Error(val errorResponse: ErrorResponse) : AuthResult()
    data class NetworkException(val exception: Throwable) : AuthResult()
}