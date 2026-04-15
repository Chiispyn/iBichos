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
            totalCaptures = capturesResult.size(),
            medals        = (doc.get("medals") as? List<String>) ?: emptyList(),
            uniqueInsectsCount = doc.getLong("uniqueInsectsCount")?.toInt() ?: 0,
            categoryCounts = (doc.get("categoryCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
        )
    }

    override suspend fun updateAvatar(uid: String, uri: Uri): String {
        // Evitamos usar Firebase Storage para no incurrir en gastos de facturación GCP.
        // Guardamos la URI de contenido local (content://) o equivalente en Firestore como un String directo.
        val localUrl = uri.toString()
        db.collection("users").document(uid)
            .update("avatarUrl", localUrl)
            .await()
        return localUrl
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

        return snapshot.documents.map { mapDocumentToUserProfileFast(it) }
    }

    override suspend fun getTopUsersByUniqueInsects(limit: Int): List<UserProfile> {
        val snapshot = db.collection("users")
            .orderBy("uniqueInsectsCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents.map { mapDocumentToUserProfileFast(it) }
    }

    override suspend fun getTopUsersByMedals(limit: Int): List<UserProfile> {
        // Firestore no permite ordenar nativamente por longitud de arreglos (medals.size).
        // Para la demo universitaria, traer 100 de los más activos y ordenarlos en RAM es eficiente y limpio.
        val snapshot = db.collection("users")
            .orderBy("xp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(100L)
            .get()
            .await()

        return snapshot.documents
            .map { mapDocumentToUserProfileFast(it) }
            .sortedByDescending { it.medals.size }
            .take(limit)
    }

    // Función auxiliar para leer rápidamente del DB sin consultar la tabla `captures`
    private fun mapDocumentToUserProfileFast(doc: com.google.firebase.firestore.DocumentSnapshot): UserProfile {
        return UserProfile(
            uid          = doc.id,
            displayName  = doc.getString("displayName") ?: "Usuario",
            email        = doc.getString("email") ?: "",
            xp           = doc.getLong("xp") ?: 0L,
            level        = doc.getString("level") ?: "Casual",
            avatarUrl    = doc.getString("avatarUrl"),
            totalCaptures = 0,
            medals        = (doc.get("medals") as? List<String>) ?: emptyList(),
            uniqueInsectsCount = doc.getLong("uniqueInsectsCount")?.toInt() ?: 0,
            categoryCounts = (doc.get("categoryCounts") as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
        )
    }

    override suspend fun unlockMedalsAndIncrementUnique(uid: String, medalsToUnlock: List<String>, isNewInsect: Boolean, incrementCategory: String?) {
        if (medalsToUnlock.isEmpty() && !isNewInsect && incrementCategory == null) return

        val userRef = db.collection("users").document(uid)
        val updates = mutableMapOf<String, Any>()

        if (medalsToUnlock.isNotEmpty()) {
            updates["medals"] = com.google.firebase.firestore.FieldValue.arrayUnion(*medalsToUnlock.toTypedArray())
        }
        if (isNewInsect) {
            updates["uniqueInsectsCount"] = com.google.firebase.firestore.FieldValue.increment(1)
        }
        if (incrementCategory != null) {
            updates["categoryCounts.$incrementCategory"] = com.google.firebase.firestore.FieldValue.increment(1)
        }
        
        userRef.update(updates).await()
    }
}
