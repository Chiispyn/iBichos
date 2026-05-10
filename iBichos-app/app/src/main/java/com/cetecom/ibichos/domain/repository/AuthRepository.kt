package com.cetecom.ibichos.domain.repository

/**
 * Contrato para las operaciones de autenticación y gestión de cuenta.
 * Abstrae Firebase Auth y las escrituras iniciales en Firestore.
 */
interface AuthRepository {
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): String?

    /** Inicia sesión con email/contraseña. Lanza excepción si falla. */
    suspend fun login(email: String, password: String)

    /**
     * Registra un nuevo usuario y crea su documento en Firestore.
     * @return uid del usuario recién creado
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ): String

    /**
     * Autentica con Google y crea el documento de Firestore si es usuario nuevo.
     * @return Pair(uid, isNewUser)
     */
    suspend fun signInWithGoogle(idToken: String): Pair<String, Boolean>

    /** Completa el perfil del usuario (datos demográficos) */
    suspend fun completeProfile(
        uid: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    )

    /** Verifica si el perfil del usuario tiene los campos obligatorios completos */
    suspend fun checkProfileCompletion(uid: String): Boolean

    /** Carga las regiones/ciudades desde Firestore metadata */
    suspend fun getLocations(): Map<String, List<String>>

    /** Cierra la sesión del usuario actual */
    fun signOut()

    /** Envía email de recuperación de contraseña */
    fun sendPasswordResetEmail(email: String)
}
