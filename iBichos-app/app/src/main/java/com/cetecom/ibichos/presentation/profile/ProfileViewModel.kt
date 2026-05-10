package com.cetecom.ibichos.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.profile.GetUserProfileUseCase
import com.cetecom.ibichos.domain.usecase.profile.UploadAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getUserProfileUseCase(uid) }
                .onSuccess { profile ->
                    _uiState.update { it.copy(profile = profile, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar perfil: ${e.message}") }
                }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            runCatching {
                // Copiar Uri a File temporal (operación Android-específica, permanece en el ViewModel)
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se pudo abrir la imagen")
                val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                FileOutputStream(tempFile).use { inputStream.copyTo(it) }

                uploadAvatarUseCase(uid, tempFile)
            }
                .onSuccess { url ->
                    _uiState.update { state ->
                        state.copy(
                            isUploadingAvatar = false,
                            profile = state.profile?.copy(avatarUrl = url),
                            successMessage = "¡Avatar actualizado en la nube!"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingAvatar = false, error = "Error al subir foto: ${e.message}") }
                }
        }
    }

    fun logout() = authRepository.signOut()

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}
