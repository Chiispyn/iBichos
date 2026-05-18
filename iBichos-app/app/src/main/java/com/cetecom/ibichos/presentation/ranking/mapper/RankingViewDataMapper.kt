package com.cetecom.ibichos.presentation.ranking.mapper

import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.presentation.ranking.RankingType
import com.cetecom.ibichos.presentation.ranking.viewdata.RankingItemViewData
import java.util.Locale

fun List<UserProfile>.toRankingViewData(
    type: RankingType,
    currentUserId: String?
): List<RankingItemViewData> = mapIndexed { index, profile ->
    RankingItemViewData(
        rank           = index + 1,
        uid            = profile.uid,
        displayName    = profile.displayName,
        avatarUrl      = profile.avatarUrl,
        valueFormatted = profile.formatValue(type),
        isCurrentUser  = profile.uid == currentUserId
    )
}

private fun UserProfile.formatValue(type: RankingType): String = when (type) {
    RankingType.XP     -> String.format(Locale("es"), "%,d XP", gamification.xp).replace(',', '.')
    RankingType.UNIQUE -> "${gamification.uniqueInsectsCount} especies"
    RankingType.MEDALS -> "${gamification.medals.size} medallas"
}
