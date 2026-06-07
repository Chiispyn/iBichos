package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.repository.CaptureRepository

/**
 * Implementación falsa de CaptureRepository para tests de UI.
 * Devuelve una lista predefinida de capturas sin acceder a Firebase.
 */
class FakeCaptureRepository : CaptureRepository {

    val fakeCaptures = listOf(
        CaptureItem(
            id = "fake_1",
            userId = "test_uid",
            insectName = "Abeja",
            scientificName = "Apis mellifera",
            imageUrl = "",
            category = InsectCategory.HYMENOPTERA,
            dangerLevel = DangerLevel.CAUTION,
            probability = 0.94,
            capturedAt = System.currentTimeMillis(),
            xpAwarded = 50L,
            description = "Abeja de prueba",
            status = "APPROVED",
            needsReview = false
        ),
        CaptureItem(
            id = "fake_2",
            userId = "test_uid",
            insectName = "Araña",
            scientificName = "Latrodectus mactans",
            imageUrl = "",
            category = InsectCategory.ARACHNID,
            dangerLevel = DangerLevel.VENOMOUS,
            probability = 0.88,
            capturedAt = System.currentTimeMillis() - 86400000L,
            xpAwarded = 100L,
            description = "Araña de prueba",
            status = "REJECTED",
            needsReview = false
        )
    )

    override suspend fun getCaptures(userId: String): List<CaptureItem> = fakeCaptures

    override suspend fun getGlobalCaptures(limit: Int): List<CaptureItem> = fakeCaptures

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
    ): String = "fake_new_id"

    override suspend fun hasCaughtInsect(userId: String, scientificName: String): Boolean = false

    override suspend fun deleteCapture(id: String) {}

    override suspend fun appealCapture(id: String) {}
}
