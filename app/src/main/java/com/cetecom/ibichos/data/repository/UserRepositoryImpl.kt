package com.cetecom.ibichos.data.repository

import android.net.Uri
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Implementación de [UserRepository] usando Firestore + Firebase Storage.
 */
class UserRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : UserRepository {

    override suspend fun getUserProfile(uid: String): UserProfile {
        val doc = db.collection("users").document(uid).get().await()

        // Obtener número de capturas del usuario
        val capturesResult = db.collection("captures")
            .whereEqualTo("userId", uid)
            .get()
            .await()

        return UserProfile(
            uid          = uid,
            displayName  = doc.getString("displayName") ?: "",
            email        = doc.getString("email") ?: "",
            xp           = doc.getLong("xp") ?: 0L,
            level        = doc.getString("level") ?: "Casual",
            avatarUrl    = doc.getString("avatarUrl"),
            totalCaptures = capturesResult.size()
        )
    }

    override suspend fun updateAvatar(uid: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await().toString()
        db.collection("users").document(uid)
            .update("avatarUrl", downloadUrl)
            .await()
        return downloadUrl
    }

    override suspend fun incrementXp(uid: String, xpGain: Long) {
        val userRef = db.collection("users").document(uid)
        val doc = userRef.get().await()
        val currentXp = (doc.getLong("xp") ?: 0L) + xpGain

        // Calcular nivel según XP acumulado
        val newLevel = when {
            currentXp >= 1000 -> "Maestro"
            currentXp >= 500  -> "Experto"
            currentXp >= 200  -> "Explorador"
            currentXp >= 50   -> "Aprendiz"
            else              -> "Casual"
        }

        userRef.set(
            mapOf("xp" to currentXp, "level" to newLevel),
            SetOptions.merge()
        ).await()
    }
}
