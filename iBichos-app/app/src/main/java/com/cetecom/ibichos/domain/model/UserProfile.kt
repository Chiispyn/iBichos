package com.cetecom.ibichos.domain.model

import com.cetecom.ibichos.domain.model.enums.Gender

/**
 * Modelo de dominio para el perfil del usuario.
 *
 * Colección Firestore: users/{uid}
 * El sub-documento 'gamification' se guarda como Map anidado (ver GamificationData.kt).
 *
 * Sistema de Shadowban (moderación automatizada):
 *   - strikes: se incrementa +1 automáticamente si la IA rechaza una captura (probabilidad < 40%).
 *   - isShadowBanned: pasa a true al llegar a 3 strikes. El usuario puede seguir usando la app,
 *     pero sus capturas NO otorgan XP, NO actualizan el ranking y NO aparecen en el Dashboard.
 *     Esto evita que el usuario baneado se dé cuenta y cree cuentas alternativas.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val region: String = "",

    /** Antes era 'comuna'. Renombrado a 'city' para consistencia con inglés en el código. */
    val city: String = "",

    val birthDate: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val avatarUrl: String? = null,
    val totalCaptures: Int = 0,

    // --- Moderación automatizada (Shadowban) ---
    /** Contador de rechazos de la IA. Se suma +1 por captura con probabilidad < 40%. */
    val strikes: Int = 0,

    /**
     * Si es true, el usuario está en modo shadowban:
     * - Sus capturas se guardan en su historial (él las ve normalmente).
     * - No recibe XP ni medallas.
     * - No aparece en el ranking global.
     * - El Dashboard puede verlo y reactivarlo si es un falso positivo.
     */
    val isShadowBanned: Boolean = false,

    // --- Gamificación ---
    /** Sub-documento con toda la información de progreso y logros. */
    val gamification: GamificationData = GamificationData()
)
