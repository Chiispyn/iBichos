package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.mapper.toUserProfile
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.UserLevel
import com.cetecom.ibichos.domain.repository.EventRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val eventRepo: EventRepository
) : UserRepository {

    override suspend fun getUserProfile(uid: String): UserProfile {
        val doc = db.collection("users").document(uid).get().await()
        val captureCount = db.collection("captures")
            .whereEqualTo("userId", uid)
            .get()
            .await()
            .size()
        return doc.toUserProfile(captureCount)
    }

    override suspend fun updateAvatar(uid: String, avatarUrl: String): String {
        db.collection("users").document(uid)
            .update("avatarUrl", avatarUrl)
            .await()
        return avatarUrl
    }

    override suspend fun incrementXp(uid: String, xpGain: Long) {
        val userRef = db.collection("users").document(uid)
        val doc = userRef.get().await()

        val isShadowBanned = doc.getBoolean("isShadowBanned") ?: false
        if (isShadowBanned) return

        @Suppress("UNCHECKED_CAST")
        val gamMap = doc.get("gamification") as? Map<String, Any> ?: emptyMap()
        val currentXp = ((gamMap["xp"] as? Long) ?: 0L) + xpGain
        val currentLevelStr = (gamMap["level"] as? String) ?: UserLevel.CASUAL.name
        val newLevelStr = UserLevel.fromXp(currentXp).name

        if (doc.exists()) {
            val updates = mutableMapOf<String, Any>(
                "gamification.xp"    to currentXp,
                "gamification.level" to newLevelStr,
                "xp"                 to currentXp
            )
            if (newLevelStr != currentLevelStr) {
                updates["gamification.levelUpAt.$newLevelStr"] = System.currentTimeMillis()
                runCatching {
                    eventRepo.logLevelUp(uid, currentLevelStr, newLevelStr, currentXp)
                }
            }
            userRef.update(updates).await()
        } else {
            val levelUpAtMap = if (newLevelStr != UserLevel.CASUAL.name) {
                mapOf(newLevelStr to System.currentTimeMillis())
            } else emptyMap()

            userRef.set(
                mapOf(
                    "xp"                 to currentXp,
                    "uniqueInsectsCount" to 0,
                    "medalsCount"        to 0,
                    "strikes"            to 0,
                    "isShadowBanned"     to false,
                    "gamification"       to mapOf(
                        "xp"                 to currentXp,
                        "level"              to newLevelStr,
                        "uniqueInsectsCount" to 0,
                        "categoryCounts"     to emptyMap<String, Int>(),
                        "medals"             to emptyList<String>(),
                        "medalsEarnedAt"     to emptyMap<String, Long>(),
                        "levelUpAt"          to levelUpAtMap
                    )
                )
            ).await()
        }
    }

    override suspend fun getTopUsersByXp(limit: Int): List<UserProfile> {
        return db.collection("users")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { it.toUserProfile() }
    }

    override suspend fun getTopUsersByUniqueInsects(limit: Int): List<UserProfile> {
        return db.collection("users")
            .orderBy("uniqueInsectsCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { it.toUserProfile() }
    }

    override suspend fun getTopUsersByMedals(limit: Int): List<UserProfile> {
        return db.collection("users")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(100L)
            .get()
            .await()
            .documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { it.toUserProfile() }
            .sortedByDescending { it.gamification.medals.size }
            .take(limit)
    }

    override suspend fun unlockMedalsAndIncrementUnique(
        uid: String,
        medalsToUnlock: List<String>,
        isNewInsect: Boolean,
        incrementCategory: String?
    ) {
        if (medalsToUnlock.isEmpty() && !isNewInsect && incrementCategory == null) return

        val userRef = db.collection("users").document(uid)
        val doc = userRef.get().await()

        val isShadowBanned = doc.getBoolean("isShadowBanned") ?: false
        if (isShadowBanned) return

        val updates = mutableMapOf<String, Any>()

        if (medalsToUnlock.isNotEmpty()) {
            updates["gamification.medals"] = FieldValue.arrayUnion(*medalsToUnlock.toTypedArray())
            val now = System.currentTimeMillis()
            medalsToUnlock.forEach { medal ->
                updates["gamification.medalsEarnedAt.$medal"] = now
            }
            updates["medalsCount"] = FieldValue.increment(medalsToUnlock.size.toLong())
        }
        if (isNewInsect) {
            updates["gamification.uniqueInsectsCount"] = FieldValue.increment(1)
            updates["uniqueInsectsCount"] = FieldValue.increment(1)
        }
        if (incrementCategory != null) {
            updates["gamification.categoryCounts.$incrementCategory"] = FieldValue.increment(1)
        }

        userRef.update(updates).await()

        val currentXp = runCatching {
            @Suppress("UNCHECKED_CAST")
            val gamMap = userRef.get().await().get("gamification") as? Map<String, Any> ?: emptyMap()
            (gamMap["xp"] as? Long) ?: 0L
        }.getOrDefault(0L)

        medalsToUnlock.forEach { medal ->
            runCatching { eventRepo.logMedalUnlocked(uid, medal, currentXp) }
        }
    }
}
