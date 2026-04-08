package com.cetecom.ibichos.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.cetecom.ibichos.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val auth       = FirebaseAuth.getInstance()
    private val repository = UserRepositoryImpl()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val profile = repository.getUserProfile(uid)
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar perfil: ${e.message}")
                }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            try {
                val newUrl = repository.updateAvatar(uid, uri)
                _uiState.update { state ->
                    state.copy(
                        isUploadingAvatar = false,
                        profile = state.profile?.copy(avatarUrl = newUrl),
                        successMessage = "¡Avatar actualizado!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploadingAvatar = false, error = "Error al subir foto: ${e.message}")
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
