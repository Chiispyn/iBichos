package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.repository.AuthRepository

/**
 * Implementación falsa de AuthRepository para tests de UI.
 * Simula un usuario autenticado con perfil completo.
 *
 * Para simular un fallo de registro (ej. correo ya existente), establece:
 *   FakeAuthRepository.shouldFailRegister = true
 *   FakeAuthRepository.registerErrorMessage = "El correo electrónico ya está registrado"
 * Resetear a false en @After para no contaminar otros tests.
 */
class FakeAuthRepository : AuthRepository {

    companion object {
        var shouldFailRegister = false
        var registerErrorMessage = "El correo electrónico ya está registrado"
    }

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
    ): String {
        if (shouldFailRegister) throw Exception(registerErrorMessage)
        return "test_uid"
    }

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

    override suspend fun getLocations(): Map<String, List<String>> = mapOf(
        "Arica y Parinacota" to listOf("Arica", "Camarones", "Putre", "General Lagos"),
        "Tarapacá" to listOf("Iquique", "Alto Hospicio", "Pozo Almonte"),
        "Antofagasta" to listOf("Antofagasta", "Mejillones", "Calama")
    )

    override fun signOut() {}

    override fun sendPasswordResetEmail(email: String) {}
}
