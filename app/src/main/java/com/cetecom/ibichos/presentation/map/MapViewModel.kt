package com.cetecom.ibichos.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.data.repository.CaptureRepositoryImpl
import com.cetecom.ibichos.domain.model.CaptureItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val auth       = FirebaseAuth.getInstance()
    private val repository = CaptureRepositoryImpl()

    private val _captures = MutableStateFlow<List<CaptureItem>>(emptyList())
    val captures: StateFlow<List<CaptureItem>> = _captures.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCaptures()
    }

    private fun loadCaptures() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                _captures.value = repository.getCaptures(uid)
            } catch (e: Exception) {
                // Silencioso — el mapa simplemente no muestra pines
            } finally {
                _isLoading.value = false
            }
        }
    }
}
