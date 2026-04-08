package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementación de [CaptureRepository] usando Firebase Firestore.
 * La extensión .await() convierte las Tasks de Firebase en coroutines.
 */
class CaptureRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CaptureRepository {

    override suspend fun getCaptures(userId: String): List<CaptureItem> {
        val result = db.collection("captures")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return result.documents
            .mapNotNull { doc ->
                CaptureItem(
                    id            = doc.id,
                    imageUrl      = doc.getString("imageUrl") ?: "",
                    insectName    = doc.getString("insectName") ?: "",
                    scientificName = doc.getString("scientificName") ?: "",
                    probability   = doc.getDouble("probability") ?: 0.0,
                    dangerLevel   = doc.getString("dangerLevel") ?: "Inofensivo",
                    latitude      = doc.getDouble("latitude"),
                    longitude     = doc.getDouble("longitude"),
                    capturedAt    = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                    xpAwarded     = doc.getLong("xpAwarded") ?: 50L
                )
            }
            .sortedByDescending { it.capturedAt }
    }

    override suspend fun getGlobalCaptures(limit: Int): List<CaptureItem> {
        val result = db.collection("captures")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return result.documents
            .mapNotNull { doc ->
                CaptureItem(
                    id            = doc.id,
                    imageUrl      = doc.getString("imageUrl") ?: "",
                    insectName    = doc.getString("insectName") ?: "",
                    scientificName = doc.getString("scientificName") ?: "",
                    probability   = doc.getDouble("probability") ?: 0.0,
                    dangerLevel   = doc.getString("dangerLevel") ?: "Inofensivo",
                    latitude      = doc.getDouble("latitude"),
                    longitude     = doc.getDouble("longitude"),
                    capturedAt    = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                    xpAwarded     = doc.getLong("xpAwarded") ?: 50L
                )
            }
    }

    override suspend fun saveCapture(
        userId: String,
        imageUrl: String,
        insectName: String,
        scientificName: String,
        dangerLevel: String,
        probability: Double,
        latitude: Double?,
        longitude: Double?,
        xpAwarded: Long
    ): String {
        val captureData = hashMapOf(
            "userId"        to userId,
            "imageUrl"      to imageUrl,
            "insectName"    to insectName,
            "scientificName" to scientificName,
            "dangerLevel"   to dangerLevel,
            "probability"   to probability,
            "latitude"      to latitude,
            "longitude"     to longitude,
            "timestamp"     to Timestamp.now(),
            "xpAwarded"     to xpAwarded
        )

        val docRef = db.collection("captures").add(captureData).await()
        return docRef.id
    }
}
