package com.cetecom.ibichos.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.map.GetMapCapturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val captures: List<CaptureItem> = emptyList(),
    val isLoading: Boolean = false,
    val isGlobalMap: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getMapCapturesUseCase: GetMapCapturesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadCaptures()
    }

    fun setGlobalMode(isGlobal: Boolean) {
        if (_uiState.value.isGlobalMap == isGlobal) return
        _uiState.update { it.copy(isGlobalMap = isGlobal) }
        loadCaptures()
    }

    fun loadCaptures() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getMapCapturesUseCase(uid, _uiState.value.isGlobalMap) }
                .onSuccess { captures ->
                    _uiState.update { it.copy(captures = captures, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}
