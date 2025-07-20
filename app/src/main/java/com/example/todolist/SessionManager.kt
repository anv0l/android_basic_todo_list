package com.example.todolist

import javax.inject.Inject

interface SessionManager {
    fun getToken(): String

    class Impl @Inject constructor() : SessionManager {
        override fun getToken(): String {
            return "empty key"
        }
    }
}