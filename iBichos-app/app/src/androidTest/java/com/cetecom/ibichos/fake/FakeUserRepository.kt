package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.model.GamificationData
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.UserLevel
import com.cetecom.ibichos.domain.repository.UserRepository

/**
 * Implementación falsa de UserRepository para tests de UI.
 * Devuelve 3 usuarios fake con distintos niveles para el ranking.
 */
class FakeUserRepository : UserRepository {

    val fakeUsers = listOf(
        // test_uid va primero → rank 1, siempre visible, se muestra como "Tú"
        UserProfile(
            uid = "test_uid",
            displayName = "Jugador Test",
            gamification = GamificationData(xp = 1500L, level = UserLevel.EXPLORER, uniqueInsectsCount = 15, medals = listOf("FIRST_CAPTURE", "ARACHNOLOGIST"))
        ),
        UserProfile(
            uid = "rank_uid_1",
            displayName = "Jugador Alfa",
            gamification = GamificationData(xp = 1200L, level = UserLevel.AMATEUR, uniqueInsectsCount = 10, medals = listOf("FIRST_CAPTURE"))
        ),
        UserProfile(
            uid = "rank_uid_2",
            displayName = "Jugador Beta",
            gamification = GamificationData(xp = 900L, level = UserLevel.CASUAL, uniqueInsectsCount = 5, medals = emptyList())
        )
    )

    override suspend fun getUserProfile(uid: String): UserProfile =
        fakeUsers.find { it.uid == uid } ?: UserProfile(uid = uid)

    override suspend fun updateAvatar(uid: String, avatarUrl: String): String = avatarUrl

    override suspend fun incrementXp(uid: String, xpGain: Long) {}

    override suspend fun getTopUsersByXp(limit: Int): List<UserProfile> = fakeUsers

    override suspend fun getTopUsersByUniqueInsects(limit: Int): List<UserProfile> =
        fakeUsers.sortedByDescending { it.gamification.uniqueInsectsCount }

    override suspend fun getTopUsersByMedals(limit: Int): List<UserProfile> =
        fakeUsers.sortedByDescending { it.gamification.medals.size }

    override suspend fun unlockMedalsAndIncrementUnique(
        uid: String,
        medalsToUnlock: List<String>,
        isNewInsect: Boolean,
        incrementCategory: String?
    ) {}
}
