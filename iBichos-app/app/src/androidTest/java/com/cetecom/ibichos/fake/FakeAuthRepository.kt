package com.cetecom.ibichos.fake

import com.cetecom.ibichos.domain.repository.AuthRepository

/**
 * Implementación falsa de AuthRepository para tests de UI.
 *
 * Flags de companion object (configurar ANTES de hiltRule.inject()):
 *   - initialUserId: String?  → si null, simula usuario no autenticado (LoginScreen no auto-navega)
 *   - shouldFailRegister: Boolean → si true, register() lanza la excepción registerErrorMessage
 *   - registerErrorMessage: String → mensaje de error al registrar
 *
 * Resetear todos los flags a su valor por defecto en @After para no contaminar otros tests.
 */
class FakeAuthRepository : AuthRepository {

    companion object {
        /** null = usuario no autenticado; "test_uid" = autenticado (default) */
        var initialUserId: String? = "test_uid"
        var shouldFailRegister = false
        var registerErrorMessage = "El correo electrónico ya está registrado"
    }

    // Estado de sesión mutable por instancia (login/logout cambia este valor)
    private var userId: String? = initialUserId

    override fun isLoggedIn(): Boolean = userId != null
    override fun getCurrentUserId(): String? = userId

    override suspend fun login(email: String, password: String) {
        // Simula login exitoso: el usuario queda autenticado
        userId = "test_uid"
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
        if (shouldFailRegister) throw Exception(registerErrorMessage)
        userId = "test_uid"
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

    override fun signOut() { userId = null }

    override fun sendPasswordResetEmail(email: String) {}
}
