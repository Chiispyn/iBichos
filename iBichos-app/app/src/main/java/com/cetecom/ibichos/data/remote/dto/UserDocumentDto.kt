package com.cetecom.ibichos.data.remote.dto

import com.google.firebase.Timestamp

/**
 * DTO de escritura para el documento de usuario en Firestore.
 *
 * Se construye en el repositorio antes de hacer .set() o .update(),
 * centralizando los nombres de campos en un solo lugar.
 */
data class UserDocumentDto(
    val displayName: String,
    val email: String,
    val region: String,
    val city: String,
    val birthDate: String,
    val gender: String,
    val avatarUrl: String? = null
) {
    fun toNewUserMap(): Map<String, Any?> = mapOf(
        "displayName"        to displayName,
        "email"              to email,
        "region"             to region,
        "city"               to city,
        "birthDate"          to birthDate,
        "gender"             to gender,
        "avatarUrl"          to avatarUrl,
        "totalCaptures"      to 0,
        "createdAt"          to Timestamp.now(),
        "strikes"            to 0,
        "isShadowBanned"     to false,
        "xp"                 to 0L,
        "uniqueInsectsCount" to 0,
        "medalsCount"        to 0,
        "gamification"       to initialGamificationMap()
    )

    companion object {
        fun initialGamificationMap(): Map<String, Any> = mapOf(
            "xp"                 to 0L,
            "level"              to "CASUAL",
            "uniqueInsectsCount" to 0,
            "medals"             to emptyList<String>(),
            "medalsEarnedAt"     to emptyMap<String, Long>(),
            "categoryCounts"     to emptyMap<String, Int>(),
            "levelUpAt"          to emptyMap<String, Long>()
        )
    }
}
