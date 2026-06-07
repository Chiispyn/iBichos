package com.cetecom.ibichos.presentation.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit4.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
