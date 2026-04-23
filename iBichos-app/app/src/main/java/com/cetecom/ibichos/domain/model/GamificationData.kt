package com.cetecom.ibichos.domain.model

import com.cetecom.ibichos.domain.model.enums.UserLevel

/**
 * Sub-documento de gamificación del usuario.
 *
 * En Firestore se guarda como un Map anidado dentro del documento Users/{uid}.
 * Esto permite leer solo el perfil básico del usuario sin descargar todo el historial
 * de medallas y niveles (optimización de lectura de datos).
 *
 * Estructura en Firestore:
 *   users/{uid}/gamification: {
 *     xp: 250,
 *     level: "AMATEUR",
 *     uniqueInsectsCount: 12,
 *     categoryCounts: { "ARACHNID": 5, "COLEOPTERA": 7 },
 *     medals: ["FIRST_CAPTURE", "ARACHNOLOGIST"],
 *     medalsEarnedAt: { "FIRST_CAPTURE": 1713820000000, "ARACHNOLOGIST": 1713900000000 },
 *     levelUpAt: { "AMATEUR": 1713850000000 }
 *   }
 */
data class GamificationData(
    val xp: Long = 0L,

    /** Nivel actual calculado desde la XP. Usar UserLevel.fromXp(xp) para actualizar. */
    val level: UserLevel = UserLevel.CASUAL,

    /** Total de especies únicas capturadas (sin repetir). */
    val uniqueInsectsCount: Int = 0,

    /**
     * Contador de capturas por categoría taxonómica.
     * Clave: nombre del InsectCategory (ej. "ARACHNID"), Valor: cantidad de capturas.
     * Se usa para calcular logros por categoría (ej. "Aracnólogo" al llegar a 10 ARACHNID).
     */
    val categoryCounts: Map<String, Int> = emptyMap(),

    /** Lista de IDs de medallas desbloqueadas (ej. "FIRST_CAPTURE", "ARACHNOLOGIST"). */
    val medals: List<String> = emptyList(),

    /**
     * Registro de cuándo se ganó cada medalla.
     * Clave: ID de la medalla, Valor: timestamp epoch en ms.
     */
    val medalsEarnedAt: Map<String, Long> = emptyMap(),

    /**
     * Registro histórico de cuándo el usuario subió a cada nivel.
     * Clave: nombre del UserLevel (ej. "AMATEUR"), Valor: timestamp epoch en ms.
     */
    val levelUpAt: Map<String, Long> = emptyMap()
)
