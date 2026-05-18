package com.cetecom.ibichos.presentation.map.mapper

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.presentation.map.viewdata.MapCaptureViewData

fun CaptureItem.toMapViewData(): MapCaptureViewData? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return MapCaptureViewData(
        id            = id,
        imageUrl      = imageUrl,
        insectName    = insectName,
        categoryLabel = category.displayName(),
        latitude      = lat,
        longitude     = lon
    )
}

fun List<CaptureItem>.toMapViewDataList(): List<MapCaptureViewData> =
    mapNotNull { it.toMapViewData() }
