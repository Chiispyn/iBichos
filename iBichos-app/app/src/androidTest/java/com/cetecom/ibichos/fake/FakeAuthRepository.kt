package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.repository.AuthRepository

/**
 * Implementación falsa de AuthRepository para tests de UI.
 * Simula un usuario autenticado con perfil completo.
 */
class FakeAuthRepository : AuthRepository {
    override fun isLoggedIn(): Boolean = true
    override fun getCurrentUserId(): String = "test_uid"

    override suspend fun login(email: String, password: String) {}

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ): String = "test_uid"

    override suspend fun signInWithGoogle(idToken: String): Pair<String, Boolean> =
        Pair("test_uid", false)

    override suspend fun completeProfile(
        uid: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {}

    override suspend fun checkProfileCompletion(uid: String): Boolean = true

    override suspend fun getLocations(): Map<String, List<String>> = emptyMap()

    override fun signOut() {}

    override fun sendPasswordResetEmail(email: String) {}
}
