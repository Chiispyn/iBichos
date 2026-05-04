package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                    id             = doc.id,
                    userId         = doc.getString("userId") ?: "",
                    imageUrl       = doc.getString("imageUrl") ?: "",
                    insectName     = doc.getString("insectName") ?: "",
                    scientificName = doc.getString("scientificName") ?: "",
                    category       = runCatching {
                        InsectCategory.valueOf(doc.getString("category") ?: InsectCategory.UNKNOWN.name)
                    }.getOrDefault(InsectCategory.UNKNOWN),
                    probability    = doc.getDouble("probability") ?: 0.0,
                    dangerLevel    = runCatching {
                        DangerLevel.valueOf(doc.getString("dangerLevel") ?: DangerLevel.UNKNOWN.name)
                    }.getOrDefault(DangerLevel.UNKNOWN),
                    latitude       = doc.getDouble("latitude"),
                    longitude      = doc.getDouble("longitude"),
                    capturedAt     = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                    xpAwarded      = doc.getLong("xpAwarded") ?: 50L,
                    description    = doc.getString("description") ?: "Sin descripción adicional.",
                    needsReview    = doc.getBoolean("needsReview") ?: false,
                    status         = doc.getString("status") ?: "APPROVED"
                )
            }
            .filter { it.status != "DELETED" && it.status != "REJECTED" }
            .sortedByDescending { it.capturedAt }
    }

    override suspend fun getGlobalCaptures(limit: Int): List<CaptureItem> {
        val result = db.collection("captures")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return result.documents
            .mapNotNull { doc ->
                CaptureItem(
                    id             = doc.id,
                    userId         = doc.getString("userId") ?: "",
                    imageUrl       = doc.getString("imageUrl") ?: "",
                    insectName     = doc.getString("insectName") ?: "",
                    scientificName = doc.getString("scientificName") ?: "",
                    category       = runCatching {
                        InsectCategory.valueOf(doc.getString("category") ?: InsectCategory.UNKNOWN.name)
                    }.getOrDefault(InsectCategory.UNKNOWN),
                    probability    = doc.getDouble("probability") ?: 0.0,
                    dangerLevel    = runCatching {
                        DangerLevel.valueOf(doc.getString("dangerLevel") ?: DangerLevel.UNKNOWN.name)
                    }.getOrDefault(DangerLevel.UNKNOWN),
                    latitude       = doc.getDouble("latitude"),
                    longitude      = doc.getDouble("longitude"),
                    capturedAt     = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                    xpAwarded      = doc.getLong("xpAwarded") ?: 50L,
                    description    = doc.getString("description") ?: "Sin descripción adicional.",
                    needsReview    = doc.getBoolean("needsReview") ?: false,
                    status         = doc.getString("status") ?: "APPROVED"
                )
            }
            .filter { it.status != "DELETED" && it.status != "REJECTED" }
    }

    override suspend fun saveCapture(
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
    ): String {
        val captureData = hashMapOf(
            "userId"         to userId,
            "imageUrl"       to imageUrl,
            "insectName"     to insectName,
            "scientificName" to scientificName,
            "category"       to category.name,      // Se guarda el nombre del enum en Firestore
            "dangerLevel"    to dangerLevel.name,   // Se guarda el nombre del enum en Firestore
            "probability"    to probability,
            "latitude"       to latitude,
            "longitude"      to longitude,
            "timestamp"      to Timestamp.now(),
            "xpAwarded"      to xpAwarded,
            "description"    to description,
            "needsReview"    to needsReview,
            "status"         to status
        )

        val docRef = db.collection("captures").add(captureData).await()
        return docRef.id
    }

    override suspend fun hasCaughtInsect(userId: String, scientificName: String): Boolean {
        val result = db.collection("captures")
            .whereEqualTo("userId", userId)
            .whereEqualTo("scientificName", scientificName)
            .limit(1)
            .get()
            .await()
        return !result.isEmpty
    }

    override suspend fun deleteCapture(id: String) {
        db.collection("captures")
            .document(id)
            .update("status", "DELETED")
            .await()
    }
}
