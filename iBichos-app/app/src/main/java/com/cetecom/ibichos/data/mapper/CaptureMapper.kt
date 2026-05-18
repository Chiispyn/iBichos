package com.cetecom.ibichos.data.mapper

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toCaptureItem(): CaptureItem = CaptureItem(
    id             = id,
    userId         = getString("userId") ?: "",
    imageUrl       = getString("imageUrl") ?: "",
    insectName     = getString("insectName") ?: "",
    scientificName = getString("scientificName") ?: "",
    category       = runCatching {
        InsectCategory.valueOf(getString("category") ?: InsectCategory.UNKNOWN.name)
    }.getOrDefault(InsectCategory.UNKNOWN),
    probability    = getDouble("probability") ?: 0.0,
    dangerLevel    = runCatching {
        DangerLevel.valueOf(getString("dangerLevel") ?: DangerLevel.UNKNOWN.name)
    }.getOrDefault(DangerLevel.UNKNOWN),
    latitude       = getDouble("latitude"),
    longitude      = getDouble("longitude"),
    capturedAt     = getTimestamp("timestamp")?.toDate()?.time ?: 0L,
    xpAwarded      = getLong("xpAwarded") ?: 50L,
    description    = getString("description") ?: "Sin descripción adicional.",
    needsReview    = getBoolean("needsReview") ?: false,
    status         = getString("status") ?: "APPROVED"
)
