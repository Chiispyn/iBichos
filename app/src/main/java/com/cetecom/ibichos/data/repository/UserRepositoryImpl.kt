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

    override suspend fun getTopUsersByXp(limit: Int): List<UserProfile> {
        val snapshot = db.collection("users")
            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val uid = doc.id
            // En una app real, idealmente guardaríamos totalCaptures directamente en el doc de usuario
            // para evitar consultar la recolección de capturas por cada usuario.
            // Por simplicidad para el ranking, consultaremos las capturas si es necesario, 
            // aunque puede ser mas lento. Para evitar miles de reads, solo mostramos el XP y limitamos.
            
            UserProfile(
                uid          = uid,
                displayName  = doc.getString("displayName") ?: "Usuario",
                email        = doc.getString("email") ?: "",
                xp           = doc.getLong("xp") ?: 0L,
                level        = doc.getString("level") ?: "Casual",
                avatarUrl    = doc.getString("avatarUrl"),
                totalCaptures = 0 // Optimización: no consultaremos capturas individuales aquí por temas de reads, el ranking principal será por XP
            )
        }
    }
}
