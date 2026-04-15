package com.cetecom.ibichos.domain.model

/**
 * Modelo de dominio para el perfil del usuario.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val region: String = "",
    val comuna: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val xp: Long = 0L,
    val level: String = "Casual",
    val avatarUrl: String? = null,
    val totalCaptures: Int = 0,
    val medals: List<String> = emptyList(),
    val uniqueInsectsCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap()
)
