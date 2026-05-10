package com.cetecom.ibichos.presentation.auth

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.auth.CompleteProfileUseCase
import com.cetecom.ibichos.domain.usecase.auth.GetLocationsUseCase
import com.cetecom.ibichos.domain.usecase.auth.LoginUseCase
import com.cetecom.ibichos.domain.usecase.auth.RegisterUseCase
import com.cetecom.ibichos.domain.usecase.auth.SignInWithGoogleUseCase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userId: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val completeProfileUseCase: CompleteProfileUseCase,
    private val getLocationsUseCase: GetLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _locations = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val locations: StateFlow<Map<String, List<String>>> = _locations.asStateFlow()

    init {
        _uiState.update { it.copy(userId = authRepository.getCurrentUserId()) }
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            runCatching { _locations.value = getLocationsUseCase() }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { loginUseCase(email, password) }
                .onSuccess {
                    _uiState.update { s ->
                        s.copy(isLoading = false, isSuccess = true, userId = authRepository.getCurrentUserId())
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {
        if (listOf(email, password, displayName, region, city, birthDate, gender).any { it.isBlank() }) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { registerUseCase(email, password, displayName, region, city, birthDate, gender) }
                .onSuccess { uid ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userId = uid) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun completeProfile(region: String, city: String, birthDate: String, gender: String) {
        val uid = authRepository.getCurrentUserId() ?: return
        if (listOf(region, city, birthDate, gender).any { it.isBlank() }) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { completeProfileUseCase(uid, region, city, birthDate, gender) }
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun checkProfileCompletion(onResult: (Boolean) -> Unit) {
        val uid = authRepository.getCurrentUserId() ?: return onResult(false)
        viewModelScope.launch {
            onResult(runCatching { authRepository.checkProfileCompletion(uid) }.getOrDefault(false))
        }
    }

    fun handleGoogleResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            setError("Inicio de sesión cancelado")
            return
        }
        try {
            val task    = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: run { setError("No se pudo obtener la identidad de Google"); return }
            signInWithGoogle(idToken)
        } catch (e: Exception) {
            setError("Error al conectar con Google")
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { signInWithGoogleUseCase(idToken) }
                .onSuccess { uid ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userId = uid) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error Firebase") }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    fun resetPassword(email: String) {
        authRepository.sendPasswordResetEmail(email)
    }

    fun setError(message: String) = _uiState.update { it.copy(error = message) }
    fun clearError()              = _uiState.update { it.copy(error = null) }
    fun resetSuccess()            = _uiState.update { it.copy(isSuccess = false) }
}
