package com.cetecom.ibichos.domain.model.enums

/**
 * Niveles de progreso del usuario en la gamificación.
 * Los umbrales de XP para subir de nivel se definen en GamificationConfig.
 */
enum class UserLevel {
    CASUAL,
    AMATEUR,
    EXPLORER,
    ENTOMOLOGIST,
    BUG_MASTER;

    /** Texto a mostrar en la app (en español). */
    fun displayName(): String = when (this) {
        CASUAL       -> "Casual"
        AMATEUR      -> "Aficionado"
        EXPLORER     -> "Explorador"
        ENTOMOLOGIST -> "Entomólogo"
        BUG_MASTER   -> "Maestro de Bichos"
    }

    companion object {
        /**
         * Calcula el nivel correspondiente a la XP acumulada del usuario.
         * Delegado a [GamificationConfig]
         */
        fun fromXp(xp: Long): UserLevel = GamificationConfig.calculateLevel(xp)
    }
}
