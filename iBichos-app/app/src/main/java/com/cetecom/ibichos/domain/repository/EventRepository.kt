package com.cetecom.ibichos.domain.repository

interface EventRepository {
    suspend fun logLevelUp(userId: String, previousLevel: String, newLevel: String, xpAtEvent: Long)
    suspend fun logMedalUnlocked(userId: String, medalId: String, xpAtEvent: Long)
    suspend fun logSpeciesDiscovered(userId: String, scientificName: String, insectName: String, category: String, xpAtEvent: Long)
    suspend fun logUserRegistered(userId: String)
}
