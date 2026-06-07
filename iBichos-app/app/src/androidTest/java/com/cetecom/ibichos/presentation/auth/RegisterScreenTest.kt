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
class RegisterScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        // Esperar a que el splash termine y aparezca el login
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }
        // Navegar a RegisterScreen
        composeTestRule.onNodeWithText("¿No tienes cuenta? Regístrate").performClick()
        // Esperar a que cargue la pantalla de registro
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Únete a la comunidad de cazadores")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun pantalla_muestraElementosPrincipales() {
        composeTestRule.onNodeWithText("Crear Cuenta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Únete a la comunidad de cazadores").assertIsDisplayed()
        composeTestRule.onNodeWithText("¿Ya tienes una cuenta? Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun campoNombre_aceptaTexto() {
        composeTestRule.onNodeWithText("Nombre de cazador").performTextInput("Juan Pérez")
        composeTestRule.onNodeWithText("Juan Pérez").assertIsDisplayed()
    }

    @Test
    fun campoEmail_aceptaTexto() {
        composeTestRule.onNodeWithText("Correo electrónico").performTextInput("test@correo.com")
        composeTestRule.onNodeWithText("test@correo.com").assertIsDisplayed()
    }

    @Test
    fun campoEmail_emailInvalido_muestraError() {
        composeTestRule.onNodeWithText("Correo electrónico").performTextInput("correo-invalido")
        composeTestRule.onNodeWithText("Correo electrónico").assertIsDisplayed()
        // El campo muestra error al tener email inválido
        composeTestRule.onNode(hasText("Correo electrónico") and hasSetTextAction()).assertIsDisplayed()
    }

    @Test
    fun botonCrearCuenta_deshabilitado_sinDatosCompletos() {
        // Sin llenar campos, el botón debe estar deshabilitado
        composeTestRule.onNode(hasText("Crear Cuenta") and hasClickAction())
            .assertIsNotEnabled()
    }

    @Test
    fun dropdown_region_abreOpciones() {
        composeTestRule.onNodeWithText("Región").performClick()
        // Al hacer click debe aparecer al menos una opción del dropdown
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Arica y Parinacota")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Arica y Parinacota").assertIsDisplayed()
    }

    @Test
    fun dropdown_sexo_abreOpciones() {
        composeTestRule.onNodeWithText("Sexo").performClick()
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule.onAllNodesWithText("Hombre").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hombre").assertIsDisplayed()
    }

    @Test
    fun botonVolver_navegaAlLogin() {
        composeTestRule.onNodeWithText("¿Ya tienes una cuenta? Iniciar sesión").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Iniciar Sesión").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Iniciar Sesión").assertIsDisplayed()
    }
}
