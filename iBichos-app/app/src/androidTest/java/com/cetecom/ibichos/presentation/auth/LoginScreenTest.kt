package com.cetecom.ibichos.presentation.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.cetecom.ibichos.presentation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class LoginScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun pantalla_muestraElementosPrincipales() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("iBichos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").assertIsDisplayed()
    }

    @Test
    fun campoEmail_aceptaTexto() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Correo electrónico").performTextInput("test@correo.com")
        composeTestRule.onNodeWithText("test@correo.com").assertIsDisplayed()
    }

    @Test
    fun campoContrasena_aceptaTexto() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Contraseña").performTextInput("miPassword123")
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun botonOlvidaste_abreDialogo() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        composeTestRule.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enviar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun dialogoRecuperar_seCierraAlCancelar() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {}
            )
        }

        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.onNodeWithText("Recuperar contraseña").assertDoesNotExist()
    }

    @Test
    fun botonRegistrate_navegaAlRegistro() {
        var navegoARegistro = false

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navegoARegistro = true }
            )
        }

        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").performClick()
        assert(navegoARegistro)
    }
}
