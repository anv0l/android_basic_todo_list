package com.example.todolist.network

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val detail: String
)
