package com.cetecom.ibichos.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.data.repository.UserRepositoryImpl
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RankingType {
    XP, UNIQUE, MEDALS
}

data class RankingUiState(
    val isLoading: Boolean = false,
    val users: List<UserProfile> = emptyList(),
    val error: String? = null,
    val currentType: RankingType = RankingType.XP,
    val currentUserId: String? = null,
    val currentUserProfile: UserProfile? = null,
    val currentUserRank: Int? = null
)

class RankingViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking(RankingType.XP)
    }

    fun loadRanking(type: RankingType = _uiState.value.currentType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentType = type) }
            try {
                val currentUid = auth.currentUser?.uid
                val currentProfile = if (currentUid != null) {
                    try { userRepository.getUserProfile(currentUid) } catch (e: Exception) { null }
                } else null

                val topUsers = when (type) {
                    RankingType.XP -> userRepository.getTopUsersByXp(50)
                    RankingType.UNIQUE -> userRepository.getTopUsersByUniqueInsects(50)
                    RankingType.MEDALS -> userRepository.getTopUsersByMedals(50)
                }
                
                val currentRank = if (currentUid != null) {
                    val index = topUsers.indexOfFirst { it.uid == currentUid }
                    if (index >= 0) index + 1 else null
                } else null

                _uiState.update { it.copy(
                    isLoading = false, 
                    users = topUsers,
                    currentUserId = currentUid,
                    currentUserProfile = currentProfile,
                    currentUserRank = currentRank
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = "Error al cargar el ranking: ${e.message}"
                )}
            }
        }
    }
}

