package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.mapper.toCaptureItem
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CaptureRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CaptureRepository {

    override suspend fun getCaptures(userId: String): List<CaptureItem> {
        return db.collection("captures")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .map { it.toCaptureItem() }
            .filter { it.status != "DELETED" }
            .sortedByDescending { it.capturedAt }
    }

    override suspend fun getGlobalCaptures(limit: Int): List<CaptureItem> {
        return db.collection("captures")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .map { it.toCaptureItem() }
            .filter { it.status != "DELETED" }
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
            "category"       to category.name,
            "dangerLevel"    to dangerLevel.name,
            "probability"    to probability,
            "latitude"       to latitude,
            "longitude"      to longitude,
            "timestamp"      to Timestamp.now(),
            "xpAwarded"      to xpAwarded,
            "description"    to description,
            "needsReview"    to needsReview,
            "status"         to status
        )
        return db.collection("captures").add(captureData).await().id
    }

    override suspend fun hasCaughtInsect(userId: String, scientificName: String): Boolean {
        return db.collection("captures")
            .whereEqualTo("userId", userId)
            .whereEqualTo("scientificName", scientificName)
            .limit(1)
            .get()
            .await()
            .isEmpty
            .not()
    }

    override suspend fun deleteCapture(id: String) {
        db.collection("captures").document(id).update("status", "DELETED").await()
    }

    override suspend fun appealCapture(id: String) {
        db.collection("captures").document(id).update(
            mapOf("status" to "PENDING_REVIEW", "needsReview" to true)
        ).await()
    }
}
