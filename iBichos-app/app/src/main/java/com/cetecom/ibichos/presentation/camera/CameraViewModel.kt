package com.cetecom.ibichos.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.capture.AnalyzeCaptureUseCase
import com.cetecom.ibichos.domain.usecase.capture.CaptureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

sealed interface CameraUiState {
    object Idle : CameraUiState
    object Loading : CameraUiState
    data class Preview(val bitmap: Bitmap, val lat: Double?, val lon: Double?) : CameraUiState
    data class Success(val message: String) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val analyzeCaptureUseCase: AnalyzeCaptureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun setLoading()  { _uiState.value = CameraUiState.Loading }
    fun setPreview(bitmap: Bitmap, lat: Double?, lon: Double?) { _uiState.value = CameraUiState.Preview(bitmap, lat, lon) }
    fun setError(message: String)  { _uiState.value = CameraUiState.Error(message) }
    fun resetState()  { _uiState.value = CameraUiState.Idle }

    fun processCapture(bitmap: Bitmap, lat: Double?, lon: Double?) {
        val uid = authRepository.getCurrentUserId() ?: run {
            _uiState.value = CameraUiState.Error("No hay usuario autenticado")
            return
        }

        val imageBytes = ByteArrayOutputStream()
            .also { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }
            .toByteArray()

        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            runCatching { analyzeCaptureUseCase(uid, imageBytes, lat, lon) }
                .onSuccess { result ->
                    _uiState.value = when (result) {
                        is CaptureResult.Success      -> CameraUiState.Success(result.message)
                        is CaptureResult.LowConfidence -> CameraUiState.Error(
                            "La IA no pudo identificar un insecto con suficiente certeza " +
                            "(${(result.probability * 100).toInt()}%). Intenta con otra foto con mejor iluminación."
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.value = CameraUiState.Error("Error: ${e.message}")
                }
        }
    }
}
