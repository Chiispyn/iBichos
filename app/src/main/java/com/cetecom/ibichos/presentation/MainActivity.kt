package com.cetecom.ibichos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.cetecom.ibichos.presentation.navigation.AppNavigation
import com.cetecom.ibichos.ui.theme.IBichosTheme
import com.cetecom.ibichos.utils.ThemeMode
import com.cetecom.ibichos.utils.ThemePreferences

val LocalThemePreferences = staticCompositionLocalOf<ThemePreferences> {
    error("No ThemePreferences provided")
}

/**
 * Single Activity — punto de entrada de la app.
 * Solo monta el NavHost dentro del tema. Toda la navegación ocurre en Compose.
 */
class MainActivity : ComponentActivity() {
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
}

