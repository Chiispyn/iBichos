package com.cetecom.ibichos.domain.usecase.ranking

import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.UserRepository
import com.cetecom.ibichos.presentation.ranking.RankingType
import javax.inject.Inject

class GetRankingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(type: RankingType, limit: Int = 50): List<UserProfile> =
        when (type) {
            RankingType.XP     -> userRepository.getTopUsersByXp(limit)
            RankingType.UNIQUE -> userRepository.getTopUsersByUniqueInsects(limit)
            RankingType.MEDALS -> userRepository.getTopUsersByMedals(limit)
        }
}
