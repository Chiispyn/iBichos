package com.cetecom.ibichos.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.profile.GetUserProfileUseCase
import com.cetecom.ibichos.domain.usecase.ranking.GetRankingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RankingType { XP, UNIQUE, MEDALS }

data class RankingUiState(
    val isLoading: Boolean = false,
    val users: List<UserProfile> = emptyList(),
    val error: String? = null,
    val currentType: RankingType = RankingType.XP,
    val currentUserId: String? = null,
    val currentUserProfile: UserProfile? = null,
    val currentUserRank: Int? = null
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getRankingUseCase: GetRankingUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking(RankingType.XP)
    }

    fun loadRanking(type: RankingType = _uiState.value.currentType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentType = type) }
            val currentUid = authRepository.getCurrentUserId()
            runCatching {
                val currentProfile = currentUid?.let {
                    runCatching { getUserProfileUseCase(it) }.getOrNull()
                }
                val topUsers = getRankingUseCase(type)
                val currentRank = currentUid?.let { uid ->
                    topUsers.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }?.plus(1)
                }
                Triple(topUsers, currentProfile, currentRank)
            }
                .onSuccess { (topUsers, currentProfile, currentRank) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = topUsers,
                            currentUserId = currentUid,
                            currentUserProfile = currentProfile,
                            currentUserRank = currentRank
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar el ranking: ${e.message}") }
                }
        }
    }
}
