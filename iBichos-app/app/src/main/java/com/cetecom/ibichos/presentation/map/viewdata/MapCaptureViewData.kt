package com.cetecom.ibichos.presentation.map.viewdata

/**
 * Modelo de UI para un pin en el mapa.
 *
 * Solo contiene los campos necesarios para renderizar el marcador y su popup.
 * Se excluyen campos como description, xpAwarded, status, etc. que el mapa no usa.
 */
data class MapCaptureViewData(
    val id: String,
    val imageUrl: String,
    val insectName: String,
    val categoryLabel: String,  // "Arácnido", "Himenóptero"…
    val latitude: Double,
    val longitude: Double
)
