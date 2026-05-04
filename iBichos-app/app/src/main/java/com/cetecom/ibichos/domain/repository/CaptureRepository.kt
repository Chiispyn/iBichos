package com.cetecom.ibichos.domain.repository

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory

/**
 * Contrato para las operaciones sobre capturas.
 * La implementación vive en la capa data.
 */
interface CaptureRepository {
    /** Obtiene todas las capturas del usuario ordenadas por fecha desc */
    suspend fun getCaptures(userId: String): List<CaptureItem>

    /** Obtiene capturas globales de la comunidad ordenadas por fecha desc */
    suspend fun getGlobalCaptures(limit: Int = 200): List<CaptureItem>

    /** Guarda una nueva captura y devuelve su ID de documento */
    suspend fun saveCapture(
        userId: String,
        imageUrl: String,
        insectName: String,
        scientificName: String,
        category: InsectCategory,
        dangerLevel: DangerLevel,
        probability: Double,
        latitude: Double?,
        longitude: Double?,
        xpAwarded: Long,
        description: String,
        needsReview: Boolean,
        status: String
    ): String

    /** Verifica si el usuario ya ha capturado esta especie antes */
    suspend fun hasCaughtInsect(userId: String, scientificName: String): Boolean

    /** Elimina una captura (Borrado lógico o físico según implementación) */
    suspend fun deleteCapture(id: String)
}
