package com.example.todolist.network

import com.example.todolist.ui.auth.AuthResult
import com.example.todolist.ui.auth.LoginRequest
import com.example.todolist.ui.auth.RegisterRequest
import javax.inject.Inject

interface AuthService {

    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(username: String, email: String, password: String): AuthResult

    class Impl @Inject constructor(private val authManager: AuthManager) : AuthService {
        override suspend fun login(email: String, password: String): AuthResult {
            return authManager(LoginRequest(email, password))
        }

        override suspend fun register(
            username: String,
            email: String,
            password: String
        ): AuthResult {
            return authManager(RegisterRequest(username, email, password))
        }

    }

}
