package com.example.todolist.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.EncryptedPrefsRepository
import com.example.todolist.network.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val encryptedPrefsRepository: EncryptedPrefsRepository,
    private val authService: AuthService
) :
    ViewModel() {
    val userCredential = encryptedPrefsRepository.userCredentials
    val authState = encryptedPrefsRepository.authState

    private val _mode = MutableStateFlow<AuthMode>(AuthMode.LOGIN)
    val mode = _mode.asStateFlow()

    private val _errorDetail = MutableStateFlow<String?>(null)
    val errorDetail = _errorDetail.asStateFlow()

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.READY)
    val networkState = _networkState.asStateFlow()

    fun initCredentials() {
        viewModelScope.launch {
            encryptedPrefsRepository.initCredentials()
        }
    }

    fun initAuthState() {
        viewModelScope.launch {
            encryptedPrefsRepository.initAuthState()
        }
    }

    fun saveCredentials(user: String, password: String) {
        viewModelScope.launch {
            encryptedPrefsRepository.saveCredentials(user, password)
        }
    }

    fun setAuthState(authState: AuthState) {
        viewModelScope.launch {
            encryptedPrefsRepository.setAuthState(authState)
        }

    }

    fun clearCredentials() {
        viewModelScope.launch {
            encryptedPrefsRepository.clearCredentials()
        }
    }

    fun toggleMode() {
        _mode.value = if (_mode.value == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
    }

    private var loginJob: Job? = null
    fun login(email: String, password: String) {
        loginJob?.cancel()
        _networkState.value = NetworkState.READY

        viewModelScope.launch {
            encryptedPrefsRepository.saveCredentials(email, password)
        }

        loginJob = CoroutineScope(Dispatchers.IO).launch {
            _networkState.value = NetworkState.LOADING
            val result = authService.login(email, password)
            when (result) {
                is AuthResult.Success -> {
                    encryptedPrefsRepository.saveToken(result.authResponse.accessToken)
                    setAuthState(AuthState.AUTHENTICATED)
                    _errorDetail.value = ""
                }

                is AuthResult.Error -> {
                    _errorDetail.value = result.errorResponse.detail
                    setAuthState(AuthState.NOT_LOGGED_IN)
                }

                is AuthResult.NetworkException -> {
                    _errorDetail.value = result.exception.message ?: "Network error"
                    setAuthState(AuthState.NOT_LOGGED_IN)
                }

            }

        }
    }

}

enum class NetworkState {
    LOADING,
    READY,
    NOT_AVAILABLE
}