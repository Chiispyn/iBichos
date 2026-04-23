package com.cetecom.ibichos.domain.model

/**
 * Modelo de dominio para registrar sesiones de uso de la app.
 *
 * Permite generar métricas de engagement en el Dashboard sin depender de Firebase Analytics.
 * Se guarda en la colección Firestore: sessions/{sessionId}
 *
 * Ciclo de vida:
 *   1. Al abrir la app → se crea el documento con startedAt y endedAt = null.
 *   2. Al cerrar/minimizar la app → se actualiza endedAt y se calcula durationMinutes.
 */
data class AppSession(
    val sessionId: String = "",

    /** UID del usuario. Relaciona esta sesión con Users/{uid}. */
    val userId: String = "",

    /** Timestamp epoch (ms) de cuando el usuario abrió la app. */
    val startedAt: Long = 0L,

    /** Timestamp epoch (ms) de cuando el usuario cerró/minimizó la app. Null si aún está activa. */
    val endedAt: Long? = null,

    /** Duración calculada en minutos al cerrar la sesión: (endedAt - startedAt) / 60000. */
    val durationMinutes: Int = 0,

    /** Sistema operativo del dispositivo. Por defecto "Android". */
    val deviceOS: String = "Android"
)
