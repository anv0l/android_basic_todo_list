package com.example.todolist.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.EncryptedPrefsRepository
import com.example.todolist.data.repository.PrefsRepository
import com.example.todolist.network.NetworkHelper
import com.example.todolist.network.auth.AuthResult
import com.example.todolist.network.auth.AuthService
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
    private val authService: AuthService,
    private val prefsRepository: PrefsRepository,
    private val networkHelper: NetworkHelper
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

    // I don't think we need to store token somewhere outside viewmodel,
    // because in reality it doesn't live long enough, just refresh it if not available
    private val _accessToken = MutableStateFlow<String>("")
    val accessToken = _accessToken.asStateFlow() // TODO: remove, for debug only

    private fun saveToken(token: String) {
        _accessToken.value = token
    }

    private fun invalidateToken() {
        _accessToken.value = ""
    }


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

    fun logout() {
        viewModelScope.launch {
            encryptedPrefsRepository.logout()
            invalidateToken()
        }
    }

    fun toggleMode() {
        _mode.value = if (_mode.value == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
    }

    private var loginJob: Job? = null
    fun login(email: String, password: String) {
        if (_networkState.value == NetworkState.LOADING) return

        loginJob?.cancel()

        saveCredentials(email, password)

        loginJob = CoroutineScope(Dispatchers.IO).launch {
            _networkState.value = NetworkState.LOADING
            val result = networkHelper.safeApiCall { authService.login(email, password) }

            if (result.isSuccess) {
                val loginResult = result.getOrNull()
                if (loginResult != null) {
                    when (loginResult) {
                        is AuthResult.Success -> {
                            saveToken(loginResult.authResponse.accessToken)
                            setAuthState(AuthState.AUTHENTICATED)
                            _errorDetail.value = ""
                            prefsRepository.toggleSync()
                        }

                        is AuthResult.Error -> {
                            _errorDetail.value = loginResult.errorResponse.detail
                            setAuthState(AuthState.NOT_LOGGED_IN)
                            prefsRepository.disableSync()
                        }

                        is AuthResult.NetworkException -> {
                            _errorDetail.value = loginResult.exception.message ?: "Network error"
                            _networkState.value = NetworkState.NOT_AVAILABLE
                            prefsRepository.disableSync()
                            // Shouldn't change auth state due to network error
                        }
                    }
                } else {
                    _networkState.value = NetworkState.NOT_AVAILABLE
                    _errorDetail.value = "Empty response"
                }
                _networkState.value =
                    if (_networkState.value == NetworkState.LOADING) NetworkState.READY else _networkState.value
            } else {
                _networkState.value = NetworkState.NOT_AVAILABLE
                _errorDetail.value = result.exceptionOrNull()!!.message
            }
        }
    }

    private var registerJob: Job? = null
    fun register(email: String, password: String) {
        if (_networkState.value == NetworkState.LOADING) return

        registerJob?.cancel()

        saveCredentials(email, password)

        registerJob = CoroutineScope(Dispatchers.IO).launch {
            _networkState.value = NetworkState.LOADING
            val result = networkHelper.safeApiCall { authService.register(email, email, password) }

            if (result.isSuccess) {
                val authResult = result.getOrNull()
                if (authResult != null) {
                    when (authResult) {
                        is AuthResult.Success -> {
                            saveToken(authResult.authResponse.accessToken)
                            setAuthState(AuthState.AUTHENTICATED)
                            _errorDetail.value = ""
                            prefsRepository.toggleSync()
                        }

                        is AuthResult.Error -> {
                            _errorDetail.value = authResult.errorResponse.detail
                            setAuthState(AuthState.NOT_LOGGED_IN)
                            prefsRepository.disableSync()
                        }

                        is AuthResult.NetworkException -> {
                            _errorDetail.value = authResult.exception.message ?: "Network error"
                            _networkState.value = NetworkState.NOT_AVAILABLE
                            prefsRepository.disableSync()
                        }
                    }
                    _networkState.value =
                        if (_networkState.value == NetworkState.LOADING) NetworkState.READY else _networkState.value
                } else {
                    _networkState.value = NetworkState.NOT_AVAILABLE
                    _errorDetail.value = "Empty response"
                }
            } else {
                _networkState.value = NetworkState.NOT_AVAILABLE
                _errorDetail.value = result.exceptionOrNull()!!.message
            }
        }
    }

}

enum class NetworkState {
    LOADING,
    READY,
    NOT_AVAILABLE
}