package com.cetecom.ibichos.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.app.Activity
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.cetecom.ibichos.data.repository.EventRepositoryImpl

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null
    )

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val eventRepo = EventRepositoryImpl()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _locations = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val locations: StateFlow<Map<String, List<String>>> = _locations.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update { it.copy(user = user) }
        }
        loadLocations()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            try {
                val doc = db.collection("metadata").document("locations").get().await()
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val regions = doc.get("regions") as? Map<String, List<String>>
                    if (regions != null) {
                        _locations.value = regions
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** ¿Hay sesión activa? Se evalúa al arrancar la pantalla de Login */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank() ||
            region.isBlank() || city.isBlank() || birthDate.isBlank() || gender.isBlank()) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid    = result.user?.uid ?: ""

                // Crear documento con la nueva estructura (gamification como sub-mapa)
                db.collection("users").document(uid).set(
                    hashMapOf(
                        // Datos del perfil
                        "displayName"        to displayName,
                        "email"              to email,
                        "region"             to region,
                        "city"               to city,
                        "birthDate"          to birthDate,
                        "gender"             to gender,
                        "avatarUrl"          to null,
                        "totalCaptures"      to 0,
                        "createdAt"          to Timestamp.now(),

                        // Moderación
                        "strikes"            to 0,
                        "isShadowBanned"     to false,

                        // Campos raíz denormalizados para queries de ranking sin índices compuestos
                        "xp"                 to 0L,
                        "uniqueInsectsCount" to 0,
                        "medalsCount"        to 0,

                        // Sub-documento de gamificación
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

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                // 📝 Log de registro para analíticas históricas
                runCatching { eventRepo.logUserRegistered(uid) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun completeProfile(
        region: String,
        city: String,
        birthDate: String,
        gender: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        if (region.isBlank() || city.isBlank() || birthDate.isBlank() || gender.isBlank()) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                db.collection("users").document(uid).update(
                    mapOf(
                        "region"    to region,
                        "city"      to city,
                        "birthDate" to birthDate,
                        "gender"    to gender
                    )
                ).await()
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun checkProfileCompletion(onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                val region = doc.getString("region") ?: ""
                val city = doc.getString("city") ?: ""
                val birthDate = doc.getString("birthDate") ?: ""
                onResult(region.isNotEmpty() && city.isNotEmpty() && birthDate.isNotEmpty())
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun resetSuccess() = _uiState.update { it.copy(isSuccess = false) }


    fun handleGoogleResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            setError("Inicio de sesión cancelado")
            return
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                signInWithGoogle(idToken)
            } else {
                setError("No se pudo obtener la identidad de Google")
            }
        } catch (e: Exception) {
            setError("Error al conectar con Google")
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user

                // Crear documento en Firestore solo si es un usuario NUEVO con Google
                if (result.additionalUserInfo?.isNewUser == true && user != null) {
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

                _uiState.value = AuthUiState(
                    isLoading = false,
                    user = result.user,
                    error = null
                )
                // 📝 Log de registro para nuevos usuarios de Google
                if (result.additionalUserInfo?.isNewUser == true && user != null) {
                    runCatching { eventRepo.logUserRegistered(user.uid) }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error Firebase"
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}


