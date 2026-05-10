package com.cetecom.ibichos.domain.repository

import com.cetecom.ibichos.domain.model.UserProfile

/**
 * Contrato para las operaciones sobre el perfil del usuario.
 */
interface UserRepository {
    /** Carga el perfil desde Firestore */
    suspend fun getUserProfile(uid: String): UserProfile

    /** Actualiza la URL del avatar en Firestore y la devuelve */
    suspend fun updateAvatar(uid: String, avatarUrl: String): String

    /** Incrementa el XP y actualiza el nivel si corresponde */
    suspend fun incrementXp(uid: String, xpGain: Long)

    /** Obtiene el top de usuarios ordenados por mayor XP */
    suspend fun getTopUsersByXp(limit: Int = 50): List<UserProfile>

    /** Obtiene el top de usuarios ordenados por mayor cantidad de especies únicas */
    suspend fun getTopUsersByUniqueInsects(limit: Int = 50): List<UserProfile>

    /** Obtiene el top de usuarios ordenados por cantidad de medallas desbloqueadas */
    suspend fun getTopUsersByMedals(limit: Int = 50): List<UserProfile>

    /** Desbloquea medallas e incrementa el contador de especies únicas descubiertas */
    suspend fun unlockMedalsAndIncrementUnique(uid: String, medalsToUnlock: List<String>, isNewInsect: Boolean, incrementCategory: String?)
}
