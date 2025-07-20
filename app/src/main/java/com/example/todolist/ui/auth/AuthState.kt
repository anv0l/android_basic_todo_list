package com.example.todolist.ui.auth

enum class AuthState(private val state: Int, private val description: String) {
    NOT_LOGGED_IN(0, "Not logged in"),
    AUTHENTICATED(1, "Authenticated"),
    WRONG_CREDENTIAL(2, "Wrong credentials")
}