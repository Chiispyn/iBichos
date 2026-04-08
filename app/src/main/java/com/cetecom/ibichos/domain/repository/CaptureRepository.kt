package com.cetecom.ibichos.domain.repository

import com.cetecom.ibichos.domain.model.CaptureItem

/**
 * Contrato para las operaciones sobre capturas.
 * La implementación vive en la capa data.
 */
interface CaptureRepository {
    /** Obtiene todas las capturas del usuario ordenadas por fecha desc */
    suspend fun getCaptures(userId: String): List<CaptureItem>

    /** Guarda una nueva captura y devuelve su ID de documento */
    suspend fun saveCapture(
        userId: String,
        imageUrl: String,
        insectName: String,
        scientificName: String,
        dangerLevel: String,
        probability: Double,
        latitude: Double?,
        longitude: Double?,
        xpAwarded: Long
    ): String
}
