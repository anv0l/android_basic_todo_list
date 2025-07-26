package com.example.todolist.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.todolist.ui.auth.AuthState
import com.example.todolist.ui.common.helpers.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// TODO: replace with something more suitable
class EncryptedPrefsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val CREDENTIALS_EMAIL = stringPreferencesKey("credentials_email")
    private val CREDENTIALS_PASSWORD = stringPreferencesKey("credentials_password")
    private val AUTH_STATE = stringPreferencesKey("auth_state")

    private val _userCredentials = MutableStateFlow<Credentials?>(null)
    val userCredentials = _userCredentials.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.NOT_LOGGED_IN)
    val authState = _authState.asStateFlow()

    suspend fun setAuthState(authState: AuthState) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_STATE] = authState.name
        }
        _authState.value = authState
    }

    suspend fun initAuthState() {
        context.dataStore.data.map { prefs ->
            prefs[AUTH_STATE]
        }.collect { value ->
            _authState.value = when {
                value == AuthState.WRONG_CREDENTIAL.name -> AuthState.WRONG_CREDENTIAL
                value == AuthState.AUTHENTICATED.name -> AuthState.AUTHENTICATED
                else -> AuthState.NOT_LOGGED_IN
            }
        }
    }

    suspend fun initCredentials() {
        context.dataStore.data.map { prefs ->
            Pair(prefs[CREDENTIALS_EMAIL] ?: "", prefs[CREDENTIALS_PASSWORD] ?: "")
        }.collect { value ->
            _userCredentials.value = Credentials(value.first, value.second)
        }
    }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[CREDENTIALS_EMAIL] = email
            prefs[CREDENTIALS_PASSWORD] = password
        }
        _userCredentials.value = Credentials(email, password)
    }

    private suspend fun clearCredentials() {
        saveCredentials("", "")
    }

    suspend fun logout() {
        setAuthState(AuthState.NOT_LOGGED_IN)
        clearCredentials()
    }

}

data class Credentials(
    val email: String,
    val password: String
)