package com.cetecom.ibichos.presentation.ranking.viewdata

/**
 * Modelo de UI para un ítem del ranking.
 *
 * [valueFormatted] cambia según el tipo de ranking activo:
 *   - XP      → "1.200 XP"
 *   - UNIQUE  → "12 especies"
 *   - MEDALS  → "5 medallas"
 */
data class RankingItemViewData(
    val rank: Int,
    val uid: String,
    val displayName: String,
    val avatarUrl: String?,
    val levelLabel: String,
    val valueFormatted: String,
    val isCurrentUser: Boolean
)
