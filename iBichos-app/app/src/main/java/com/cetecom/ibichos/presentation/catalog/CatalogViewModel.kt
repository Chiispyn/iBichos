package com.cetecom.ibichos.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.data.repository.CaptureRepositoryImpl
import com.cetecom.ibichos.domain.model.CaptureItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val captures: List<CaptureItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CatalogViewModel : ViewModel() {

    private val auth       = FirebaseAuth.getInstance()
    private val repository = CaptureRepositoryImpl()

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCaptures()
    }

    fun loadCaptures() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val captures = repository.getCaptures(uid)
                _uiState.update { it.copy(captures = captures, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al cargar capturas: ${e.message}")
                }
            }
        }
    }
}

