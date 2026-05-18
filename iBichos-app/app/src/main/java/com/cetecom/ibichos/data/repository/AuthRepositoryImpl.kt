package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.data.remote.dto.UserDocumentDto
import com.cetecom.ibichos.domain.repository.AuthRepository
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

        val dto = UserDocumentDto(
            displayName = displayName,
            email       = email,
            region      = region,
            city        = city,
            birthDate   = birthDate,
            gender      = gender
        )
        db.collection("users").document(uid).set(dto.toNewUserMap()).await()

        return uid
    }

    override suspend fun signInWithGoogle(idToken: String): Pair<String, Boolean> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw IllegalStateException("Usuario nulo tras Google Sign-In")
        val isNewUser = result.additionalUserInfo?.isNewUser == true

        if (isNewUser) {
            val dto = UserDocumentDto(
                displayName = user.displayName ?: "Cazador",
                email       = user.email ?: "",
                region      = "",
                city        = "",
                birthDate   = "",
                gender      = "UNSPECIFIED",
                avatarUrl   = user.photoUrl?.toString()
            )
            db.collection("users").document(user.uid).set(dto.toNewUserMap()).await()
        }

        return Pair(user.uid, isNewUser)
    }

    override suspend fun completeProfile(uid: String, region: String, city: String, birthDate: String, gender: String) {
        db.collection("users").document(uid).update(
            mapOf("region" to region, "city" to city, "birthDate" to birthDate, "gender" to gender)
        ).await()
    }

    override suspend fun checkProfileCompletion(uid: String): Boolean {
        val doc = db.collection("users").document(uid).get().await()
        return listOf("region", "city", "birthDate").all { doc.getString(it)?.isNotEmpty() == true }
    }

    override suspend fun getLocations(): Map<String, List<String>> {
        val doc = db.collection("metadata").document("locations").get().await()
        if (!doc.exists()) return emptyMap()
        @Suppress("UNCHECKED_CAST")
        return (doc.get("regions") as? Map<String, List<String>>) ?: emptyMap()
    }

    override fun signOut() = auth.signOut()
    override fun sendPasswordResetEmail(email: String) { auth.sendPasswordResetEmail(email) }
}
