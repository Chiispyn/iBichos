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

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val user: FirebaseUser? = null
    )

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _uiState.update { it.copy(user = user) }
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
        comuna: String,
        birthDate: String,
        gender: String
    ) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank() ||
            region.isBlank() || comuna.isBlank() || birthDate.isBlank() || gender.isBlank()) {
            _uiState.update { it.copy(error = "Completá todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid    = result.user?.uid ?: ""

                // Crear documento de usuario en Firestore
                db.collection("users").document(uid).set(
                    hashMapOf(
                        "displayName" to displayName,
                        "email"       to email,
                        "region"      to region,
                        "comuna"      to comuna,
                        "birthDate"   to birthDate,
                        "gender"      to gender,
                        "level"       to "Casual",
                        "xp"          to 0L,
                        "createdAt"   to Timestamp.now(),
                        "uniqueInsectsCount" to 0,
                        "medals"      to emptyList<String>(),
                        "categoryCounts" to emptyMap<String, Int>()
                    )
                ).await()

                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
                setError("No se pudo obtener el idToken")
            }
        } catch (e: Exception) {
            setError(e.message ?: "Error Google Sign-In")
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()

                _uiState.value = AuthUiState(
                    isLoading = false,
                    user = result.user,
                    error = null
                )
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


