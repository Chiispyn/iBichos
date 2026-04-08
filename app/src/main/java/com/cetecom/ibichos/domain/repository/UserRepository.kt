package com.cetecom.ibichos.domain.repository

import android.net.Uri
import com.cetecom.ibichos.domain.model.UserProfile

/**
 * Contrato para las operaciones sobre el perfil del usuario.
 */
interface UserRepository {
    /** Carga el perfil desde Firestore */
    suspend fun getUserProfile(uid: String): UserProfile

    /** Sube el avatar a Firebase Storage y actualiza la URL en Firestore */
    suspend fun updateAvatar(uid: String, uri: Uri): String

    /** Incrementa el XP y actualiza el nivel si corresponde */
    suspend fun incrementXp(uid: String, xpGain: Long)
}
