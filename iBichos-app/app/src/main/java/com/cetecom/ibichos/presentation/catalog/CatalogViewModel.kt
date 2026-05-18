package com.cetecom.ibichos.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.usecase.capture.DeleteCaptureUseCase
import com.cetecom.ibichos.domain.usecase.capture.GetCapturesUseCase
import com.cetecom.ibichos.presentation.catalog.mapper.toViewDataList
import com.cetecom.ibichos.presentation.catalog.viewdata.CaptureViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val captures: List<CaptureViewData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val captureRepository: CaptureRepository,
    private val getCapturesUseCase: GetCapturesUseCase,
    private val deleteCaptureUseCase: DeleteCaptureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init { loadCaptures() }

    fun loadCaptures() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getCapturesUseCase(uid) }
                .onSuccess { captures ->
                    _uiState.update { it.copy(captures = captures.toViewDataList(), isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar capturas: ${e.message}") }
                }
        }
    }

    fun deleteCapture(id: String) {
        viewModelScope.launch {
            runCatching { deleteCaptureUseCase(id) }
                .onSuccess { loadCaptures() }
                .onFailure { e -> _uiState.update { it.copy(error = "No se pudo eliminar: ${e.message}") } }
        }
    }

    fun appealCapture(id: String) {
        viewModelScope.launch {
            runCatching { captureRepository.appealCapture(id) }
                .onSuccess { loadCaptures() }
                .onFailure { e -> _uiState.update { it.copy(error = "No se pudo apelar: ${e.message}") } }
        }
    }
}
