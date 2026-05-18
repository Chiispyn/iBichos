package com.cetecom.ibichos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.cetecom.ibichos.domain.repository.SessionRepository
import com.cetecom.ibichos.presentation.navigation.AppNavigation
import com.cetecom.ibichos.data.local.ThemePreferences
import com.cetecom.ibichos.presentation.theme.IBichosTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences> {
    error("No ThemePreferences provided")
}

/**
 * Single Activity — punto de entrada de la app.
 * Solo monta el NavHost dentro del tema. Toda la navegación ocurre en Compose.
 * También gestiona el ciclo de vida de las AppSessions para métricas del Dashboard.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionRepository: SessionRepository
    private val auth = FirebaseAuth.getInstance()

    /** ID de la sesión activa. Null si no hay sesión iniciada o el usuario no está logueado. */
    private var currentSessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePrefs = ThemePreferences(this)

        enableEdgeToEdge()
        setContent {
            val themeMode by themePrefs.themeMode.collectAsState()
            val darkTheme = false // Obligado a false por petición de diseño para presentación

            CompositionLocalProvider(LocalThemePreferences provides themePrefs) {
                IBichosTheme(darkTheme = darkTheme) {
                    AppNavigation()
                }
            }
        }
    }

    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onStart() {
        super.onStart()
        // Escuchar cambios de sesión (Login / Logout) en tiempo real
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                // Si el usuario está logueado y no hay sesión activa, iniciarla
                if (currentSessionId == null) {
                    lifecycleScope.launch {
                        try {
                            currentSessionId = sessionRepository.startSession(uid)
                        } catch (_: Exception) {}
                    }
                }
            } else {
                // Si se desloguea, cerrar la sesión activa
                endCurrentSession()
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        // Al minimizar la app, dejamos de escuchar el login y cerramos la sesión
        authStateListener?.let { auth.removeAuthStateListener(it) }
        endCurrentSession()
    }

    private fun endCurrentSession() {
        val sessionId = currentSessionId ?: return
        lifecycleScope.launch {
            try {
                sessionRepository.endSession(sessionId)
                currentSessionId = null
            } catch (_: Exception) {}
        }
    }
}
