package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw IllegalStateException("UID nulo tras el registro")

        db.collection("users").document(uid).set(
            hashMapOf(
                "displayName"        to displayName,
                "email"              to email,
                "region"             to region,
                "city"               to city,
                "birthDate"          to birthDate,
                "gender"             to gender,
                "avatarUrl"          to null,
                "totalCaptures"      to 0,
                "createdAt"          to Timestamp.now(),
                "strikes"            to 0,
                "isShadowBanned"     to false,
                "xp"                 to 0L,
                "uniqueInsectsCount" to 0,
                "medalsCount"        to 0,
                "gamification" to hashMapOf(
                    "xp"                 to 0L,
                    "level"              to "CASUAL",
                    "uniqueInsectsCount" to 0,
                    "medals"             to emptyList<String>(),
                    "medalsEarnedAt"     to emptyMap<String, Long>(),
                    "categoryCounts"     to emptyMap<String, Int>(),
                    "levelUpAt"          to emptyMap<String, Long>()
                )
            )
        ).await()

        return uid
    }

    override suspend fun signInWithGoogle(idToken: String): Pair<String, Boolean> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Usuario nulo tras Google Sign-In")
        val isNewUser = result.additionalUserInfo?.isNewUser == true

        if (isNewUser) {
            db.collection("users").document(user.uid).set(
                hashMapOf(
                    "displayName"        to (user.displayName ?: "Cazador"),
                    "email"              to (user.email ?: ""),
                    "region"             to "",
                    "city"               to "",
                    "birthDate"          to "",
                    "gender"             to "UNSPECIFIED",
                    "avatarUrl"          to user.photoUrl?.toString(),
                    "totalCaptures"      to 0,
                    "createdAt"          to Timestamp.now(),
                    "strikes"            to 0,
                    "isShadowBanned"     to false,
                    "xp"                 to 0L,
                    "uniqueInsectsCount" to 0,
                    "medalsCount"        to 0,
                    "gamification"       to hashMapOf(
                        "xp"                 to 0L,
                        "level"              to "CASUAL",
                        "uniqueInsectsCount" to 0,
                        "medals"             to emptyList<String>(),
                        "medalsEarnedAt"     to emptyMap<String, Long>(),
                        "categoryCounts"     to emptyMap<String, Int>(),
                        "levelUpAt"          to emptyMap<String, Long>()
                    )
                )
            ).await()
        }

        return Pair(user.uid, isNewUser)
    }

    override suspend fun completeProfile(
        uid: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {
        db.collection("users").document(uid).update(
            mapOf(
                "region"    to region,
                "city"      to city,
                "birthDate" to birthDate,
                "gender"    to gender
            )
        ).await()
    }

    override suspend fun checkProfileCompletion(uid: String): Boolean {
        val doc = db.collection("users").document(uid).get().await()
        val region = doc.getString("region") ?: ""
        val city = doc.getString("city") ?: ""
        val birthDate = doc.getString("birthDate") ?: ""
        return region.isNotEmpty() && city.isNotEmpty() && birthDate.isNotEmpty()
    }

    override suspend fun getLocations(): Map<String, List<String>> {
        val doc = db.collection("metadata").document("locations").get().await()
        if (!doc.exists()) return emptyMap()
        @Suppress("UNCHECKED_CAST")
        return (doc.get("regions") as? Map<String, List<String>>) ?: emptyMap()
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
    }
}
