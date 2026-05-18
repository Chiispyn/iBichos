package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.mapper.toCaptureItem
import com.cetecom.ibichos.data.remote.dto.CaptureDocumentDto
import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CaptureRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CaptureRepository {

    override suspend fun getCaptures(userId: String): List<CaptureItem> =
        db.collection("captures")
            .whereEqualTo("userId", userId)
            .get().await().documents
            .map { it.toCaptureItem() }
            .filter { it.status != "DELETED" }
            .sortedByDescending { it.capturedAt }

    override suspend fun getGlobalCaptures(limit: Int): List<CaptureItem> =
        db.collection("captures")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get().await().documents
            .map { it.toCaptureItem() }
            .filter { it.status != "DELETED" }

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
        val dto = CaptureDocumentDto(
            userId         = userId,
            imageUrl       = imageUrl,
            insectName     = insectName,
            scientificName = scientificName,
            category       = category.name,
            dangerLevel    = dangerLevel.name,
            probability    = probability,
            latitude       = latitude,
            longitude      = longitude,
            xpAwarded      = xpAwarded,
            description    = description,
            needsReview    = needsReview,
            status         = status
        )
        return db.collection("captures").add(dto.toMap()).await().id
    }

    override suspend fun hasCaughtInsect(userId: String, scientificName: String): Boolean =
        db.collection("captures")
            .whereEqualTo("userId", userId)
            .whereEqualTo("scientificName", scientificName)
            .limit(1).get().await().isEmpty.not()

    override suspend fun deleteCapture(id: String) {
        db.collection("captures").document(id).update("status", "DELETED").await()
    }

    override suspend fun appealCapture(id: String) {
        db.collection("captures").document(id)
            .update(mapOf("status" to "PENDING_REVIEW", "needsReview" to true)).await()
    }
}
