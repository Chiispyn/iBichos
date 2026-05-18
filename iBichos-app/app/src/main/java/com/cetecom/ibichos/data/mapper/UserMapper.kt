package com.cetecom.ibichos.data.mapper

import com.cetecom.ibichos.domain.model.GamificationData
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.Gender
import com.cetecom.ibichos.domain.model.enums.UserLevel
import com.google.firebase.firestore.DocumentSnapshot

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toUserProfile(captureCount: Int = 0): UserProfile {
    val gamMap = get("gamification") as? Map<String, Any> ?: emptyMap()

    val gamification = GamificationData(
        xp                 = (gamMap["xp"] as? Long) ?: 0L,
        level              = runCatching {
            UserLevel.valueOf((gamMap["level"] as? String) ?: UserLevel.CASUAL.name)
        }.getOrDefault(UserLevel.CASUAL),
        uniqueInsectsCount = ((gamMap["uniqueInsectsCount"] as? Long)?.toInt()) ?: 0,
        categoryCounts     = (gamMap["categoryCounts"] as? Map<String, Long>)
            ?.mapValues { it.value.toInt() } ?: emptyMap(),
        medals             = (gamMap["medals"] as? List<String>) ?: emptyList(),
        medalsEarnedAt     = (gamMap["medalsEarnedAt"] as? Map<String, Long>) ?: emptyMap(),
        levelUpAt          = (gamMap["levelUpAt"] as? Map<String, Long>) ?: emptyMap()
    )

    val gender = runCatching {
        Gender.valueOf(getString("gender") ?: Gender.UNSPECIFIED.name)
    }.getOrDefault(Gender.UNSPECIFIED)

    return UserProfile(
        uid            = id,
        displayName    = getString("displayName") ?: "Usuario",
        email          = getString("email") ?: "",
        region         = getString("region") ?: "",
        city           = getString("city") ?: "",
        birthDate      = getString("birthDate") ?: "",
        gender         = gender,
        avatarUrl      = getString("avatarUrl"),
        totalCaptures  = captureCount,
        strikes        = (getLong("strikes")?.toInt()) ?: 0,
        isShadowBanned = getBoolean("isShadowBanned") ?: false,
        gamification   = gamification
    )
}
