package com.cetecom.ibichos.presentation.catalog.viewdata

/**
 * Modelo de UI para una captura — listo para renderizar, sin lógica de formato en la pantalla.
 *
 * Las conversiones (enum → label, Long → fecha, Double → %) ocurren en el mapper,
 * no en el Composable.
 */
data class CaptureViewData(
    val id: String,
    val imageUrl: String,
    val insectName: String,
    val scientificName: String,
    val categoryLabel: String,          // "Arácnido", "Himenóptero"…
    val dangerLabel: String,            // "Venenoso 🔴", "Inofensivo 🟢"…
    val probabilityFormatted: String,   // "94%"
    val dateFormatted: String,          // "12 may 2026"
    val xpAwarded: Long,
    val description: String,
    val status: String,                 // "APPROVED", "PENDING_REVIEW", "DELETED"
    val needsReview: Boolean,
    val latitude: Double?,
    val longitude: Double?
)
