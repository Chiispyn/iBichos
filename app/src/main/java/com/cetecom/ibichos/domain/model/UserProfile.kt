package com.cetecom.ibichos.domain.model

/**
 * Modelo de dominio para el perfil del usuario.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val xp: Long = 0L,
    val level: String = "Casual",
    val avatarUrl: String? = null,
    val totalCaptures: Int = 0
)
