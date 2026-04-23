package com.cetecom.ibichos.domain.model

/**
 * Representa un evento de gamificación registrado como log inmutable en Firestore.
 *
 * Guardado en: events/{auto-id}
 *
 * Permite al Dashboard responder preguntas históricas como:
 *   - "¿Qué nivel tenía Pedro hace 3 meses?"
 *   - "¿Cuántos usuarios subieron de nivel en enero?"
 *   - "¿Cuántas especies nuevas se descubrieron la semana pasada?"
 *
 * Es un log append-only — nunca se edita ni borra.
 */
data class UserEvent(
    val eventId: String = "",

    /** UID del usuario que generó el evento. */
    val userId: String = "",

    /**
     * Tipo de evento. Valores posibles:
     *   LEVEL_UP          — el usuario subió de nivel
     *   MEDAL_UNLOCKED    — el usuario desbloqueó un logro
     *   SPECIES_DISCOVERED — el usuario capturó una especie nueva
     *   USER_REGISTERED   — el usuario se registró en la app
     */
    val eventType: String = "",

    /** Timestamp epoch (ms) de cuándo ocurrió el evento. */
    val occurredAt: Long = 0L,

    // ── Datos específicos por tipo de evento ─────────────────────────────────

    /** [LEVEL_UP] Nivel al que llegó. Ej: "AMATEUR" */
    val newLevel: String? = null,

    /** [LEVEL_UP] Nivel anterior. Ej: "CASUAL" */
    val previousLevel: String? = null,

    /** [LEVEL_UP / MEDAL_UNLOCKED / SPECIES_DISCOVERED] XP total del usuario en ese momento. */
    val xpAtEvent: Long? = null,

    /** [MEDAL_UNLOCKED] Identificador del logro. Ej: "FIRST_CAPTURE" */
    val medalId: String? = null,

    /** [SPECIES_DISCOVERED] Nombre científico de la especie. */
    val scientificName: String? = null,

    /** [SPECIES_DISCOVERED] Nombre común del insecto. */
    val insectName: String? = null,

    /** [SPECIES_DISCOVERED] Categoría taxonómica. Ej: "HYMENOPTERA" */
    val category: String? = null
)
