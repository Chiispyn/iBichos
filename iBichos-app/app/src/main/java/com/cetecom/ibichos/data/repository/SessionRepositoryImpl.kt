package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.repository.SessionRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Implementación de [SessionRepository] usando Firebase Firestore.
 *
 * Estructura del documento en Firestore:
 * sessions/{sessionId}: {
 *   userId:          "uid-del-usuario",
 *   startedAt:       Timestamp,
 *   endedAt:         Timestamp | null,
 *   durationMinutes: Int,
 *   deviceOS:        "Android"
 * }
 */

class SessionRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SessionRepository {

    override suspend fun startSession(userId: String): String {
        val sessionId = UUID.randomUUID().toString()

        db.collection("sessions").document(sessionId).set(
            hashMapOf(
                "userId" to userId,
                "startedAt" to Timestamp.now(),
                "endedAt" to null,
                "durationMinutes" to 0,
                "deviceOS" to "Android"
            )
        ).await()

        return sessionId
    }

    override suspend fun endSession(sessionId: String) {
        // Leer startedAt para calcular la duración correctamente
        val doc = db.collection("sessions").document(sessionId).get().await()
        val startedAt = doc.getTimestamp("startedAt")?.toDate()?.time ?: return

        val endedAt = System.currentTimeMillis()
        val durationMinutes = ((endedAt - startedAt) / 60_000).toInt().coerceAtLeast(0)

        db.collection("sessions").document(sessionId).update(
            mapOf(
                "endedAt" to Timestamp.now(),
                "durationMinutes" to durationMinutes
            )
        ).await()
    }
}
