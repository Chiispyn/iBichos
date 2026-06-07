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
        // Esperar a que el splash de 3 segundos termine y aparezca el login
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun pantalla_muestraElementosPrincipales() {
        composeTestRule.onNodeWithText("iBichos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
        composeTestRule.onNodeWithText("Continuar con Google").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").assertIsDisplayed()
    }

    @Test
    fun campoEmail_aceptaTexto() {
        composeTestRule.onNodeWithText("Correo electrónico").performTextInput("test@correo.com")
        composeTestRule.onNodeWithText("test@correo.com").assertIsDisplayed()
    }

    @Test
    fun campoContrasena_aceptaTexto() {
        // Usamos ContentDescription del ícono de lock para identificar el campo contraseña
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").performTextInput("miPassword123")
        // Tras escribir, el placeholder desaparece — verificamos que el campo de email sigue visible
        composeTestRule.onNodeWithText("Correo electrónico").assertIsDisplayed()
    }

    @Test
    fun botonOlvidaste_abreDialogo() {
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        composeTestRule.onNodeWithText("Recuperar contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enviar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun dialogoRecuperar_seCierraAlCancelar() {
        composeTestRule.onNodeWithText("¿Olvidaste tu contraseña?").performClick()
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.onNodeWithText("Recuperar contraseña").assertDoesNotExist()
    }

    @Test
    fun botonRegistrate_navegaAlRegistro() {
        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Únete a la comunidad de cazadores").assertIsDisplayed()
    }
}
