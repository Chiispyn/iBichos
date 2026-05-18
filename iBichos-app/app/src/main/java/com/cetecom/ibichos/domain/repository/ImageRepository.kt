package com.cetecom.ibichos.domain.repository

interface ImageRepository {
    suspend fun upload(imageBytes: ByteArray): String
}
