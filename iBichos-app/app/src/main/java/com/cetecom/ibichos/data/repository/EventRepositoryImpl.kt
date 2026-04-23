package com.cetecom.ibichos.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Escribe eventos de gamificación en la colección Firestore `events`.
 *
 * Es un log append-only: nunca se edita ni borra un documento.
 * El Dashboard puede consultarla con filtros de fecha y tipo para obtener
 * métricas históricas como nivel promedio, tasa de activación de logros, etc.
 */
class EventRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Registra que un usuario subió de nivel.
     * @param previousLevel Nivel anterior (nombre del enum, ej: "CASUAL")
     * @param newLevel      Nuevo nivel (nombre del enum, ej: "AMATEUR")
     * @param xpAtEvent     XP total del usuario en el momento del cambio
     */
    suspend fun logLevelUp(
        userId: String,
        previousLevel: String,
        newLevel: String,
        xpAtEvent: Long
    ) {
        db.collection("events").add(
            hashMapOf(
                "userId"        to userId,
                "eventType"     to "LEVEL_UP",
                "occurredAt"    to Timestamp.now(),
                "previousLevel" to previousLevel,
                "newLevel"      to newLevel,
                "xpAtEvent"     to xpAtEvent
            )
        ).await()
    }

    /**
     * Registra que un usuario desbloqueó un logro/medalla.
     * @param medalId   ID del logro (ej: "FIRST_CAPTURE", "ARACHNOLOGIST")
     * @param xpAtEvent XP total del usuario en ese momento
     */
    suspend fun logMedalUnlocked(
        userId: String,
        medalId: String,
        xpAtEvent: Long
    ) {
        db.collection("events").add(
            hashMapOf(
                "userId"     to userId,
                "eventType"  to "MEDAL_UNLOCKED",
                "occurredAt" to Timestamp.now(),
                "medalId"    to medalId,
                "xpAtEvent"  to xpAtEvent
            )
        ).await()
    }

    /**
     * Registra que un usuario descubrió una especie nueva (primera captura de esa especie).
     * @param scientificName Nombre científico del insecto
     * @param insectName     Nombre común/display
     * @param category       Categoría taxonómica (nombre del enum, ej: "HYMENOPTERA")
     * @param xpAtEvent      XP total del usuario en ese momento
     */
    suspend fun logSpeciesDiscovered(
        userId: String,
        scientificName: String,
        insectName: String,
        category: String,
        xpAtEvent: Long
    ) {
        db.collection("events").add(
            hashMapOf(
                "userId"         to userId,
                "eventType"      to "SPECIES_DISCOVERED",
                "occurredAt"     to Timestamp.now(),
                "scientificName" to scientificName,
                "insectName"     to insectName,
                "category"       to category,
                "xpAtEvent"      to xpAtEvent
            )
        ).await()
    }

    /**
     * Registra que un nuevo usuario se registró en la app.
     * Se llama desde AuthViewModel al completar el registro.
     */
    suspend fun logUserRegistered(userId: String) {
        db.collection("events").add(
            hashMapOf(
                "userId"     to userId,
                "eventType"  to "USER_REGISTERED",
                "occurredAt" to Timestamp.now()
            )
        ).await()
    }
}
