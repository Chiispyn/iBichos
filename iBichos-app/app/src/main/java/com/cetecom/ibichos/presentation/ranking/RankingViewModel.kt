package com.cetecom.ibichos.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cetecom.ibichos.domain.repository.AuthRepository
import com.cetecom.ibichos.domain.usecase.profile.GetUserProfileUseCase
import com.cetecom.ibichos.domain.usecase.ranking.GetRankingUseCase
import com.cetecom.ibichos.presentation.ranking.mapper.toRankingViewData
import com.cetecom.ibichos.presentation.ranking.viewdata.RankingItemViewData
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
    val items: List<RankingItemViewData> = emptyList(),
    val currentType: RankingType = RankingType.XP,
    val currentUserRank: Int? = null,
    val error: String? = null
)

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getRankingUseCase: GetRankingUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init { loadRanking(RankingType.XP) }

    fun loadRanking(type: RankingType = _uiState.value.currentType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentType = type) }
            val currentUid = authRepository.getCurrentUserId()
            runCatching { getRankingUseCase(type) }
                .onSuccess { topUsers ->
                    val items = topUsers.toRankingViewData(type, currentUid)
                    val currentUserRank = items.firstOrNull { it.isCurrentUser }?.rank
                    _uiState.update {
                        it.copy(isLoading = false, items = items, currentUserRank = currentUserRank)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar el ranking: ${e.message}") }
                }
        }
    }
}
