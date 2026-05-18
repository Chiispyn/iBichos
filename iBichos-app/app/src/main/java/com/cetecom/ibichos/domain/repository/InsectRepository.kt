package com.cetecom.ibichos.domain.repository

import com.cetecom.ibichos.domain.model.InsectIdentification

interface InsectRepository {
    suspend fun identify(imageBytes: ByteArray, lat: Double?, lon: Double?): InsectIdentification
}
