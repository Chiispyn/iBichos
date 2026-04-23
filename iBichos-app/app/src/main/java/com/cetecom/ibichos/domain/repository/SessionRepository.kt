package com.cetecom.ibichos.domain.repository

/**
 * Contrato para el registro de sesiones de uso de la app.
 * Los datos se guardan en Firestore: sessions/{sessionId}
 */
interface SessionRepository {

    /**
     * Inicia una nueva sesión para el usuario.
     * Crea el documento en Firestore con startedAt = ahora y endedAt = null.
     * @return El sessionId del documento creado (para usarlo al cerrar sesión).
     */
    suspend fun startSession(userId: String): String

    /**
     * Cierra la sesión activa, calculando la duración en minutos.
     * Actualiza el documento con endedAt y durationMinutes.
     */
    suspend fun endSession(sessionId: String)
}
