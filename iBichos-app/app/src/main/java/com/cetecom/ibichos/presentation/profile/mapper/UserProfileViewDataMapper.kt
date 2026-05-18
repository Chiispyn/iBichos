package com.cetecom.ibichos.presentation.profile.mapper

import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.presentation.profile.viewdata.UserProfileViewData
import java.util.Locale

fun UserProfile.toViewData(): UserProfileViewData = UserProfileViewData(
    uid                 = uid,
    displayName         = displayName,
    avatarUrl           = avatarUrl,
    region              = region,
    city                = city,
    xp                  = gamification.xp,
    xpFormatted         = gamification.xp.toFormattedXp(),
    levelLabel          = gamification.level.displayName(),
    medals              = gamification.medals,
    medalsEarnedAt      = gamification.medalsEarnedAt,
    medalsCount         = gamification.medals.size,
    uniqueInsectsCount  = gamification.uniqueInsectsCount,
    totalCaptures       = totalCaptures,
    isShadowBanned      = isShadowBanned
)

private fun Long.toFormattedXp(): String =
    String.format(Locale("es"), "%,d XP", this).replace(',', '.')
