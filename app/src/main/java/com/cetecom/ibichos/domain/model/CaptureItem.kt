package com.cetecom.ibichos.domain.model

/**
 * Modelo de dominio para una captura de insecto.
 * Esta capa NO depende de Firebase ni de ningún framework externo.
 */
data class CaptureItem(
    val id: String = "",
    val imageUrl: String = "",
    val insectName: String = "",
    val scientificName: String = "",
    val probability: Double = 0.0,
    val dangerLevel: String = "Inofensivo",
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** Milisegundos epoch — convertido desde Firebase Timestamp en el repositorio */
    val capturedAt: Long = 0L,
    val xpAwarded: Long = 50L,
    val description: String = ""
)
