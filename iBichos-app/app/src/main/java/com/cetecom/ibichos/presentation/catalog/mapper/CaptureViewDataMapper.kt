package com.cetecom.ibichos.presentation.catalog.mapper

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.presentation.catalog.viewdata.CaptureViewData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun CaptureItem.toViewData(): CaptureViewData = CaptureViewData(
    id                   = id,
    userId               = userId,
    imageUrl             = imageUrl,
    insectName           = insectName,
    scientificName       = scientificName,
    categoryLabel        = category.displayName(),
    dangerLabel          = dangerLevel.displayName(),
    probabilityFormatted = "${(probability * 100).toInt()}%",
    dateFormatted        = capturedAt.toFormattedDate(),
    xpAwarded            = xpAwarded,
    description          = description,
    status               = status,
    needsReview          = needsReview,
    latitude             = latitude,
    longitude            = longitude
)

fun List<CaptureItem>.toViewDataList(): List<CaptureViewData> = map { it.toViewData() }

private fun Long.toFormattedDate(): String =
    SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(this))
