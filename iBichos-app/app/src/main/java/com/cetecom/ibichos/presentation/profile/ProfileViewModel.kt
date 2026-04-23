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
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.cetecom.ibichos.data.repository.CloudinaryModule

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

    fun uploadAvatar(uri: Uri, context: Context) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            try {
                // 1. Copiar Uri a File temporal
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri) 
                    ?: throw Exception("No se pudo abrir la imagen")
                
                val filename = UUID.randomUUID().toString() + ".jpg"
                val tempFile = File(context.cacheDir, filename)
                
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }

                // 2. Subir a Cloudinary
                val requestFile = tempFile.asRequestBody("image/*".toMediaType())
                val filePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                val presetBody = "IBichos".toRequestBody("text/plain".toMediaType())

                val cloudinaryResponse = CloudinaryModule.api.uploadImage(
                    cloudName = "drubfka1z",
                    file = filePart,
                    uploadPreset = presetBody
                )

                val cloudinaryUrl = cloudinaryResponse.secure_url
                    ?: throw Exception("Cloudinary no devolvió una URL válida")

                // 3. Guardar la URL remota en Firestore
                val finalUrl = repository.updateAvatar(uid, Uri.parse(cloudinaryUrl))
                
                _uiState.update { state ->
                    state.copy(
                        isUploadingAvatar = false,
                        profile = state.profile?.copy(avatarUrl = finalUrl),
                        successMessage = "¡Avatar actualizado en la nube!"
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

