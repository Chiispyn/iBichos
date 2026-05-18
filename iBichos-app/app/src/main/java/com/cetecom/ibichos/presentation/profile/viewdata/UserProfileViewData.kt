package com.cetecom.ibichos.presentation.profile.viewdata

/**
 * Modelo de UI para el perfil del usuario.
 *
 * Expone solo lo que la pantalla necesita mostrar,
 * con valores ya formateados para no contaminar los Composables con lógica.
 */
data class UserProfileViewData(
    val uid: String,
    val displayName: String,
    val avatarUrl: String?,
    val region: String,
    val city: String,
    val xpFormatted: String,            // "1.200 XP"
    val levelLabel: String,             // "Explorador"
    val medalsCount: Int,
    val uniqueInsectsCount: Int,
    val totalCaptures: Int,
    val isShadowBanned: Boolean
)
