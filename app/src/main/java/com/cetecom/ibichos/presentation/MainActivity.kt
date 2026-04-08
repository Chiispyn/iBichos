package com.cetecom.ibichos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cetecom.ibichos.presentation.navigation.AppNavigation
import com.cetecom.ibichos.ui.theme.IBichosTheme

/**
 * Single Activity — punto de entrada de la app.
 * Solo monta el NavHost dentro del tema. Toda la navegación ocurre en Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IBichosTheme {
                AppNavigation()
            }
        }
    }
}
