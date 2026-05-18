package com.cetecom.ibichos.data.remote.dto

import com.google.firebase.Timestamp

/**
 * DTO de escritura para el documento de captura en Firestore.
 *
 * Centraliza los nombres de campo en un único lugar,
 * eliminando los hashMapOf dispersos en el repositorio.
 */
data class CaptureDocumentDto(
    val userId: String,
    val imageUrl: String,
    val insectName: String,
    val scientificName: String,
    val category: String,       // nombre del enum: "HYMENOPTERA"
    val dangerLevel: String,    // nombre del enum: "CAUTION"
    val probability: Double,
    val latitude: Double?,
    val longitude: Double?,
    val xpAwarded: Long,
    val description: String,
    val needsReview: Boolean,
    val status: String
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId"         to userId,
        "imageUrl"       to imageUrl,
        "insectName"     to insectName,
        "scientificName" to scientificName,
        "category"       to category,
        "dangerLevel"    to dangerLevel,
        "probability"    to probability,
        "latitude"       to latitude,
        "longitude"      to longitude,
        "timestamp"      to Timestamp.now(),
        "xpAwarded"      to xpAwarded,
        "description"    to description,
        "needsReview"    to needsReview,
        "status"         to status
    )
}
