package com.cetecom.ibichos.presentation.map.mapper

import com.cetecom.ibichos.domain.model.CaptureItem
import com.cetecom.ibichos.presentation.map.viewdata.MapCaptureViewData

fun CaptureItem.toMapViewData(): MapCaptureViewData? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    if (lat < -90.0 || lat > 90.0) return null
    if (lon < -180.0 || lon > 180.0) return null
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
