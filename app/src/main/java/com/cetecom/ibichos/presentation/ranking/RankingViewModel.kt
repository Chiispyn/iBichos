package com.cetecom.ibichos.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val isLoading: Boolean = false,
    val users: List<UserProfile> = emptyList(),
    val error: String? = null
)

class RankingViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking()
    }

    fun loadRanking() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch top 50 users sorted by XP
                val topUsers = userRepository.getTopUsersByXp(50)
                _uiState.value = _uiState.value.copy(isLoading = false, users = topUsers)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el ranking: ${e.message}"
                )
            }
        }
    }
}

